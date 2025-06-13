import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RoadPanel extends JPanel implements ActionListener, KeyListener {
    private static final int NUM_SEGMENTS = 25;
    private static final double ROAD_WIDTH = 600;
    private static final double MIN_CAR_DISTANCE = 3.0;
    private static final double LANE_WIDTH = 0.6;

    // Collision detection constants
    private static final int MAX_COLLISIONS = 20;
    private static final double COLLISION_DISTANCE = 2.0; // Distance threshold for collision
    private static final double COLLISION_LANE_WIDTH = 0.4; // Lane width for collision detection

    // Game state variables
    private int collisionCount = 0;
    private int totalScore = 0;
    private boolean gameOver = false;
    private boolean showCollisionWarning = false;
    private int warningTimer = 0;
    private List<Car> recentlyCollidedCars = new ArrayList<>(); // To prevent multiple collisions with same car

    // Colors
    private final Color GRASS_COLOR = new Color(16, 200, 16);
    private final Color ROAD_DARK = new Color(105, 105, 105);
    private final Color ROAD_LIGHT = new Color(169, 169, 169);
    private final Color LINE_WHITE = Color.WHITE;
    private final Color SKY_COLOR = new Color(135, 206, 235);
    private final Color RUMBLE_WHITE = Color.WHITE;
    private final Color RUMBLE_RED = Color.RED;

    private BufferedImage background1;
    private BufferedImage background2;
    private BufferedImage car;
    private BufferedImage npc1img;
    private BufferedImage npc2img;

    private List<Car> npcCars;
    private Random random;
    private Car playerCar;

    private double position = 0.0;
    private double speed = 0.3;
    private static final double MIN_SPEED = 0.05;
    private static final double MAX_SPEED = 1.0;
    private static final double SPEED_INCREMENT = 0.003;
    private static final double BRAKE_INCREMENT = 0.004;
    private static final double NATURAL_DECELERATION = 0.001;

    private static final double CAR_MOVE_SPEED = 8.0;
    private static final double CAR_MIN_X = 150;
    private static final double CAR_MAX_X = 430;

    private boolean wPressed = false;
    private boolean sPressed = false;
    private boolean aPressed = false;
    private boolean dPressed = false;

    public RoadPanel() {
        Timer timer = new Timer(16, this);
        timer.start();

        npcCars = new ArrayList<>();
        random = new Random();

        try {
            background1 = ImageIO.read(new File("src/skyline.png"));
            background2 = ImageIO.read(new File("src/desert.png"));
            car = ImageIO.read(new File("src/car.png"));
            npc1img = ImageIO.read(new File("src/npc.png"));
            npc2img = ImageIO.read(new File("src/npc2.png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        playerCar = new Car(car, 290, 465, 0);
        spawnInitialNPCCars();

        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
    }

    private void spawnInitialNPCCars() {
        for (int i = 0; i < 2; i++) {
            BufferedImage npcImage;
            if (random.nextBoolean()) {
                npcImage = npc1img;
            } else {
                npcImage = npc2img;
            }

            double roadPos = 20 + i * 8;
            double lane = (random.nextDouble() - 0.5) * 1.2;
            double npcSpeed = 0.7 + random.nextDouble() * 0.3;

            Car npcCar = new Car(npcImage, 0, 0, roadPos, lane, npcSpeed);
            npcCars.add(npcCar);
        }
    }

    private boolean wouldCollide(double roadPos, double laneOffset) {
        for (Car existingCar : npcCars) {
            double roadDistance = Math.abs(existingCar.getRoadPosition() - roadPos);
            if (roadDistance < MIN_CAR_DISTANCE) {
                double laneDistance = Math.abs(existingCar.getLaneOffset() - laneOffset);
                if (laneDistance < LANE_WIDTH) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean findSafeSpawnPosition(double[] outPosition) {
        int maxAttempts = 10;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            double roadPos = 25 + random.nextDouble() * 10;
            double lane = (random.nextDouble() - 0.5) * 1.5;

            if (!wouldCollide(roadPos, lane)) {
                outPosition[0] = roadPos;
                outPosition[1] = lane;
                return true;
            }
        }
        return false;
    }

    // Convert player car screen position to road coordinates for collision detection
    private double getPlayerLaneOffset() {
        // Convert player's screen X position to lane offset
        int roadCenterX = getWidth() / 2;
        int roadWidth = (int)(ROAD_WIDTH * 1.0); // At player's depth (near road)
        return (double)(playerCar.getxCoord() + car.getWidth()/2 - roadCenterX) / (roadWidth * 0.3);
    }

    private void checkPlayerCollisions() {
        if (gameOver) return;

        double playerLaneOffset = getPlayerLaneOffset();
        double playerRoadPosition = 0; // Player is always at position 0

        // Clean up recently collided cars list
        recentlyCollidedCars.removeIf(car -> car.getRoadPosition() < -5 || car.getRoadPosition() > 5);

        for (Car npc : npcCars) {
            // Skip if we recently collided with this car
            if (recentlyCollidedCars.contains(npc)) continue;

            double roadDistance = Math.abs(npc.getRoadPosition() - playerRoadPosition);
            double laneDistance = Math.abs(npc.getLaneOffset() - playerLaneOffset);

            // Check if collision occurred
            if (roadDistance < COLLISION_DISTANCE && laneDistance < COLLISION_LANE_WIDTH) {
                collisionCount++;
                recentlyCollidedCars.add(npc);
                showCollisionWarning = true;
                warningTimer = 60; // Show warning for 60 frames (about 1 second)

                if (collisionCount >= MAX_COLLISIONS) {
                    gameOver = true;
                }
                break; // Only count one collision per frame
            }
        }
    }

    private void updateNPCCars() {
        for (int i = npcCars.size() - 1; i >= 0; i--) {
            Car npc = npcCars.get(i);

            double currentRoadPos = npc.getRoadPosition();
            npc.setRoadPosition(currentRoadPos - speed * npc.getSpeed());

            if (npc.getRoadPosition() < -2) {
                npcCars.remove(i);
                recentlyCollidedCars.remove(npc); // Clean up reference
            }
        }

        if (npcCars.size() < 2 && random.nextInt(100) < 2) {
            BufferedImage npcImage;
            if (random.nextBoolean()) {
                npcImage = npc1img;
            } else {
                npcImage = npc2img;
            }

            double[] safePosition = new double[2];
            if (findSafeSpawnPosition(safePosition)) {
                double roadPos = safePosition[0];
                double lane = safePosition[1];
                double npcSpeed = 0.6 + random.nextDouble() * 0.8;

                Car npcCar = new Car(npcImage, 0, 0, roadPos, lane, npcSpeed);
                npcCars.add(npcCar);
            }
        }
    }

    private void resetGame() {
        collisionCount = 0;
        gameOver = false;
        showCollisionWarning = false;
        warningTimer = 0;
        recentlyCollidedCars.clear();
        npcCars.clear();
        position = 0.0;
        speed = 0.3;
        playerCar.setxCoord(290);
        spawnInitialNPCCars();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();

        // Draw sky and background
        g2d.setColor(SKY_COLOR);
        g2d.fillRect(0, 0, width, height);
        g2d.drawImage(background1, 50, 0, null);

        // Draw ground/grass
        g2d.setColor(GRASS_COLOR);
        g2d.fillRect(0, (height / 2) + 11, width, height / 2);

        // Game over screen
        if (gameOver) {
            g2d.setColor(new Color(0, 0, 0, 128)); // Semi-transparent overlay
            g2d.fillRect(0, 0, width, height);

            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            String gameOverText = "GAME OVER!";
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(gameOverText);
            g2d.drawString(gameOverText, (width - textWidth) / 2, height / 2 - 50);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            String collisionText = "Too many collisions: " + collisionCount + "/" + MAX_COLLISIONS;
            fm = g2d.getFontMetrics();
            textWidth = fm.stringWidth(collisionText);
            g2d.drawString(collisionText, (width - textWidth) / 2, height / 2);

            String restartText = "Press R to restart";

            fm = g2d.getFontMetrics();
            textWidth = fm.stringWidth(restartText);
            g2d.drawString(restartText, (width - textWidth) / 2, height / 2 + 50);

            return;
        }

        // Show collision warning
        if (showCollisionWarning && warningTimer > 0) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            String warningText = "COLLISION!";
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(warningText);
            g2d.drawString(warningText, (width - textWidth) / 2, 150);
            warningTimer--;
            if (warningTimer <= 0) {
                showCollisionWarning = false;
            }
        }

        // Show game stats
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Speed: " + String.format("%.2f", speed), 10, 30);
        g2d.drawString("Collisions: " + collisionCount + "/" + MAX_COLLISIONS, 10, 50);
        g2d.drawString("W: Speed Up, S: Slow Down", 10, 70);
        g2d.drawString("A: Move Left, D: Move Right", 10, 90);
        g2d.drawString("NPC Cars: " + npcCars.size(), 10, 110);

        int roadCenterX = width / 2;
        int horizonY = height / 2;

        // Draw road segments
        for (int i = 0; i < NUM_SEGMENTS - 1; i++) {
            double segmentIndex = i + (position % 3);

            double depth1 = (double) i / NUM_SEGMENTS;
            double scale1;
            if (1.0 - depth1 * 0.7 > 0.01) {
                scale1 = 1.0 - depth1 * 0.7;
            } else {
                scale1 = 0.01;
            }

            double depth2 = (double) (i + 1) / NUM_SEGMENTS;
            double scale2;
            if (1.0 - depth2 * 0.7 > 0.01) {
                scale2 = 1.0 - depth2 * 0.7;
            } else {
                scale2 = 0.01;
            }

            int y1 = (int) (horizonY + (1 - depth1) * (height - horizonY));
            int y2 = (int) (horizonY + (1 - depth2) * (height - horizonY));

            int roadWidth1 = (int) (ROAD_WIDTH * scale1);
            int roadWidth2 = (int) (ROAD_WIDTH * scale2);

            int rumbleWidth1 = (int) (roadWidth1 * 1.2);
            int rumbleWidth2 = (int) (roadWidth2 * 1.2);

            boolean isEven = ((int) (segmentIndex / 3)) % 2 == 0;
            Color roadColor;
            if (isEven) {
                roadColor = ROAD_LIGHT;
            } else {
                roadColor = ROAD_DARK;
            }

            Color rumbleColor;
            if (isEven) {
                rumbleColor = RUMBLE_WHITE;
            } else {
                rumbleColor = RUMBLE_RED;
            }

            drawTrapezoid(g2d, rumbleColor,
                    roadCenterX - rumbleWidth1 / 2, y1, rumbleWidth1,
                    roadCenterX - rumbleWidth2 / 2, y2, rumbleWidth2);

            drawTrapezoid(g2d, roadColor,
                    roadCenterX - roadWidth1 / 2, y1, roadWidth1,
                    roadCenterX - roadWidth2 / 2, y2, roadWidth2);

            if (isEven && scale1 > 0.1) {
                int lineWidth1;
                if (roadWidth1 * 0.05 > 1) {
                    lineWidth1 = (int) (roadWidth1 * 0.05);
                } else {
                    lineWidth1 = 1;
                }

                int lineWidth2;
                if (roadWidth2 * 0.05 > 1) {
                    lineWidth2 = (int) (roadWidth2 * 0.05);
                } else {
                    lineWidth2 = 1;
                }

                drawTrapezoid(g2d, LINE_WHITE,
                        roadCenterX - lineWidth1 / 2, y1, lineWidth1,
                        roadCenterX - lineWidth2 / 2, y2, lineWidth2);
            }
        }

        drawNPCCars(g2d, width, height, roadCenterX, horizonY);
        g2d.drawImage(car, playerCar.getxCoord(), playerCar.getyCoord(), null);
    }

    private void drawNPCCars(Graphics2D g2d, int width, int height, int roadCenterX, int horizonY) {
        for (Car npc : npcCars) {
            double depth = npc.getRoadPosition() / NUM_SEGMENTS;

            if (depth < 0 || depth > 1) continue;

            double scale;
            if (1.0 - depth * 0.7 > 0.01) {
                scale = 1.0 - depth * 0.7;
            } else {
                scale = 0.01;
            }

            int y = (int) (horizonY + (1 - depth) * (height - horizonY));
            int roadWidth = (int) (ROAD_WIDTH * scale);
            int carX = (int) (roadCenterX + (npc.getLaneOffset() * roadWidth * 0.3));

            BufferedImage carImage = npc.getImage();
            int carWidth = (int) (carImage.getWidth() * scale);
            int carHeight = (int) (carImage.getHeight() * scale);

            carX -= carWidth / 2;
            y -= carHeight;

            if (scale > 0.1 && y > horizonY) {
                g2d.drawImage(carImage, carX, y, carWidth, carHeight, null);
            }
        }
    }

    private void drawTrapezoid(Graphics2D g2d, Color color, int x1, int y1, int width1, int x2, int y2, int width2) {
        g2d.setColor(color);
        int[] xPoints = {x1, x1 + width1, x2 + width2, x2};
        int[] yPoints = {y1, y1, y2, y2};
        g2d.fillPolygon(xPoints, yPoints, 4);
    }

    private void updatePosition() {
        if (gameOver) return; // Don't update if game is over

        // Speed logic
        if (wPressed && sPressed) {
            if (speed - NATURAL_DECELERATION > MIN_SPEED) {
                speed = speed - NATURAL_DECELERATION;
            } else {
                speed = MIN_SPEED;
            }
        } else if (wPressed) {
            if (speed + SPEED_INCREMENT < MAX_SPEED) {
                speed = speed + SPEED_INCREMENT;
            } else {
                speed = MAX_SPEED;
            }
        } else if (sPressed) {
            if (speed - BRAKE_INCREMENT > MIN_SPEED) {
                speed = speed - BRAKE_INCREMENT;
            } else {
                speed = MIN_SPEED;
            }
        } else {
            if (speed - NATURAL_DECELERATION > MIN_SPEED) {
                speed = speed - NATURAL_DECELERATION;
            } else {
                speed = MIN_SPEED;
            }
        }

        // Horizontal movement
        int currentX = playerCar.getxCoord();
        if (aPressed && !dPressed) {
            if (currentX - CAR_MOVE_SPEED > CAR_MIN_X) {
                playerCar.setxCoord((int) (currentX - CAR_MOVE_SPEED));
            } else {
                playerCar.setxCoord((int) CAR_MIN_X);
            }
        } else if (dPressed && !aPressed) {
            if (currentX + CAR_MOVE_SPEED < CAR_MAX_X) {
                playerCar.setxCoord((int) (currentX + CAR_MOVE_SPEED));
            } else {
                playerCar.setxCoord((int) CAR_MAX_X);
            }
        }

        updateNPCCars();
        checkPlayerCollisions(); // Check for collisions each frame
        position += speed;
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updatePosition();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == 87) {
            wPressed = true;
        } else if (keyCode == 83) {
            sPressed = true;
        } else if (keyCode == 65) {
            aPressed = true;
        } else if (keyCode == 68) {
            dPressed = true;
        } else if (keyCode == 82 && gameOver) {
            resetGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == 87) { // W
            wPressed = false;
        } else if (keyCode == 83) {
            sPressed = false;
        } else if (keyCode == 65) {
            aPressed = false;
        } else if (keyCode == 68) {
            dPressed = false;
        }
    }
}