package main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

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

    // Core asynchronous method to listen for UDP packets with callbacks
    public void listen_and_capture_udp(
        String host,
        int port,
        Consumer<String> onDataReceived,
        Consumer<Exception> onError,
        Runnable onStop,
        String start_signal,
        String stop_signal,
        int timeout_seconds,
        boolean auto_stop_after_timeout,
        boolean include_signals_in_message,
        boolean verbose
    ) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            StringBuilder messageBuilder = new StringBuilder(); // To accumulate messages

            try (DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName(host))) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                boolean listening = false;
                boolean messageStarted = false;
                long startTime = System.currentTimeMillis();

                while (true) {
                    // Check for timeout
                    if ((System.currentTimeMillis() - startTime) > TimeUnit.SECONDS.toMillis(timeout_seconds)) {
                        if (auto_stop_after_timeout && messageStarted) {
                            if (verbose) System.out.println("Auto Closed because of Timeout.");
                            if (onStop != null) onStop.run();
                            onDataReceived.accept(messageBuilder.toString()); // Return accumulated message
                            break;
                        } else {
                            if (onError != null) onError.accept(new Exception("Timeout reached after " + timeout_seconds + " seconds"));
                            if (verbose) System.out.println("Error Occurred: Timeout reached.");
                            break;
                        }
                    }

                    // Receive UDP packet
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());

                    // Start signal detection
                    if (!listening && start_signal != null && message.equals(start_signal)) {
                        listening = true;
                        if (include_signals_in_message && onDataReceived != null) {
                            messageBuilder.append("Start Signal: ").append(message).append("\n");
                        }
                        if (verbose) System.out.println("Start Signal Received. Listening for data...");
                        continue;
                    }

                    // Message started
                    if (!message.isEmpty()) {
                        messageStarted = true;
                        // Append message to the builder, exclude stop signal if not included
                        if (!message.equals(stop_signal)) {
                            messageBuilder.append(message).append("\n");
                        }
                        if (verbose) System.out.println("Message Started.");
                    } else {
                        if (verbose) System.out.println("Empty Message Received.");
                    }

                    // Stop signal detection
                    if (stop_signal != null && message.equals(stop_signal)) {
                        if (include_signals_in_message && onDataReceived != null) {
                            messageBuilder.append("Stop Signal: ").append(message).append("\n");
                        }
                        if (verbose) System.out.println("Stop Signal Received.");
                        onDataReceived.accept(messageBuilder.toString()); // Return accumulated message
                        if (onStop != null) onStop.run();
                        socket.close();  // Close socket immediately after stop signal
                        break;  // Ensure to break after receiving the stop signal
                    }
                }
            } catch (Exception e) {
                // Handle errors or exceptions
                if (onError != null) onError.accept(e);
                if (verbose) System.out.println("Error Occurred: " + e.getMessage());
            } finally {
                executorService.shutdown();  // Ensure executor shuts down
            }
        });
    }

    // Overloaded method 1: auto_stop_after_timeout default to true
    public void listen_and_capture_udp(String host, int port, Consumer<String> onDataReceived, Consumer<Exception> onError, Runnable onStop, String start_signal, String stop_signal, int timeout_seconds, boolean include_signals_in_message, boolean verbose ) {
        listen_and_capture_udp(host, port, onDataReceived, onError, onStop, start_signal, stop_signal, timeout_seconds, true, include_signals_in_message, verbose);
    }

    // Overloaded method 2: with start_signal, stop_signal defaults to null
    public void listen_and_capture_udp(String host, int port, Consumer<String> onDataReceived, Consumer<Exception> onError, Runnable onStop, int timeout_seconds, boolean include_signals_in_message, boolean verbose ) {
        listen_and_capture_udp(host, port, onDataReceived, onError, onStop, null, null, timeout_seconds, true, include_signals_in_message, verbose);
    }

    // Overloaded method 3: minimal params with default values
    public void listen_and_capture_udp(String host, int port, Consumer<String> onDataReceived, Consumer<Exception> onError, Runnable onStop) {
        listen_and_capture_udp(host, port, onDataReceived, onError, onStop, null, null, 30, true, false, false);
    }

    // Overloaded method 4: no onStop callback
    public void listen_and_capture_udp(String host, int port, Consumer<String> onDataReceived, Consumer<Exception> onError) {
        listen_and_capture_udp(host, port, onDataReceived, onError, null, null, null, 30, true, false, false);
    }

    // Overloaded method 5: no onError and onStop
    public void listen_and_capture_udp(String host, int port, Consumer<String> onDataReceived) {
        listen_and_capture_udp(host, port, onDataReceived, null, null, null, null, 30, true, false, false);
    }

    // Overloaded method 6: no onDataReceived, onError, onStop
    public void listen_and_capture_udp(String host, int port) {
        listen_and_capture_udp(host, port, null, null, null, null, null, 30, true, false, false);
    }

    // Overloaded method 7: With start_signal and stop_signal (All Callbacks)
    public void listen_and_capture_udp(String host, int port, Consumer<String> onDataReceived, Consumer<Exception> onError, Runnable onStop, String start_signal, String stop_signal ) {
        listen_and_capture_udp(host, port, onDataReceived, onError, onStop, start_signal, stop_signal, 30, true, false, false);
    }

    // Overloaded method 8: With start_signal and stop_signal (No onStop Callback)
    public void listen_and_capture_udp(String host, int port, Consumer<String> onDataReceived, Consumer<Exception> onError, String start_signal, String stop_signal ) {
        listen_and_capture_udp(host, port, onDataReceived, onError, null, start_signal, stop_signal, 30, true, false, false);
    }

    // Overloaded method 9: With start_signal and stop_signal (No onError or onStop Callback)
    public void listen_and_capture_udp(String host, int port, Consumer<String> onDataReceived, String start_signal, String stop_signal ) {
        listen_and_capture_udp(host, port, onDataReceived, null, null, start_signal, stop_signal, 30, true, false, false);
    }

    // Overloaded method 10: With start_signal, stop_signal, and verbose option (All callbacks)
    public void listen_and_capture_udp(String host, int port, Consumer<String> onDataReceived, Consumer<Exception> onError, Runnable onStop, String start_signal, String stop_signal, boolean verbose ) {
        listen_and_capture_udp(host, port, onDataReceived, onError, onStop, start_signal, stop_signal, 30, true, false, verbose);
    }

    // Overloaded method 11: With start_signal, stop_signal, and verbose option (No onStop callback)
    public void listen_and_capture_udp(String host, int port, Consumer<String> onDataReceived, Consumer<Exception> onError, String start_signal, String stop_signal, boolean verbose ) {
        listen_and_capture_udp(host, port, onDataReceived, onError, null, start_signal, stop_signal, 30, true, false, verbose);
    }

    // Overloaded method 12: With start_signal, stop_signal, and verbose option (No onError and onStop)
    public void listen_and_capture_udp(String host, int port, Consumer<String> onDataReceived, String start_signal, String stop_signal, boolean verbose ) {
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
