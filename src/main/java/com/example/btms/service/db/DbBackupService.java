package com.example.btms.service.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Simple DB backup utility supporting H2 and SQL Server.
 */
public class DbBackupService {

    public enum DbKind {
        H2, SQLSRV, UNKNOWN
    }

    public static class BackupResult {
        public final File targetFile;
        public final long millis;

        public BackupResult(File targetFile, long millis) {
            this.targetFile = targetFile;
            this.millis = millis;
        }
    }

    public DbKind detectKind(Connection conn) {
        try {
            String url = conn.getMetaData().getURL();
            if (url == null)
                return DbKind.UNKNOWN;
            if (url.startsWith("jdbc:h2:"))
                return DbKind.H2;
            if (url.startsWith("jdbc:sqlserver:"))
                return DbKind.SQLSRV;
            return DbKind.UNKNOWN;
        } catch (SQLException e) {
            return DbKind.UNKNOWN;
        }
    }

    public String detectCurrentDatabase(Connection conn, DbKind kind) throws SQLException {
        if (kind == DbKind.SQLSRV) {
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT DB_NAME()")) {
                if (rs.next())
                    return rs.getString(1);
            }
            return null;
        } else if (kind == DbKind.H2) {
            String url = conn.getMetaData().getURL();
            if (url != null) {
                int idx = url.lastIndexOf('/') >= 0 ? url.lastIndexOf('/') : url.lastIndexOf('\\');
                if (idx >= 0 && idx + 1 < url.length()) {
                    String tail = url.substring(idx + 1);
                    int semi = tail.indexOf(';');
                    return semi > 0 ? tail.substring(0, semi) : tail;
                }
            }
            return "h2";
        }
        return null;
    }

    public BackupResult backupNow(Connection conn, File outDir) throws Exception {
        if (outDir == null)
            throw new IllegalArgumentException("outDir is null");
        if (!outDir.exists() && !outDir.mkdirs())
            throw new IllegalStateException("Không thể tạo thư mục: " + outDir);

        DbKind kind = detectKind(conn);
        String dbName = detectCurrentDatabase(conn, kind);
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        long start = System.currentTimeMillis();

        switch (kind) {
            case H2: {
                // Dùng lệnh H2 BACKUP TO để tạo snapshot .zip an toàn khi DB đang mở, rồi trích
                // .mv.db
                File target = new File(outDir, "backup_btms_" + ts + ".mv.db");
                File tmpZip = File.createTempFile("h2backup_", ".zip", outDir);
                String zipPath = tmpZip.getAbsolutePath().replace("'", "''");
                try (Statement st = conn.createStatement()) {
                    st.execute("BACKUP TO '" + zipPath + "'");
                }
                if (!tmpZip.exists() || tmpZip.length() == 0) {
                    throw new IOException("Không tạo được gói BACKUP (.zip). Kiểm tra quyền ghi thư mục.");
                }
                extractFirstMvDbFromZip(tmpZip, target);
                try {
                    Files.deleteIfExists(tmpZip.toPath());
                } catch (IOException ignore) {
                }
                return new BackupResult(target, System.currentTimeMillis() - start);
            }
            case SQLSRV: {
                String base = (dbName == null || dbName.isBlank()) ? "database" : dbName;
                File target = new File(outDir, base + "_" + ts + ".bak");
                String path = target.getAbsolutePath().replace("'", "''");
                try (Statement st = conn.createStatement()) {
                    String sql = "BACKUP DATABASE [" + base + "] TO DISK = N'" + path + "' WITH INIT, COMPRESSION";
                    st.execute(sql);
                }
                return new BackupResult(target, System.currentTimeMillis() - start);
            }
            case UNKNOWN:
            default:
                throw new UnsupportedOperationException("Không nhận diện được loại CSDL");
        }
    }

    private void extractFirstMvDbFromZip(File zipFile, File target) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String name = entry.getName();
                    if (name != null && name.toLowerCase().endsWith(".mv.db")) {
                        Files.copy(zis, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        return;
                    }
                }
            }
        }
        throw new IOException("Không tìm thấy file .mv.db trong gói BACKUP.");
    }
}
