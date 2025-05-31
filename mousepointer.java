import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.Scanner;

public class mousepointer {
    private Robot robot;
    private Color targetColor;
    private volatile boolean running = false;
    
    // screen check 
    private static final int CHECK_X = 263;
    private static final int CHECK_Y = 484;
    
    // timing 
    private int clickDelay = 100;
    private int scanDelay = 50;
    
    // color matching
    private int colorTolerance = 30;
    
    private long totalScans = 0;
    private long totalDetections = 0;
    
    public mousepointer(Color targetColor) throws AWTException {
        this.robot = new Robot();
        this.targetColor = targetColor;
        robot.setAutoDelay(0);
        robot.setAutoWaitForIdle(false);
    }
    
    public void startMonitoring() {
        running = true;
        System.out.println("=== Clicker Started ===");
        System.out.println("Target color: " + colorToString(targetColor));
        System.out.println("Color tolerance: ±" + colorTolerance);
        System.out.println("Checking position: (" + CHECK_X + ", " + CHECK_Y + ")");
        System.out.println("Type 'stop' and press Enter to stop\n");
        
        // input listener
        Thread inputThread = new Thread(this::listenForStop);
        inputThread.setDaemon(true);
        inputThread.start();
        
        long startTime = System.currentTimeMillis();
        
        while (running) {
            try {
                totalScans++;
                
                if (checkColorAtPosition()) {
                    performClick();
                    totalDetections++;
                    Thread.sleep(clickDelay);
                }
                
                // print stats every 100 scans
                if (totalScans % 100 == 0) {
                    printStats(startTime);
                }
                
                Thread.sleep(scanDelay);
                
            } catch (InterruptedException e) {
                System.out.println("Monitoring interrupted");
                break;
            }
        }
        
        printFinalStats(startTime);
    }
    
    private void listenForStop() {
        Scanner scanner = new Scanner(System.in);
        while (running) {
            try {
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("stop") || input.equals("quit") || input.equals("exit")) {
                    System.out.println("Stop command received...");
                    running = false;
                    break;
                } else if (input.equals("color")) {
                    showColorAtPosition();
                }
            } catch (Exception e) {
                // ignore input errors
            }
        }
        scanner.close();
    }
    
    private boolean checkColorAtPosition() {
        try {
            // capture pixel at position
            BufferedImage pixel = robot.createScreenCapture(new Rectangle(CHECK_X, CHECK_Y, 1, 1));
            int rgb = pixel.getRGB(0, 0);
            
            // rgb components
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            
            if (colorMatches(r, g, b)) {
                System.out.println("✓ Color FOUND at (" + CHECK_X + ", " + CHECK_Y + ") - RGB(" + r + ", " + g + ", " + b + ")");
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("Error checking color: " + e.getMessage());
            return false;
        }
    }
    
    private boolean colorMatches(int r, int g, int b) {
        return Math.abs(r - targetColor.getRed()) <= colorTolerance &&
               Math.abs(g - targetColor.getGreen()) <= colorTolerance &&
               Math.abs(b - targetColor.getBlue()) <= colorTolerance;
    }
    
    private void showColorAtPosition() {
        try {
            BufferedImage pixel = robot.createScreenCapture(new Rectangle(CHECK_X, CHECK_Y, 1, 1));
            Color pixelColor = new Color(pixel.getRGB(0, 0));
            
            System.out.println("\n🎨 Color at position (" + CHECK_X + ", " + CHECK_Y + "):");
            System.out.println("   Actual: " + colorToString(pixelColor));
            System.out.println("   Target: " + colorToString(targetColor));
            System.out.println("   Match: " + (colorMatches(pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue()) ? "YES" : "NO"));
            System.out.println("   Difference: R=" + Math.abs(pixelColor.getRed() - targetColor.getRed()) + 
                             ", G=" + Math.abs(pixelColor.getGreen() - targetColor.getGreen()) + 
                             ", B=" + Math.abs(pixelColor.getBlue() - targetColor.getBlue()) + "\n");
        } catch (Exception e) {
            System.out.println("Error getting color: " + e.getMessage());
        }
    }
    
    private void performClick() {
        System.out.println("🖱️  CLICKING at: (" + CHECK_X + ", " + CHECK_Y + ")");
        
        try {
            robot.mouseMove(CHECK_X, CHECK_Y);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            System.out.println("✅ Click performed");
        } catch (Exception e) {
            System.err.println("❌ Click failed: " + e.getMessage());
        }
    }
    
    private void printStats(long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        double scansPerSecond = (totalScans * 1000.0) / elapsed;
        double successRate = (totalDetections * 100.0) / totalScans;
        
        System.out.printf("📊 Stats: %d scans, %d detections (%.1f%%), %.1f scans/sec\n",
                         totalScans, totalDetections, successRate, scansPerSecond);
    }
    
    private void printFinalStats(long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        double scansPerSecond = (totalScans * 1000.0) / elapsed;
        double successRate = (totalDetections * 100.0) / totalScans;
        
        System.out.println("\n=== Final Stats ===");
        System.out.println("Total runtime: " + (elapsed / 1000.0) + " seconds");
        System.out.println("Total scans: " + totalScans);
        System.out.println("Total detections: " + totalDetections);
        System.out.println("Success rate: " + String.format("%.2f%%", successRate));
        System.out.println("Average scans per second: " + String.format("%.1f", scansPerSecond));
    }
    
    public void stop() {
        running = false;
    }
    
    // config methods
    public void setClickDelay(int delay) { this.clickDelay = delay; }
    public void setScanDelay(int delay) { this.scanDelay = delay; }
    public void setColorTolerance(int tolerance) { this.colorTolerance = tolerance; }
    
    private String colorToString(Color c) {
        return String.format("RGB(%d, %d, %d)", c.getRed(), c.getGreen(), c.getBlue());
    }
    
    public static void main(String[] args) {
        try {
            // target color - light green
            Color targetColor = new Color(75, 219, 106);
            
            mousepointer clicker = new mousepointer(targetColor);
            
            clicker.setClickDelay(200);        // 200ms between clicks
            clicker.setScanDelay(25);          // Fast scanning every 25ms
            clicker.setColorTolerance(20);     // ±20 RGB tolerance
            
            System.out.println("💡 Type 'color' to see what color is currently at position (" + CHECK_X + ", " + CHECK_Y + ")");
            
            clicker.startMonitoring();
            
        } catch (AWTException e) {
            System.err.println("Failed to create Robot: " + e.getMessage());
        }
    }
}
