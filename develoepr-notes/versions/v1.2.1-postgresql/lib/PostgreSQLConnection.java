package lib;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQLConnection {

    private static final String URL      = "jdbc:postgresql://localhost:5432/javafx_wireshark";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "commonpassword";

    // Function to establish a connection to PostgreSQL
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
