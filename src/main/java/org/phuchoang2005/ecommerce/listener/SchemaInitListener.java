package org.phuchoang2005.ecommerce.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.phuchoang2005.ecommerce.util.DBConnectionutil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes the database schema when the application context starts up.
 *
 * <p>Reads {@code db/schema.sql} from the classpath (packaged into the WAR at
 * {@code WEB-INF/classes/db/schema.sql}) and executes each statement. The script is
 * fully idempotent ({@code CREATE ... IF NOT EXISTS}), so this runs safely on every
 * boot and only creates what is missing.</p>
 *
 * <p>It borrows a raw pooled connection directly from {@link DBConnectionutil} — not
 * {@code DBContextUtil} — because no request thread or transaction exists yet at
 * startup. DDL auto-commits in H2. Any failure is rethrown so a broken schema
 * fails fast and is visible in the Tomcat logs.</p>
 */
@WebListener
public class SchemaInitListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(SchemaInitListener.class);
    private static final String SCHEMA_RESOURCE = "db/schema.sql";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("[SCHEMA] Initializing database schema from '{}'", SCHEMA_RESOURCE);

        String script = readSchema();
        int executed = 0;

        try (Connection conn = DBConnectionutil.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String statement : script.split(";")) {
                String sql = statement.trim();
                if (sql.isEmpty()) {
                    continue;
                }
                stmt.execute(sql);
                executed++;
            }
            logger.info("[SCHEMA] Schema initialization complete ({} statements executed).", executed);
        } catch (SQLException e) {
            logger.error("[SCHEMA] Failed to initialize database schema", e);
            throw new IllegalStateException("Database schema initialization failed", e);
        }
    }

    private String readSchema() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Schema resource not found on classpath: " + SCHEMA_RESOURCE);
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Strip full-line SQL comments so they don't leak into split statements.
                    if (line.stripLeading().startsWith("--")) {
                        continue;
                    }
                    sb.append(line).append('\n');
                }
            }
            return sb.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read schema resource: " + SCHEMA_RESOURCE, e);
        }
    }
}
