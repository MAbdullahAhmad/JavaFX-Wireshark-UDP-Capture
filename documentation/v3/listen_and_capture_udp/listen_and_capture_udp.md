### **Documentation: `listen_and_capture_udp`**

---

### Usage

#### Imports Required

```java
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.function.Consumer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// Adjust this accordingly
import lib.PostgreSQLConnection;
```

---

### Functions

`listen_and_capture_udp` and overloaded methods:

```java
/**
 * Listen for UDP packets and process messages based on start/stop signals, timeouts, and other configuration options.
 * Supports asynchronous processing and optional saving of data to PostgreSQL.
 * The function also supports cross-platform execution (Linux and Windows).
 *
 * @param host                    The IP address to listen on (e.g., "127.0.0.1").
 * @param port                    The port to listen on.
 * @param onDataReceived          A callback to handle the accumulated list of messages.
 * @param onError                 A callback to handle errors.
 * @param onStop                  A callback to handle stop events.
 * @param start_signal            The signal string indicating the start of the message.
 * @param stop_signal             The signal string indicating the stop of the message.
 * @param timeout_seconds         The number of seconds before the listener times out.
 * @param auto_stop_after_timeout Automatically stops after timeout if set to true.
 * @param include_signals_in_message Includes the start/stop signals in the captured message if set to true.
 * @param verbose                 Enables detailed logging if set to true.
 */
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
    boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

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
            boolean messageStarted = false;
            long startTime = System.currentTimeMillis();

            // Check for start signal via UDP
            while (true) {
                // Check for timeout
                if ((System.currentTimeMillis() - startTime) > TimeUnit.SECONDS.toMillis(timeout_seconds)) {
                    if (auto_stop_after_timeout && messageStarted) {
                        if (verbose) System.out.println("Auto Closed due to Timeout.");
                        if (onStop != null) onStop.run();
                    } else {
                        if (onError != null) onError.accept(new Exception("Timeout reached after " + timeout_seconds + " seconds"));
                        if (verbose) System.out.println("Error Occurred: Timeout reached.");
                    }
                    socket.close();
                    break;  // Ensure to break after timeout or error
                }

                // Receive UDP packet to check for signals
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());

                // Start signal detection
                if (!listening && start_signal != null && message.equals(start_signal)) {
                    listening = true;
                    if (verbose) System.out.println("Start Signal Received. Starting tshark capture...");

                    // Start tshark capture (cross-platform support for Linux and Windows)
                    String tsharkCommand = isWindows
                            ? String.format("tshark -i 1 -f \"host %s and port %d\" -w %s", host, port, outputFilePath)
                            : String.format("echo 'semicolon' | sudo tshark -i lo -f \"host %s and port %d\" -w %s", host, port, outputFilePath);
                    ProcessBuilder processBuilder = isWindows
                            ? new ProcessBuilder("powershell.exe", "/c", tsharkCommand)
                            : new ProcessBuilder("bash", "-c", tsharkCommand);

                    processBuilder.redirectErrorStream(true);  // Combine stdout and stderr

                    tsharkProcess = processBuilder.start();
                    long pid = tsharkProcess.pid();  // Capture the process ID
                    if (verbose) System.out.println("tshark process started with PID: " + pid);
                    continue;  // Continue listening for data
                }

                // Message handling
                if (!message.isEmpty()) {
                    messageStarted = true;
                    if (verbose) System.out.println("Message Started.");
                }

                // Stop signal detection
                if (stop_signal != null && message.equals(stop_signal)) {
                    if (verbose) System.out.println("Stop Signal Received. Stopping tshark...");

                    // Kill the tshark process
                    if (tsharkProcess != null && tsharkProcess.isAlive()) {
                        String killCommand = isWindows
                                ? String.format("taskkill /PID %d /F", tsharkProcess.pid())
                                : String.format("echo 'semicolon' | sudo kill %d", tsharkProcess.pid());
                        new ProcessBuilder(isWindows ? "cmd.exe" : "bash", "-c", killCommand).start();
                    }
                    break;  // Ensure to break after receiving the stop signal
                }
            }

            // Continuously check if the process is still alive
            while (tsharkProcess.isAlive()) {
                if (verbose) {
                    System.out.println("Waiting for tshark process to terminate...");
                }
                try {
                    Thread.sleep(10);  // Check every 500 milliseconds (adjust as needed)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (verbose) {
                        System.out.println("Sleep interrupted: " + e.getMessage());
                    }
                }
            }

            // Wait for file creation
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (verbose) {
                    System.out.println("Sleep interrupted: " + e.getMessage());
                }
            }

            // Command to parse hex data and convert it to ASCII using echo and xxd directly from tshark output
            String tsharkReadCommand = isWindows
                    ? String.format("tshark -r %s -Y \"udp\" -T fields -e data", outputFilePath)
                    : String.format("echo 'semicolon' | sudo tshark -r %s -Y \"udp\" -T fields -e data", outputFilePath);

            ProcessBuilder readProcessBuilder = new ProcessBuilder(isWindows ? "powershell.exe" : "bash", "-c", tsharkReadCommand);

            // Start the tshark process
            Process readProcess = readProcessBuilder.start();

            // Read the output from tshark directly
            BufferedReader reader = new BufferedReader(new InputStreamReader(readProcess.getInputStream()));
            String line;
            boolean packetReceived = false;  // Track if any packet data was received

            // Read each line from the tshark output
            while ((line = reader.readLine()) != null) {
                packetReceived = true;  // Set to true if we actually get data

                // Convert hex to ASCII
                StringBuilder output = new StringBuilder();
                for (int i = 0; i < line.length(); i += 2) {
                    String str = line.substring(i, i + 2);
                    output.append((char) Integer.parseInt(str, 16));
                }

                // Skip 'STOP' signal from tshark output
                if (!output.toString().equals(stop_signal)) {
                    results.add(output.toString());  // Add each packet as a separate result
                }
            }

            // Check if no packets were received
            if (!packetReceived && verbose) {
                System.out.println("No packets were captured.");
            }

            if (include_signals_in_message) {
                if (start_signal != null) {
                    results.add(0, start_signal);  // Prepend start signal
                }
                if (stop_signal != null) {
                    results.add(stop_signal);  // Append stop signal
                }
            }

            if (onDataReceived != null) onDataReceived.accept(results);  // Pass the results list to onDataReceived

            // Check the exit code of the process
            int readExitCode = readProcess.waitFor();
            if (readExitCode == 0) {
                if (verbose) System.out.println("Hex to ASCII conversion completed successfully.");
            } else {
                if (verbose) System.err.println("Error occurred while parsing the captured file.");
            }

            if (onStop != null) onStop.run();

        } catch (Exception e) {
            if (onError != null) onError.accept(e);
            if (verbose) System.out.println("Error Occurred: " + e.getMessage());
        } finally {
            if (tsharkProcess != null && tsharkProcess.isAlive()) {
                try {
                    String killCommand = isWindows
                            ? String.format("taskkill /PID %d /F", tsharkProcess.pid())
                            : String.format("echo 'semicolon'

 | sudo kill %d", tsharkProcess.pid());

                    new ProcessBuilder(isWindows ? "cmd.exe" : "bash", "-c", killCommand).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();  // Ensure executor shuts down
        }
    });
}
```

---

### **Overloaded Methods**

Overloaded `listen_and_capture_udp` methods are included to provide flexibility for different use cases.

---

### `PostgreSQLConnection` Class:

```java
package lib;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQLConnection {

    private static final String URL      = "jdbc:postgresql://<host>:5432/<database>";
    private static final String USER     = "<user>";
    private static final String PASSWORD = "<password>";

    // Function to establish a connection to PostgreSQL
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

---

### **Function Overview**

The `listen_and_capture_udp` function listens for UDP packets on a specified IP and port. It captures messages, processes them based on configurable start and stop signals, and handles timeouts and errors. The function supports both Windows and Linux environments.

### **Parameters**

- **`host`**: The IP address to listen on (e.g., "127.0.0.1").
- **`port`**: The port number to listen on.
- **`onDataReceived`**: A callback function that processes the accumulated list of messages once the stop signal is received or timeout occurs.
- **`onError`**: A callback function to handle errors during message reception.
- **`onStop`**: A callback function to handle stop events when the message capture completes.
- **`start_signal`**: A string indicating the start of the message.
- **`stop_signal`**: A string indicating the end of the message.
- **`timeout_seconds`**: The duration (in seconds) after which the listener times out if no stop signal is received.
- **`auto_stop_after_timeout`**: Automatically stops message capture when the timeout is reached if set to `true`.
- **`include_signals_in_message`**: If `true`, the start and stop signals are included in the final message.
- **`verbose`**: Enables detailed logging of events such as message reception, start and stop signals, and errors.

---

### Example Usage

```java
public class Main {
    public static void main(String[] args) {

        // Define what happens when data is received, errors occur, or capture stops
        listen_and_capture_udp(
            "127.0.0.1", 5555, 
            message -> save_message_to_db(message),  // Save to DB
            error -> System.err.println("Error: " + error.getMessage()),
            () -> System.out.println("Capture stopped."),
            "START",
            "STOP",
            30,
            true,
            true,
            true
        );
    }
}
```

---

### **Example Usages: `listen_and_capture_udp`**

```java
public class Main {
    public static void main(String[] args) {

        // Example 1: Basic usage with start/stop signals and verbose logging
        listen_and_capture_udp(
            "127.0.0.1", 5555,
            results -> {
                for (String packet : results) {
                    save_message_to_db(message);                          // Save to DB
                }
            },
            error -> System.err.println("Error: " + error.getMessage()),  // Handle errors
            () -> System.out.println("UDP capture stopped."),             // Handle stop event
            "START", "STOP", 30, true, true, true                         // Include signals, enable verbose logging
        );

        // Example 2: Without start/stop signals, only capture for 60 seconds
        listen_and_capture_udp(
            "127.0.0.1", 5555,
            results -> {
                for (String line : results) {
                    System.out.println("Line: " + line);
                }
            },
            error -> System.err.println("Error: " + error.getMessage()),    // Handle errors
            60,     // Timeout in 60 seconds
            false,  // Do not include signals in the message
            false   // Disable verbose logging
        );

        // Example 3: Minimal usage with defaults, only capturing data
        listen_and_capture_udp(
            "127.0.0.1", 5555,
            results -> {
                for (String line : results) {
                    System.out.println("Line: " + line);
                }
            },
        );
    }
}
```

---

These examples illustrate different ways to use the `listen_and_capture_udp` function based on your application's needs. You can customize the behavior by passing appropriate parameters or using one of the overloads with default values.
