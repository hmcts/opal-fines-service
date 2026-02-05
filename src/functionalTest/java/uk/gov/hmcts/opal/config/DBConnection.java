package uk.gov.hmcts.opal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    static Logger log = LoggerFactory.getLogger(DBConnection.class.getName());

    public Connection getPostgresConnection() {
        Connection conn = null;

        log.info("Making postgres connection");
        //Local connection only
        String url =  "jdbc:postgresql://localhost/";
        String database = "opal-fines-db";
        String username = "opal-fines";
        String password = "opal-fines";

        Properties props = new Properties();
        props.put("user", username);
        props.put("password", password);
        try {
            conn = DriverManager.getConnection(
                url + database,
                props
            );
            log.info("Successfully made to postgres database");
        } catch (SQLException e) {
            log.info("Error: " + e);
        }
        return conn;
    }
}
