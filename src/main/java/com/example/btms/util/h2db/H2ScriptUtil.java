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
     * Đọc file script SQL Server, chuyển đổi cú pháp sang H2 rồi chạy trên DB file.
     */
    public static void runSqlServerScriptOnH2FileDb(String dbName, File scriptFile) throws Exception {
        if (scriptFile == null || !scriptFile.isFile())
            return;

        String raw = readTextAuto(scriptFile);
        String converted = convertSqlServerToH2(raw);

        // KHÔNG DÙNG MODE=MSSQLServer nữa
        String url = "jdbc:h2:file:./database/" + dbName
                + ";DATABASE_TO_UPPER=FALSE;AUTO_SERVER=TRUE;INIT=CREATE SCHEMA IF NOT EXISTS PUBLIC\\;SET SCHEMA PUBLIC";

        try (Connection c = DriverManager.getConnection(url, "sa", "")) {
            c.setAutoCommit(false);
            try (Statement st = c.createStatement()) {
                st.execute("CREATE SCHEMA IF NOT EXISTS PUBLIC");
                st.execute("SET SCHEMA PUBLIC");

                for (String sql : splitBySemicolon(converted)) {
                    if (sql.isBlank())
                        continue;
                    try {
                        st.execute(sql);
                    } catch (SQLException ex) {
                        String snip = sql.length() > 240 ? sql.substring(0, 240) + "..." : sql;
                        throw new SQLException("Lỗi khi chạy SQL: " + snip + "\n" + ex.getMessage(), ex);
                    }
                }
            }
            c.commit();
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
}
