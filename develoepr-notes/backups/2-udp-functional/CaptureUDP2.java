package main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.function.Consumer;
import java.util.concurrent.TimeUnit;

public class CaptureUDP2 {

    // Method to start listening for UDP packets with callbacks and configuration options
    public void listen_and_capture_udp(int timeout_seconds, String host, int port, boolean auto_stop_after_timeout, boolean verbose,
                                       String start_signal, String stop_signal, boolean include_signals_in_message,
                                       Consumer<String> onDataReceived, Runnable onStop, Consumer<Exception> onError) {
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
                        onStop.run();
                        break;
                    } else {
                        onError.accept(new Exception("Timeout reached after " + timeout_seconds + " seconds"));
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
                    if (include_signals_in_message) onDataReceived.accept("Start Signal: " + message);
                    if (verbose) System.out.println("Start Signal Received. Listening for data...");
                    continue;
                }

                // Message started
                if (!message.isEmpty()) {
                    messageStarted = true;
                    onDataReceived.accept("Message received: " + message);
                    if (verbose) System.out.println("Message Started.");
                } else {
                    if (verbose) System.out.println("Empty Message Received.");
                }

                // Stop signal detection
                if (stop_signal != null && message.equals(stop_signal)) {
                    if (include_signals_in_message) onDataReceived.accept("Stop Signal: " + message);
                    if (verbose) System.out.println("Stop Signal Received.");
                    onStop.run();
                    break;
                }

                // Detect server stop (server closes connection)
                if (socket.isClosed()) {
                    if (verbose) System.out.println("Connection closed.");
                    onStop.run();
                    break;
                }
            }
        } catch (Exception e) {
            // Handle errors or exceptions
            onError.accept(e);
            if (verbose) System.out.println("Error Occurred: " + e.getMessage());
        }
    }
}
