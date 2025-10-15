package com.example.btms.util.h2db;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.io.File;
import java.nio.ByteBuffer;
import java.sql.*;

public class H2ScriptUtil {
    private H2ScriptUtil() {
    }

    /* ---------- PUBLIC API ---------- */

    /** Đọc file script, convert T-SQL → H2 và chạy trên H2 (file DB). */
    public static void runSqlServerScriptOnH2FileDb(String dbName, File scriptFile) throws Exception {
        if (scriptFile == null || !scriptFile.isFile())
            return;
        String raw = readTextAuto(scriptFile);
        String converted = convertSqlServerToH2(raw);

        String url = "jdbc:h2:file:./database/" + dbName + ";MODE=MSSQLServer;DATABASE_TO_UPPER=FALSE";
        try (Connection c = DriverManager.getConnection(url, "sa", "")) {
            c.setAutoCommit(false);
            try (Statement st = c.createStatement()) {
                st.execute("CREATE SCHEMA IF NOT EXISTS dbo");
                st.execute("SET SCHEMA dbo");
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

    /** Chỉ chuyển đổi T-SQL → H2 (nếu bạn muốn tự chạy). */
    public static String convertSqlServerToH2(String raw) {
        String s = raw.replace("\r\n", "\n");

        // DROP IF EXISTS
        s = s.replaceAll(
                "(?im)^\\s*IF\\s+OBJECT_ID\\([^)]*\\)\\s+IS\\s+NOT\\s+NULL\\s+DROP\\s+TABLE\\s+([^;\\r\\n]+);?\\s*$",
                "DROP TABLE IF EXISTS $1;");

        // Loại GO
        s = s.replaceAll("(?im)^\\s*GO\\s*(?:--.*)?$", "");

        // WITH CHECK & CHECK CONSTRAINT
        s = s.replaceAll("(?i)\\sWITH\\s+CHECK\\s+ADD\\s+", " ADD ");
        s = s.replaceAll("(?im)^\\s*ALTER\\s+TABLE\\s+.*CHECK\\s+CONSTRAINT.*$\\n?", "");

        // [dbo]. / dbo. / [] / N'...'
        s = s.replaceAll("(?i)\\[dbo\\]\\.", "");
        s = s.replaceAll("(?i)\\bdbo\\.", "");
        s = s.replace("[", "");
        s = s.replace("]", "");
        s = s.replaceAll("(?i)N'", "'");

        // MAX → varchar(1000) | DATETIME → TIMESTAMP
        s = s.replaceAll("(?i)nvarchar\\(max\\)", "varchar(1000)");
        s = s.replaceAll("(?i)varchar\\(max\\)", "varchar(1000)");
        s = s.replaceAll("(?i)\\bDATETIME\\b", "TIMESTAMP");

        // Dọn dấu phẩy/ngoặc
        s = s.replaceAll(",\\s*\\)", ")");
        s = s.replaceAll("(?m)^\\)\\s*$", ");");
        s = s.replaceAll("(?i)\\)\\s*(\\n\\s*CREATE\\s+TABLE)", ");$1");

        // Gộp REFERENCES
        s = s.replaceAll("(?im)\\n\\s*REFERENCES\\s+", " REFERENCES ");

        // GHÉP ALTER TABLE nhiều dòng → 1 dòng & thêm ;
        s = s.replaceAll("(?im)^\\s*ALTER\\s+TABLE\\s+([^\\n]+)\\n\\s*ADD\\s+", "ALTER TABLE $1 ADD ");
        s = s.replaceAll("(?im)\\n\\s+(CONSTRAINT\\s+)", " $1");
        s = s.replaceAll("(?im)^(\\s*ALTER\\s+TABLE\\s+[^;]+)$", "$1;");

        // Xoá USE db; nếu có
        s = s.replaceAll("(?im)^\\s*USE\\s+\\S+\\s*;?\\s*$", "");

        return s;
    }

    /* ---------- Helpers ---------- */

    private static String[] splitBySemicolon(String s) {
        return s.split(";\\s*(?:\\r?\\n|$)");
    }

    /** Đọc text auto-detect BOM/charset. */
    public static String readTextAuto(File file) throws java.io.IOException {
        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
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
                    return new String(bytes, StandardCharsets.UTF_8); // fallback mềm
                }
            }
        }
    }
}
