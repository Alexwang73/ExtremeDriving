// RoadFrame.java
import javax.swing.*;
import java.awt.*;

public class RoadFrame extends JFrame {
    private RoadPanel roadPanel;

    public RoadFrame() {
        setTitle("Pseudo 3D Road");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        roadPanel = new RoadPanel();
        add(roadPanel);

        setVisible(true);
    }
}