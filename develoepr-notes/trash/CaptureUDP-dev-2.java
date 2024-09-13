package main;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// PostgreSQL Connection
import lib.PostgreSQLConnection;


public class CaptureUDP {
    
    public void listen_and_capture_udp(
        String host,
        int port,
        Consumer<List<String>> onDataReceived,
        Consumer<Exception> onError,
        Runnable onStop,
        String start_signal,
        String stop_signal,
        int timeout_seconds,
        boolean include_signals_in_message,
        boolean verbose
    ) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            List<String> results = new ArrayList<>();  // To store captured packets
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String captures_directory_path = "captures";
            String outputFilePath = captures_directory_path + "/capture_" + timestamp + ".pcap";
            Process tsharkProcess = null;

            try (DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName(host))) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                boolean listening = false;
                long startTime = System.currentTimeMillis();

                // Create captures directory
                Files.createDirectories(Paths.get(captures_directory_path));

                while (true) {
                    // Receive UDP packet
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());

                    // Start signal detection
                    if (!listening && message.equals(start_signal)) {
                        listening = true;
                        if (verbose) System.out.println("Start signal received, starting tshark...");

                        // Start tshark capture
                        String tsharkCommand = String.format("echo 'semicolon' | sudo tshark -i lo -f \"host %s and port %d\" -w %s &> tf.log", host, port, outputFilePath);
                        tsharkProcess = new ProcessBuilder("bash", "-c", tsharkCommand).start();

                        if (include_signals_in_message) results.add("Start Signal: " + message);
                        continue;
                    }

                    // Stop signal detection
                    if (listening && message.equals(stop_signal)) {
                        if (verbose) System.out.println("Stop signal received, stopping tshark...");

                        if (include_signals_in_message) results.add("Stop Signal: " + message);

                        // Kill the tshark process
                        if (tsharkProcess != null && tsharkProcess.isAlive()) {
                            String killCommand = String.format("echo 'semicolon' | sudo kill %d &>> tf.log", tsharkProcess.pid());
                            new ProcessBuilder("bash", "-c", killCommand).start();
                        }
                        break;  // Exit the loop after the stop signal is received
                    }
                }

                // Parse the captured file using tshark -r
                String tsharkReadCommand = String.format("echo 'semicolon' | tshark -r %s -Y \"udp\" -T fields -e data &>> tf.log", outputFilePath);
                ProcessBuilder readProcessBuilder = new ProcessBuilder("bash", "-c", tsharkReadCommand);
                Process readProcess = readProcessBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(readProcess.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    StringBuilder output = new StringBuilder();
                    for (int i = 0; i < line.length(); i += 2) {
                        String str = line.substring(i, i + 2);
                        output.append((char) Integer.parseInt(str, 16));
                    }
                    results.add(output.toString());  // Add parsed packet to results
                }

                if (onDataReceived != null) {
                    onDataReceived.accept(results);
                }

                if (onStop != null) onStop.run();

            } catch (Exception e) {
                if (onError != null) onError.accept(e);
                if (verbose) e.printStackTrace();
            } finally {
                executorService.shutdown();  // Ensure the executor shuts down
            }
        });
    }


    // Overloaded method 1: auto_stop_after_timeout default to true
    public void listen_and_capture_udp(String host, int port, Consumer<List<String>> onDataReceived, Consumer<Exception> onError, Runnable onStop, String start_signal, String stop_signal, int timeout_seconds, boolean include_signals_in_message, boolean verbose ) {
        listen_and_capture_udp(host, port, onDataReceived, onError, onStop, start_signal, stop_signal, timeout_seconds, true, include_signals_in_message, verbose);
    }

    // Overloaded method 2: with start_signal, stop_signal defaults to null
    public void listen_and_capture_udp(String host, int port, Consumer<List<String>> onDataReceived, Consumer<Exception> onError, Runnable onStop, int timeout_seconds, boolean include_signals_in_message, boolean verbose ) {
        listen_and_capture_udp(host, port, onDataReceived, onError, onStop, null, null, timeout_seconds, true, include_signals_in_message, verbose);
    }

    // Overloaded method 3: minimal params with default values
    public void listen_and_capture_udp(String host, int port, Consumer<List<String>> onDataReceived, Consumer<Exception> onError, Runnable onStop) {
        listen_and_capture_udp(host, port, onDataReceived, onError, onStop, null, null, 30, true, false, false);
    }

    // Overloaded method 4: no onStop callback
    public void listen_and_capture_udp(String host, int port, Consumer<List<String>> onDataReceived, Consumer<Exception> onError) {
        listen_and_capture_udp(host, port, onDataReceived, onError, null, null, null, 30, true, false, false);
    }

    // Overloaded method 5: no onError and onStop
    public void listen_and_capture_udp(String host, int port, Consumer<List<String>> onDataReceived) {
        listen_and_capture_udp(host, port, onDataReceived, null, null, null, null, 30, true, false, false);
    }

    // Overloaded method 6: no onDataReceived, onError, onStop
    public void listen_and_capture_udp(String host, int port) {
        listen_and_capture_udp(host, port, null, null, null, null, null, 30, true, false, false);
    }

    // Overloaded method 7: With start_signal and stop_signal (All Callbacks)
    public void listen_and_capture_udp(String host, int port, Consumer<List<String>> onDataReceived, Consumer<Exception> onError, Runnable onStop, String start_signal, String stop_signal ) {
        listen_and_capture_udp(host, port, onDataReceived, onError, onStop, start_signal, stop_signal, 30, true, false, false);
    }

    // Overloaded method 8: With start_signal and stop_signal (No onStop Callback)
    public void listen_and_capture_udp(String host, int port, Consumer<List<String>> onDataReceived, Consumer<Exception> onError, String start_signal, String stop_signal ) {
        listen_and_capture_udp(host, port, onDataReceived, onError, null, start_signal, stop_signal, 30, true, false, false);
    }

    // Overloaded method 9: With start_signal and stop_signal (No onError or onStop Callback)
    public void listen_and_capture_udp(String host, int port, Consumer<List<String>> onDataReceived, String start_signal, String stop_signal ) {
        listen_and_capture_udp(host, port, onDataReceived, null, null, start_signal, stop_signal, 30, true, false, false);
    }

    // Overloaded method 10: With start_signal, stop_signal, and verbose option (All callbacks)
    public void listen_and_capture_udp(String host, int port, Consumer<List<String>> onDataReceived, Consumer<Exception> onError, Runnable onStop, String start_signal, String stop_signal, boolean verbose ) {
        listen_and_capture_udp(host, port, onDataReceived, onError, onStop, start_signal, stop_signal, 30, true, false, verbose);
    }

    // Overloaded method 11: With start_signal, stop_signal, and verbose option (No onStop callback)
    public void listen_and_capture_udp(String host, int port, Consumer<List<String>> onDataReceived, Consumer<Exception> onError, String start_signal, String stop_signal, boolean verbose ) {
        listen_and_capture_udp(host, port, onDataReceived, onError, null, start_signal, stop_signal, 30, true, false, verbose);
    }

    // Overloaded method 12: With start_signal, stop_signal, and verbose option (No onError and onStop)
    public void listen_and_capture_udp(String host, int port, Consumer<List<String>> onDataReceived, String start_signal, String stop_signal, boolean verbose ) {
        listen_and_capture_udp(host, port, onDataReceived, null, null, start_signal, stop_signal, 30, true, false, verbose);
    }


    // Method to save the message to the PostgreSQL database
    public void save_message_to_db(String packetData) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // Get a connection to the PostgreSQL database
            conn = PostgreSQLConnection.getConnection();

            // Prepare the SQL statement to insert a new packet record
            String sql = "INSERT INTO udp_captures (captured_message) VALUES (?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, packetData);

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
