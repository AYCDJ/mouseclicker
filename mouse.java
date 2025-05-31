import java.awt.MouseInfo;
import java.awt.Point;

public class mousepointer {
    public static void main(String[] args) {
        try {
            while (true) {
                // get the current mouse position
                Point mousePosition = MouseInfo.getPointerInfo().getLocation();

                // print the x and y coordinates
                System.out.println("Mouse X: " + mousePosition.x + ", Y: " + mousePosition.y);
                
                // add delay to prevent excessive CPU usage
                Thread.sleep(100);  // updates every 100 milliseconds
            }
        } catch (InterruptedException e) {
            System.out.println("Program interrupted!");
        }
    }
}
