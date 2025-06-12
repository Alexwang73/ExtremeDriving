import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Car {
    protected BufferedImage image; // Changed from 'car' to 'image' for consistency
    private int topSpeed;
    private int xCoord;
    private int yCoord;
    private int score;

    // Additional properties for 3D road simulation
    private double roadPosition = 0; // Position along the road (0 = far away, higher = closer)
    private double laneOffset = 0;   // -1 to 1, where 0 is center of road
    private double speed = 1.0;      // Relative speed compared to player

    // Original constructors
    public Car(BufferedImage car, int xCoord, int yCoord, int score) {
        this.image = car;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.score = score;
    }

    // New constructor for NPC cars
    public Car(BufferedImage car, int xCoord, int yCoord, double roadPosition, double laneOffset, double speed) {
        this.image = car;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.score = 0;
        this.roadPosition = roadPosition;
        this.laneOffset = laneOffset;
        this.speed = speed;
    }

    // Original methods
    public int getxCoord() {
        return xCoord;
    }

    public int getyCoord() {
        return yCoord;
    }

    public int getScore() {
        return score;
    }

    public void collectCoin() {
        score++;
    }

    public void setxCoord(int xCoord) {
        this.xCoord = xCoord;
    }

    public void setyCoord(int yCoord) {
        this.yCoord = yCoord;
    }

    public Rectangle carRect() {
        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();
        return new Rectangle(xCoord, yCoord, imageWidth, imageHeight);
    }

    // New getters and setters for 3D road simulation
    public double getRoadPosition() {
        return roadPosition;
    }

    public void setRoadPosition(double roadPosition) {
        this.roadPosition = roadPosition;
    }

    public double getLaneOffset() {
        return laneOffset;
    }

    public void setLaneOffset(double laneOffset) {
        this.laneOffset = laneOffset;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public BufferedImage getImage() {
        return image;
    }
}