package main;

import java.util.function.Consumer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CaptureUDP2 {

    private static final String START_SIGNAL = "START";
    private static final String STOP_SIGNAL = "STOP";
    private static final int TIMEOUT_SECONDS = 30;  // Timeout constant
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Consumer<String> statusUpdateCallable;

    // Method to set the status update function
    public void setStatusUpdateCallable(Consumer<String> statusUpdateCallable) {
        this.statusUpdateCallable = statusUpdateCallable;
    }

    // Method to start listening for UDP packets with callbacks
    public void listen_and_capture_udp(Consumer<String> onDataReceived, Runnable onStop, Consumer<Exception> onError) {
        executorService.submit(() -> {
            try (DatagramSocket socket = new DatagramSocket(5005, InetAddress.getByName("127.0.0.1"))) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                boolean listening = false;
                long startTime = System.currentTimeMillis();

                while (!executorService.isShutdown()) {
                    // Check for timeout
                    if ((System.currentTimeMillis() - startTime) > TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS)) {
                        onError.accept(new Exception("Timeout reached after " + TIMEOUT_SECONDS + " seconds"));
                        updateStatus("Timeout reached");
                        break;
                    }

                    // Receive UDP packet
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());

                    // Start signal
                    if (!listening && message.equals(START_SIGNAL)) {
                        listening = true;
                        updateStatus("Start signal received. Listening for data...");
                    } 
                    // Stop signal
                    else if (listening) {
                        if (message.equals(STOP_SIGNAL)) {
                            updateStatus("Stop signal received. Stopping capture.");
                            onStop.run();
                            break;
                        }
                        // Handle other messages
                        onDataReceived.accept(message);
                    }
                }
            } catch (Exception e) {
                // Handle errors or exceptions
                onError.accept(e);
            }
        });
    }

    // Method to update the status
    private void updateStatus(String message) {
        if (statusUpdateCallable != null) {
            statusUpdateCallable.accept(message);  // Call the status update method
        }
    }
}
