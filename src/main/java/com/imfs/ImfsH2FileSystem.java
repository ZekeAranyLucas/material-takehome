package com.imfs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import org.h2.jdbcx.JdbcConnectionPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImfsH2FileSystem extends ImfsFileSystem {
    private static final List<ImfsRecord> TEST_DIRS = Arrays.asList(
            ImfsRecord.ofDir("math"),
            ImfsRecord.ofDir("history"),
            ImfsRecord.ofDir("Spanish"));

    private ImfsProvider provider;
    private String key;
    private final ImfsRecord ROOT = ImfsRecord.ofDir("");
    // Set up the H2 database
    private final JdbcConnectionPool pool;

    public ImfsH2FileSystem(ImfsProvider imfsProvider, String key) {
        this.provider = imfsProvider;
        this.key = key;
        this.pool = JdbcConnectionPool.create("jdbc:h2:mem:" + key, "sa", "");
        initTable();
        initEntries();

    }

    private void initEntries() {
        try (Connection conn = pool.getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM ImfsRecords");

            for (ImfsRecord record : TEST_DIRS) {
                stmt.execute("INSERT INTO ImfsRecords (path) VALUES ('" + record.getMaterializedPath() + "')");
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void initTable() {
        try (Connection conn = pool.getConnection();
                Statement stmt = conn.createStatement()) {

            // Create the blobs table first
            stmt.execute("CREATE TABLE blobs ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "data BLOB)");

            // Create the ImfsRecords table with a foreign key to the blobs table
            stmt.execute("CREATE TABLE ImfsRecords ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "path VARCHAR(255) UNIQUE NOT NULL, "
                    + "blob_id INT, "
                    + "FOREIGN KEY (blob_id) REFERENCES blobs(id))");

            stmt.execute("CREATE INDEX idx_name ON ImfsRecords(path)");

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        this.pool.dispose();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFileStores'");
    }

    @Override
    public Path getPath(String arg0, String... arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPath'");
    }

    @Override
    public PathMatcher getPathMatcher(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPathMatcher'");
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRootDirectories'");
    }

    @Override
    public String getSeparator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSeparator'");
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserPrincipalLookupService'");
    }

    @Override
    public boolean isOpen() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isOpen'");
    }

    @Override
    public boolean isReadOnly() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isReadOnly'");
    }

    @Override
    public WatchService newWatchService() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'newWatchService'");
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'supportedFileAttributeViews'");
    }

    public String getKey() {
        return key;
    }

    public ImfsDirectoryStream streamAllPaths(Filter<? super Path> filter) {

        try (Connection conn = pool.getConnection();
                Statement stmt = conn.createStatement()) {
            //
            var result = stmt.executeQuery("SELECT * FROM ImfsRecords WHERE path NOT LIKE '%/%'");
            // var stream = resultSetToStream(result).map(rs -> {
            // try {
            // return rs.getString("path");
            // } catch (SQLException ex) {
            // throw new RuntimeException(ex);
            // }
            // });
            var stream = resultSetToList(result).stream();
            return new ImfsDirectoryStream(this, key, "", stream, -1, filter);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public ImfsDirectoryStream streamChildren(String materializedPath, Filter<? super Path> filter) {
        if (materializedPath.length() == 0) {
            return streamAllPaths(filter);
        }
        try (Connection conn = pool.getConnection();
                Statement stmt = conn.createStatement()) {
            var query = "SELECT * FROM ImfsRecords WHERE path LIKE '" + materializedPath + "/%' AND path NOT LIKE '"
                    + materializedPath
                    + "/%/%'";
            var result = stmt.executeQuery(query);
            // var stream = resultSetToStream(result).map(rs -> {
            // try {
            // return rs.getString("path");
            // } catch (SQLException ex) {
            // throw new RuntimeException(ex);
            // }
            // });
            var stream = resultSetToList(result).stream();
            return new ImfsDirectoryStream(this, key, materializedPath, stream, -1, filter);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private List<String> resultSetToList(ResultSet rs) {
        List<String> list = new ArrayList<>();
        try {
            while (rs.next()) {
                list.add(rs.getString("path"));
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        list.sort(String::compareTo);
        return list;
    }

    public boolean contains(String materializedPath) {
        try (Connection conn = pool.getConnection();
                Statement stmt = conn.createStatement()) {
            var query = "SELECT COUNT(*) FROM ImfsRecords WHERE path = '" + materializedPath + "'";
            var result = stmt.executeQuery(query);
            result.next();
            return result.getInt(1) > 0;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void reset() {
        initEntries();
    }

    public void removeEntry(String materializedPath) {
        try (Connection conn = pool.getConnection();
                Statement stmt = conn.createStatement()) {
            var query = "DELETE FROM ImfsRecords WHERE path = '" + materializedPath + "'";
            stmt.execute(query);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void putBlob(String materializedPath, byte[] bytes) {
        // Insert the blob and get the generated ID
        int blobId = putBytes(bytes);
        var record = ImfsRecord.builder()
                .materializedPath(materializedPath)
                .file(true)
                .blobId(blobId)
                .build();
        putRecord(record);
    }

    private int putBytes(byte[] data) {
        String sql = "INSERT INTO blobs (data) VALUES (?)";
        try (Connection conn = pool.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setBytes(1, data);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating blob failed, no ID obtained.");
                }
            }

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public byte[] getBlob(String materializedPath) {
        String sql = "SELECT b.data FROM blobs b JOIN ImfsRecords r ON b.id = r.blob_id WHERE r.path = ?";
        try (Connection conn = pool.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, materializedPath);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getBytes("data");
            } else {
                throw new SQLException("No blob found for path: " + materializedPath);
            }

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public ImfsRecord getRecord(String materializedPath) {
        if (materializedPath.length() == 0) {
            return ROOT;
        }
        ImfsRecord record = null;
        String sql = "SELECT * FROM ImfsRecords WHERE path = ?";

        try (Connection conn = pool.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, materializedPath);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                record = ImfsRecord.builder()
                        .materializedPath(rs.getString("path"))
                        .file(rs.getInt("blob_id") != 0)
                        .blobId(rs.getInt("blob_id"))
                        .build();
                return record;
            } else {
                return null;
            }

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void putRecord(ImfsRecord record) {
        String sql = "MERGE INTO ImfsRecords (path, blob_id) KEY(path) VALUES (?, ?)";

        try (Connection conn = pool.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, record.getMaterializedPath());
            if (record.isFile()) {
                pstmt.setInt(2, record.getBlobId());
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }
            pstmt.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
