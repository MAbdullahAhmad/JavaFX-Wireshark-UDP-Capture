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

listen_and_capture_udp and overload methods:
```java
/**
 * Listen for UDP packets and process messages based on start/stop signals, timeouts, and other configuration options.
 * Supports asynchronous processing and optional saving of data to PostgreSQL.
 *
 * @param host                    The IP address to listen on (e.g., "127.0.0.1").
 * @param port                    The port to listen on.
 * @param onDataReceived          A callback to handle the accumulated message.
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
                    if (!message.equals(stop_signal)) {
                        messageBuilder.append(message).append("\n");
                    }
                    if (verbose) System.out.println("Message Started.");
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
```

save_message_to_db:
```java

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
```

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

### Function Overview

The `listen_and_capture_udp` function listens for UDP packets on a specified IP and port. It captures messages, processes them based on configurable start and stop signals, and handles timeouts and errors. The function is asynchronous and can run in the background, allowing other code to execute concurrently.

### Parameters:

- **`host`**: The IP address to listen on (e.g., "127.0.0.1").
- **`port`**: The port number to listen on.
- **`onDataReceived`**: A callback function that processes the accumulated message once the stop signal is received or timeout occurs.
- **`onError`**: A callback function to handle errors during message reception.
- **`onStop`**: A callback function to handle stop events when the message capture completes.
- **`start_signal`**: A string indicating the start of the message. This signal triggers the listener to start accumulating the message.
- **`stop_signal`**: A string indicating the end of the message. Once this signal is detected, the listener stops accumulating and returns the complete message.
- **`timeout_seconds`**: The duration (in seconds) after which the listener times out if no stop signal is received.
- **`auto_stop_after_timeout`**: Automatically stops message capture when the timeout is reached if set to `true`.
- **`include_signals_in_message`**: If `true`, the start and stop signals are included in the final message. If `false`, only the message between the signals is returned.
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
            "START",
            "STOP"
        );
    }
}
```

---

### Functionality Highlights:

1. **Asynchronous**: The method is designed to run asynchronously, allowing other parts of your program to execute while UDP messages are being captured.
2. **Customizable Signals**: You can specify `start_signal` and `stop_signal` to define how message capture starts and ends.
3. **Timeout Handling**: Automatically stops after a specified timeout if the stop signal is not detected, with optional auto-stop behavior.
4. **Database Integration**: The `onDataReceived` callback can be used to save the captured message to a database, using the `save_message_to_db` method in `CaptureUDP2`.

### **Example Usages: `listen_and_capture_udp`**

Here are a few common examples showcasing different ways to use the `listen_and_capture_udp` method with various overloads:

```java
public class Main {
    public static void main(String[] args) {

        // Example 1: Basic usage with start/stop signals and verbose logging
        listen_and_capture_udp(
            "127.0.0.1", 5555,
            message -> save_message_to_db(message),                       // Save to DB
            error -> System.err.println("Error: " + error.getMessage()),  // Handle errors
            () -> System.out.println("UDP capture stopped."),             // Handle stop event
            "START", "STOP", 30, true, true, true                         // Include signals, enable verbose logging
        );

        // Example 2: Without start/stop signals, only capture for 60 seconds
        listen_and_capture_udp(
            "127.0.0.1", 5555,
            message -> System.out.println("Message received: " + message),  // Print message
            error -> System.err.println("Error: " + error.getMessage()),    // Handle errors
            60,     // Timeout in 60 seconds
            false,  // Do not include signals in the message
            false   // Disable verbose logging
        );

        // Example 3: Minimal usage with defaults, only capturing data
        // This version captures data without custom handling of errors or stop events.
        listen_and_capture_udp(
            "127.0.0.1", 5555,
            message -> System.out.println("Message received: " + message)  // Print message
        );
    }
}
```

### **Explanation of the Examples**:
1. **Example 1**: This example listens for UDP messages with defined start/stop signals (`START`, `STOP`), saves the captured data to the PostgreSQL database, and logs each event to the console. The listener will auto-stop after 30 seconds if no stop signal is received.
   
2. **Example 2**: This example listens for UDP packets for 60 seconds without requiring specific start/stop signals. After 60 seconds, the listener will stop automatically.

3. **Example 3**: This is a minimal usage example where only the host and port are specified. It captures and prints any incoming UDP messages without additional error handling or stop callbacks.

---

These examples illustrate different ways to use the `listen_and_capture_udp` function based on your application's needs. You can customize the behavior by passing appropriate parameters or using one of the overloads with default values.
---