package com.example.btms.util.h2db;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tiện ích khởi tạo cơ sở dữ liệu H2 từ script SQL Server
 * Dùng cho file DB cục bộ, không cần MODE=MSSQLServer
 */
public class H2ScriptUtil {

    private H2ScriptUtil() {
    }

    /**
     * Callback interface for logging during database creation
     */
    public interface LogCallback {
        void log(String message);
    }

    /**
     * Đọc file script SQL Server, chuyển đổi cú pháp sang H2 rồi chạy trên DB file.
     */
    public static void runSqlServerScriptOnH2FileDb(String dbName, File scriptFile) throws Exception {
        runSqlServerScriptOnH2FileDb(dbName, scriptFile, null);
    }

    /**
     * Đọc file script SQL Server, chuyển đổi cú pháp sang H2 rồi chạy trên DB file
     * với log callback.
     */
    public static void runSqlServerScriptOnH2FileDb(String dbName, File scriptFile, LogCallback logger)
            throws Exception {
        if (scriptFile == null) {
            throw new Exception("Script file is null");
        }
        if (!scriptFile.exists()) {
            throw new Exception("Script file does not exist: " + scriptFile.getAbsolutePath());
        }
        if (!scriptFile.isFile()) {
            throw new Exception("Script path is not a file: " + scriptFile.getAbsolutePath());
        }

        logMessage(logger, "📖 Đọc file script: " + scriptFile.getName());
        String raw = readTextAuto(scriptFile);

        logMessage(logger, "🔄 Chuyển đổi cú pháp SQL Server sang H2...");
        String converted = convertSqlServerToH2(raw);

        // KHÔNG DÙNG MODE=MSSQLServer nữa
        String url = "jdbc:h2:file:./database/" + dbName
                + ";DATABASE_TO_UPPER=FALSE;AUTO_SERVER=TRUE;INIT=CREATE SCHEMA IF NOT EXISTS PUBLIC\\;SET SCHEMA PUBLIC";

        logMessage(logger, "🔗 Kết nối tới H2 database...");
        try (Connection c = DriverManager.getConnection(url, "sa", "")) {
            c.setAutoCommit(false);
            try (Statement st = c.createStatement()) {
                logMessage(logger, "🏗️ Tạo schema và thiết lập database...");
                st.execute("CREATE SCHEMA IF NOT EXISTS PUBLIC");
                st.execute("SET SCHEMA PUBLIC");

                String[] statements = splitBySemicolon(converted);
                logMessage(logger, "⚡ Thực thi " + statements.length + " câu lệnh SQL...");

                int executedCount = 0;
                for (String sql : statements) {
                    if (sql.isBlank())
                        continue;
                    try {
                        st.execute(sql);
                        executedCount++;
                        if (executedCount % 5 == 0) {
                            logMessage(logger,
                                    "✅ Đã thực thi " + executedCount + "/" + statements.length + " câu lệnh...");
                        }
                    } catch (SQLException ex) {
                        String snip = sql.length() > 240 ? sql.substring(0, 240) + "..." : sql;
                        throw new SQLException("❌ Lỗi thực thi câu lệnh SQL #" + executedCount + ": " + snip + "\n"
                                + ex.getMessage(), ex);
                    }
                }

                logMessage(logger, "✅ Thành công thực thi " + executedCount + " câu lệnh SQL.");
            }
            logMessage(logger, "💾 Commit transaction...");
            c.commit();
            logMessage(logger, "🎉 Khởi tạo schema database hoàn tất!");
        }
    }

    /** Chuyển đổi cú pháp SQL Server → H2 */
    public static String convertSqlServerToH2(String raw) {
        String s = raw.replace("\r\n", "\n");

        // Loại bỏ các lệnh không hợp lệ trong H2
        s = s.replaceAll("(?im)^\\s*CREATE\\s+SCHEMA\\s+DBO\\b.*$", "");
        s = s.replaceAll("(?im)^\\s*ALTER\\s+AUTHORIZATION\\s+ON\\s+SCHEMA::DBO\\b.*$", "");
        s = s.replaceAll("(?im)^\\s*ALTER\\s+SCHEMA\\s+DBO\\b.*$", "");

        // IF EXISTS
        s = s.replaceAll(
                "(?im)^\\s*IF\\s+OBJECT_ID\\([^)]*\\)\\s+IS\\s+NOT\\s+NULL\\s+DROP\\s+TABLE\\s+([^;\\r\\n]+);?\\s*$",
                "DROP TABLE IF EXISTS $1;");

        // Loại bỏ GO
        s = s.replaceAll("(?im)^\\s*GO\\s*(?:--.*)?$", "");

        // Xóa [dbo]. / [] / N'
        s = s.replaceAll("(?i)\\[dbo\\]\\.", "");
        s = s.replaceAll("(?i)\\bdbo\\.", "");
        s = s.replace("[", "");
        s = s.replace("]", "");
        s = s.replaceAll("(?i)N'", "'");

        // Kiểu dữ liệu tương thích
        s = s.replaceAll("(?i)nvarchar\\(max\\)", "varchar(1000)");
        s = s.replaceAll("(?i)varchar\\(max\\)", "varchar(1000)");
        s = s.replaceAll("(?i)\\bDATETIME\\b", "TIMESTAMP");

        // Sửa các lỗi ngoặc và dấu phẩy
        s = s.replaceAll(",\\s*\\)", ")");
        s = s.replaceAll("(?m)^\\)\\s*$", ");");

        // Xóa USE database
        s = s.replaceAll("(?im)^\\s*USE\\s+\\S+\\s*;?\\s*$", "");

        return s;
    }

    private static String[] splitBySemicolon(String s) {
        return s.split(";\\s*(?:\\r?\\n|$)");
    }

    /** Đọc text tự động phát hiện encoding (UTF-8/UTF-16) */
    public static String readTextAuto(File file) throws java.io.IOException {
        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF)
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);

        if (bytes.length >= 2) {
            int b0 = bytes[0] & 0xFF, b1 = bytes[1] & 0xFF;
            if (b0 == 0xFF && b1 == 0xFE)
                return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
            if (b0 == 0xFE && b1 == 0xFF)
                return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        }

        CharsetDecoder dec = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        try {
            return dec.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            try {
                return StandardCharsets.UTF_16LE.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(bytes)).toString();
            } catch (CharacterCodingException e2) {
                try {
                    return StandardCharsets.UTF_16BE.newDecoder()
                            .onMalformedInput(CodingErrorAction.REPORT)
                            .onUnmappableCharacter(CodingErrorAction.REPORT)
                            .decode(ByteBuffer.wrap(bytes)).toString();
                } catch (CharacterCodingException e3) {
                    return new String(bytes, StandardCharsets.UTF_8);
                }
            }
        }
    }

    /**
     * Helper method to log messages to both console and callback
     */
    private static void logMessage(LogCallback logger, String message) {
        System.out.println(message);
        if (logger != null) {
            logger.log(message);
        }
    }
}
