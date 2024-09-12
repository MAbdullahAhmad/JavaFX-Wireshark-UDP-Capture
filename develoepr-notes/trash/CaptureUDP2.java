package main;

import java.util.function.Consumer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.concurrent.TimeUnit;


public class CaptureUDP2 {

    private Consumer<String> statusUpdateCallable;

    // Method to set the status update function
    public void setStatusUpdateCallable(Consumer<String> statusUpdateCallable) {
        this.statusUpdateCallable = statusUpdateCallable;
    }

    // Method to start listening for UDP packets
    public void listen_and_capture_udp() {
        executorService.submit(() -> {
            try (DatagramSocket socket = new DatagramSocket(5005, InetAddress.getByName("127.0.0.1"))) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                boolean listening = false;

                while (!executorService.isShutdown()) {
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());

                    if (!listening && message.equals(START_SIGNAL)) {
                        listening = true;
                        updateStatus("Start signal received. Listening for data...");
                    } else if (listening) {
                        if (message.equals(STOP_SIGNAL)) {
                            updateStatus("Stop signal received. Stopping capture.");
                            onStop.run();
                            break;
                        }
                        onDataReceived.accept(message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
