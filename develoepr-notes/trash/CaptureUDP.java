package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// PostgreSQL Connection
import lib.PostgreSQLConnection;


public class CaptureUDP {

    // Test function to save records in the database
    public void capture_udp_packets(String signalType, String packetData) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // Get a connection to the PostgreSQL database
            conn = PostgreSQLConnection.getConnection();

            // Prepare the SQL statement to insert a new packet record
            String sql = "INSERT INTO packet_records (signal_type, packet_data) VALUES (?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, signalType);
            pstmt.setString(2, packetData);

            // Execute the statement
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("A new packet record was inserted successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Clean up database resources
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
