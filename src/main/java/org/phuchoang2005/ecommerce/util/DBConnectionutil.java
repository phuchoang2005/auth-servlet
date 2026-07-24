package org.phuchoang2005.ecommerce.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.phuchoang2005.ecommerce.exceptions.database.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnectionutil {
    private static final Logger logger = LoggerFactory.getLogger(DBConnectionutil.class);
    private static final HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            // Embedded, in-memory H2 in MySQL-compatibility mode.
            // DB_CLOSE_DELAY=-1 keeps the in-memory DB alive for the JVM lifetime,
            // even while HikariCP closes/reopens pooled connections.
            config.setJdbcUrl("jdbc:h2:mem:ecommerce_new;MODE=MySQL;DB_CLOSE_DELAY=-1");
            config.setUsername("sa");
            config.setPassword("");
            config.setDriverClassName("org.h2.Driver");

            // Cấu hình tối ưu hóa HikariCP (Chuẩn công nghiệp)
            config.setMaximumPoolSize(10); // Tối đa 10 kết nối trong hồ
            config.setMinimumIdle(5);      // Luôn giữ ít nhất 5 kết nối rảnh
            config.setIdleTimeout(300000); // 5 phút
            config.setConnectionTimeout(20000); // Đợi tối đa 20s để lấy kết nối

            dataSource = new HikariDataSource(config);
            logger.info("HikariCP DataSource đã được khởi tạo thành công.");
        } catch (Exception e) {
            logger.error("Không thể khởi tạo HikariCP DataSource {}", e);
            throw new DatabaseException("Lỗi cấu hình Database");
        }
    }

    public static Connection getConnection() throws SQLException {
        // Mượn một kết nối từ Pool thay vì tạo mới
        return dataSource.getConnection();
    }
}