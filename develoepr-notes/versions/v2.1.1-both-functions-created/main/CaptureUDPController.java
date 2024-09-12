package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import lib.PostgreSQLConnection;

public class CaptureUDPController {

    private CaptureUDP ui;  // Reference to the UI class

    public CaptureUDPController(CaptureUDP ui) {
        this.ui = ui;
    }

    // Method to simulate starting the capture and saving start timestamp
    public void startCapture() {
        ui.updateStatus("Starting UDP packet capture...");
        saveStartTimestamp();
    }

    // Method to simulate stopping the capture and saving stop timestamp
    public void stopCapture() {
        ui.updateStatus("Stopping UDP packet capture...");
        saveStopTimestamp();
    }

    // Method to simulate saving captured data to the database
    public void saveCapturedData(String capturedData) {
        ui.updateStatus("Saving captured data to database...");
        saveCaptureData(capturedData);
    }

    // Add start timestamp to the database
    private void saveStartTimestamp() {
        try (Connection conn = PostgreSQLConnection.getConnection()) {
            String sql = "INSERT INTO udp_captures (start_timestamp) VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.from(Instant.now()));
            pstmt.executeUpdate();
            ui.updateStatus("Start timestamp saved.");
        } catch (SQLException e) {
            e.printStackTrace();
            ui.updateStatus("Error saving start timestamp.");
        }
    }

    // Add stop timestamp to the last row in the database
    private void saveStopTimestamp() {
        try (Connection conn = PostgreSQLConnection.getConnection()) {
            String sql = "UPDATE udp_captures SET stop_timestamp = ? WHERE id = (SELECT MAX(id) FROM udp_captures)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.from(Instant.now()));
            pstmt.executeUpdate();
            ui.updateStatus("Stop timestamp saved.");
        } catch (SQLException e) {
            e.printStackTrace();
            ui.updateStatus("Error saving stop timestamp.");
        }
    }

    // Save captured data to the last row in the database
    private void saveCaptureData(String capturedData) {
        try (Connection conn = PostgreSQLConnection.getConnection()) {
            String sql = "UPDATE udp_captures SET captured_data = ? WHERE id = (SELECT MAX(id) FROM udp_captures)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, capturedData);
            pstmt.executeUpdate();
            ui.updateStatus("Captured data saved.");
        } catch (SQLException e) {
            e.printStackTrace();
            ui.updateStatus("Error saving captured data.");
        }
    }
}
