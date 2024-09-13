package main;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;

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
        boolean auto_stop_after_timeout,
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

            try {
                // Create the captures directory if it doesn't exist
                Files.createDirectories(Paths.get(captures_directory_path));

                // Construct the tshark command with echo and sudo
                String tsharkCommand = String.format("echo 'semicolon' | sudo tshark -i lo -f \"host %s and port %d\" -w %s", host, port, outputFilePath);
                ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", tsharkCommand);
                processBuilder.redirectErrorStream(true);  // Combine stdout and stderr

                // Start the tshark process
                tsharkProcess = processBuilder.start();
                BufferedReader pidReader = new BufferedReader(new InputStreamReader(tsharkProcess.getInputStream()));
                String pidLine = pidReader.readLine();
                long pid = tsharkProcess.pid();  // Capture the process ID
                if (verbose) {
                    System.out.println("tshark process started with PID: " + pid);
                }

                boolean listening = false;
                boolean messageStarted = false;
                long startTime = System.currentTimeMillis();

                // Listen for start signal and manage tshark capture process
                while (true) {
                    if ((System.currentTimeMillis() - startTime) > TimeUnit.SECONDS.toMillis(timeout_seconds)) {
                        if (auto_stop_after_timeout && messageStarted) {
                            if (verbose) System.out.println("Auto Closed due to Timeout.");
                            if (onStop != null) onStop.run();
                            // Kill the tshark process
                            String killCommand = String.format("echo 'semicolon' | sudo kill %d", pid);
                            new ProcessBuilder("bash", "-c", killCommand).start();
                            break;
                        } else {
                            if (onError != null) onError.accept(new Exception("Timeout reached after " + timeout_seconds + " seconds"));
                            if (verbose) System.out.println("Error Occurred: Timeout reached.");
                            break;
                        }
                    }

                    if (!listening && start_signal != null) {
                        if (verbose) System.out.println("Start signal detected, beginning capture.");
                        listening = true;
                        if (include_signals_in_message) {
                            results.add("Start Signal: " + start_signal);
                        }
                    }

                    if (stop_signal != null && listening) {
                        if (include_signals_in_message) {
                            results.add("Stop Signal: " + stop_signal);
                        }
                        if (verbose) System.out.println("Stop signal detected. Stopping capture.");

                        // Kill the tshark process
                        String killCommand = String.format("echo 'semicolon' | sudo kill %d", pid);
                        new ProcessBuilder("bash", "-c", killCommand).start();
                        break;
                    }
                }

                // Parse the captured file
                String tsharkReadCommand = String.format("echo 'semicolon' | tshark -r %s -Y \"udp\" -T fields -e data", outputFilePath);
                ProcessBuilder readProcessBuilder = new ProcessBuilder("bash", "-c", tsharkReadCommand);
                Process readProcess = readProcessBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(readProcess.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Convert hex to ASCII
                    StringBuilder output = new StringBuilder();
                    for (int i = 0; i < line.length(); i += 2) {
                        String str = line.substring(i, i + 2);
                        output.append((char) Integer.parseInt(str, 16));
                    }
                    results.add(output.toString());  // Add each packet as a separate result
                }

                if (onDataReceived != null) {
                    onDataReceived.accept(results);  // Pass the results list to onDataReceived
                }

                int readExitCode = readProcess.waitFor();
                if (readExitCode == 0 && verbose) {
                    System.out.println("Captured file parsed successfully.");
                } else if (verbose) {
                    System.err.println("Error occurred while parsing the captured file.");
                }

                if (onStop != null) onStop.run();

            } catch (Exception e) {
                if (onError != null) onError.accept(e);
                if (verbose) System.out.println("Error Occurred: " + e.getMessage());
            } finally {
                if (tsharkProcess != null && tsharkProcess.isAlive()) {
                    try {
                        String killCommand = String.format("echo 'semicolon' | sudo kill %d", tsharkProcess.pid());
                        new ProcessBuilder("bash", "-c", killCommand).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();  // Ensure executor shuts down
            }
        });
    }


    // // Method to read the captured file
    // private void readCapturedFile(String pcapFilePath, Consumer<String> onDataReceived, boolean verbose) {
    //     String tsharkCommand = String.format("tshark -r %s", pcapFilePath);

    //     try {
    //         ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", tsharkCommand);
    //         processBuilder.redirectErrorStream(true);

    //         Process process = processBuilder.start();

    //         BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    //         String line;
    //         StringBuilder messageBuilder = new StringBuilder();

    //         // Read tshark output line by line and accumulate the messages
    //         while ((line = reader.readLine()) != null) {
    //             messageBuilder.append(line).append("\n");
    //         }

    //         // Pass the message data to the callback function
    //         if (onDataReceived != null) {
    //             onDataReceived.accept(messageBuilder.toString());
    //         }

    //         int exitCode = process.waitFor();
    //         if (exitCode == 0) {
    //             if (verbose) System.out.println("Captured file read successfully.");
    //         } else {
    //             if (verbose) System.err.println("Error occurred while reading the captured file.");
    //         }

    //     } catch (IOException | InterruptedException e) {
    //         e.printStackTrace();
    //     }
    // }

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
