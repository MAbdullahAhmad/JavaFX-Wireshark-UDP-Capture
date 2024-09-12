import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class CaptureJREP {

    private static final String START_SIGNAL = "START";
    private static final String STOP_SIGNAL = "STOP";
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void startListening(Consumer<String> onDataReceived, Runnable onStop) {
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
                        System.out.println("Start signal received. Listening for data...");
                    } else if (listening) {
                        if (message.equals(STOP_SIGNAL)) {
                            System.out.println("Stop signal received. Stopping capture.");
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

    public void stopListening() {
        executorService.shutdownNow();
        System.out.println("Listener stopped.");
    }

    public String post_process(String data) {
        return data.trim().replace("\n", "; ");
    }

    public static void main(String[] args) {
        CaptureUDP udpCapture = new CaptureUDP();

        StringBuilder capturedData = new StringBuilder();

        // Start listening for UDP data asynchronously
        udpCapture.startListening(
            message -> capturedData.append(message).append("\n"),
            () -> {
                // Post-process and print data after receiving the stop signal
                String processedData = udpCapture.post_process(capturedData.toString());
                System.out.println("Captured Data: " + processedData);
            }
        );

        // Simulate stopping the listener after some time (e.g., 10 seconds for demonstration)
        try {
            Thread.sleep(10000);  // Allow the listener to run for 10 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Stop the listener manually (if stop signal is not received)
        udpCapture.stopListening();
    }
}
