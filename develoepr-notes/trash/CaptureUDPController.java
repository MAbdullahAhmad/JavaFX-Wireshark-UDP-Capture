package main;

public class CaptureUDPController {

    private CaptureUDP packetCapture;

    public CaptureController() {
        this.packetCapture = new CaptureUDP();
    }

    public void displayStatus(String message) {
        System.out.println(message);
    }

    public void startTestCapture() {
        displayStatus("Starting packet capture...");
        // Test capturing packet
        packetCapture.testSavePacket("Start", "Test Packet Data");
        displayStatus("Packet capture completed.");
    }

    public static void main(String[] args) {
        CaptureController controller = new CaptureController();
        controller.startTestCapture();
    }
}
