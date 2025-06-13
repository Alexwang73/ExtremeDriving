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
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;

public class RoadPanel extends JPanel implements ActionListener, KeyListener {

    private Font eightBit;
    private Font eightBitLarge;
    private Font eightBitSmall;

    private static final int NUM_SEGMENTS = 25;
    private static final double ROAD_WIDTH = 600;
    private static final double MIN_CAR_DISTANCE = 3.0;
    private static final double LANE_WIDTH = 0.6;

    private static final int MAX_COLLISIONS = 20;
    private static final double COLLISION_DISTANCE = 3;
    private static final double COLLISION_LANE_WIDTH = 0.4;

    // Game state constants
    private static final int START_SCREEN = 0;
    private static final int SETTINGS_SCREEN = 1;
    private static final int PLAYING = 2;
    private static final int GAME_OVER = 3;

    private int currentState = START_SCREEN;

    // Game state variables
    private int collisionCount = 0;
    private int totalScore = 0;
    private long gameStartTime = 0;
    private int finalTime = 0;
    private boolean showCollisionWarning = false;
    private int warningTimer = 0;
    private List<Car> recentlyCollidedCars = new ArrayList<>();

    // Settings variables
    private double maxSpeed = 1.0; // Default MAX_SPEED
    private double carMoveSpeed = 8.0; // Default CAR_MOVE_SPEED
    private static final double MIN_MAX_SPEED = 0.5;
    private static final double MAX_MAX_SPEED = 2.0;
    private static final double MIN_CAR_MOVE_SPEED = 4.0;
    private static final double MAX_CAR_MOVE_SPEED = 16.0;

    // UI Components
    private JButton startButton;
    private JButton settingsButton;
    private JButton backButton;
    private JButton restartButton;
    private JButton mainMenuButton;
    private JButton speedUpButton;
    private JButton speedDownButton;
    private JButton turnSpeedUpButton;
    private JButton turnSpeedDownButton;
    private JLabel speedLabel;
    private JLabel turnSpeedLabel;

    // Colors for game rendering only
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
    private static final double SPEED_INCREMENT = 0.003;
    private static final double BRAKE_INCREMENT = 0.004;
    private static final double NATURAL_DECELERATION = 0.001;

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

        try{
            eightBit = Font.createFont(Font.TRUETYPE_FONT,new File("src/pixel-emulator.ttf")).deriveFont(24f);
            eightBitLarge = Font.createFont(Font.TRUETYPE_FONT,new File("src/pixel-emulator.ttf")).deriveFont(50f);
            eightBitSmall = Font.createFont(Font.TRUETYPE_FONT,new File("src/pixel-emulator.ttf")).deriveFont(18f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(eightBit);
            ge.registerFont(eightBitLarge);
            ge.registerFont(eightBitSmall);
        } catch (IOException | FontFormatException e){
            e.printStackTrace();
        }

        playerCar = new Car(car, 290, 465, 0);

        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        initializeButtons();
        setupStartScreen();
    }

    private void initializeButtons() {
        // Start screen buttons
        startButton = new JButton("START GAME");
        settingsButton = new JButton("SETTINGS");

        // Settings screen buttons
        backButton = new JButton("BACK");
        speedUpButton = new JButton("+");
        speedDownButton = new JButton("-");
        turnSpeedUpButton = new JButton("+");
        turnSpeedDownButton = new JButton("-");
        speedLabel = new JLabel("Max Speed: " + String.format("%.1f", maxSpeed));
        turnSpeedLabel = new JLabel("Turn Speed: " + String.format("%.1f", carMoveSpeed));

        // Game over screen buttons
        restartButton = new JButton("RESTART");
        mainMenuButton = new JButton("MAIN MENU");

        // Add action listeners
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startNewGame();
            }
        });

        settingsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setupSettingsScreen();
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setupStartScreen();
            }
        });

        restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startNewGame();
            }
        });

        mainMenuButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setupStartScreen();
            }
        });

        speedUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                maxSpeed = Math.min(maxSpeed + 0.1, MAX_MAX_SPEED);
                speedLabel.setText("Max Speed: " + String.format("%.1f", maxSpeed));
            }
        });

        speedDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                maxSpeed = Math.max(maxSpeed - 0.1, MIN_MAX_SPEED);
                speedLabel.setText("Max Speed: " + String.format("%.1f", maxSpeed));
            }
        });

        turnSpeedUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                carMoveSpeed = Math.min(carMoveSpeed + 1.0, MAX_CAR_MOVE_SPEED);
                turnSpeedLabel.setText("Turn Speed: " + String.format("%.1f", carMoveSpeed));
            }
        });

        turnSpeedDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                carMoveSpeed = Math.max(carMoveSpeed - 1.0, MIN_CAR_MOVE_SPEED);
                turnSpeedLabel.setText("Turn Speed: " + String.format("%.1f", carMoveSpeed));
            }
        });
    }

    private void setupStartScreen() {
        currentState = START_SCREEN;
        removeAll();
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout());
        titlePanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("SPEED RACER");
        titleLabel.setFont(eightBitLarge);
        titlePanel.add(titleLabel);

        // Button panel with BoxLayout
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Center align buttons
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(startButton);
        buttonPanel.add(Box.createVerticalStrut(10)); // 10px spacing
        buttonPanel.add(settingsButton);

        // Instructions panel
        JPanel instructionsPanel = new JPanel(new FlowLayout());
        instructionsPanel.setBackground(Color.WHITE);
        JLabel instructionsLabel = new JLabel("<html><center>WASD to control your car<br/>Avoid " + MAX_COLLISIONS + " collisions to stay alive!<br/>Reach higher speeds for better scores</center></html>");
        instructionsLabel.setFont(eightBitSmall);
        instructionsPanel.add(instructionsLabel);

        add(titlePanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(instructionsPanel, BorderLayout.SOUTH);

        revalidate();
        repaint();
        requestFocusInWindow();
    }

    private void setupSettingsScreen() {
        currentState = SETTINGS_SCREEN;
        removeAll();
        setLayout(new BorderLayout());

        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("SETTINGS");
        titleLabel.setFont(eightBitLarge);
        titlePanel.add(titleLabel);

        // Settings panel with nested panels
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(Color.WHITE);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        // Max Speed setting row
        JPanel speedRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        speedRow.setBackground(Color.WHITE);
        speedRow.add(speedDownButton);
        speedRow.add(speedLabel);
        speedRow.add(speedUpButton);

        // Turn Speed setting row
        JPanel turnSpeedRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        turnSpeedRow.setBackground(Color.WHITE);
        turnSpeedRow.add(turnSpeedDownButton);
        turnSpeedRow.add(turnSpeedLabel);
        turnSpeedRow.add(turnSpeedUpButton);

        settingsPanel.add(speedRow);
        settingsPanel.add(Box.createVerticalStrut(20)); // 20px spacing
        settingsPanel.add(turnSpeedRow);

        // Back button panel
        JPanel backPanel = new JPanel();
        backPanel.setBackground(Color.WHITE);
        backPanel.add(backButton);

        add(titlePanel, BorderLayout.NORTH);
        add(settingsPanel, BorderLayout.CENTER);
        add(backPanel, BorderLayout.SOUTH);

        revalidate();
        repaint();
        requestFocusInWindow();
    }

    private void startNewGame() {
        currentState = PLAYING;
        removeAll(); // Remove UI components for game screen

        collisionCount = 0;
        showCollisionWarning = false;
        warningTimer = 0;
        recentlyCollidedCars.clear();
        npcCars.clear();
        position = 0.0;
        speed = 0.3;
        playerCar.setxCoord(290);
        gameStartTime = System.currentTimeMillis();
        spawnInitialNPCCars();

        revalidate();
        repaint();
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
            double lane = (random.nextDouble() - 0.6) * 1.2;
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

    private double getPlayerLaneOffset() {
        int roadCenterX = getWidth() / 2;
        int roadWidth = (int)(ROAD_WIDTH);
        return (double)(playerCar.getxCoord() + car.getWidth()/2 - roadCenterX) / (roadWidth * 0.3);
    }

    private void checkPlayerCollisions() {
        if (currentState != PLAYING) {
            return;
        }

        double playerLaneOffset = getPlayerLaneOffset();
        double playerRoadPosition = 0;

        List<Car> updatedList = new ArrayList<Car>();
        for (int i = 0; i < recentlyCollidedCars.size(); i++) {
            Car car = recentlyCollidedCars.get(i);
            double pos = car.getRoadPosition();
            if (pos >= -5 && pos <= 5) {
                updatedList.add(car);
            }
        }
        recentlyCollidedCars = updatedList;

        boolean collisionHandled = false;

        for (int i = 0; i < npcCars.size(); i++) {
            Car npc = npcCars.get(i);

            boolean alreadyCollided = false;
            for (int j = 0; j < recentlyCollidedCars.size(); j++) {
                if (recentlyCollidedCars.get(j) == npc) {
                    alreadyCollided = true;
                }
            }

            if (!alreadyCollided && !collisionHandled) {
                double roadDistance = Math.abs(npc.getRoadPosition() - playerRoadPosition);
                double laneDistance = Math.abs(npc.getLaneOffset() - playerLaneOffset);

                if (roadDistance < COLLISION_DISTANCE && laneDistance < COLLISION_LANE_WIDTH) {
                    collisionCount++;
                    recentlyCollidedCars.add(npc);
                    showCollisionWarning = true;
                    warningTimer = 60;

                    if (collisionCount >= MAX_COLLISIONS) {
                        finalTime = (int)((System.currentTimeMillis() - gameStartTime) / 1000);
                        SwingUtilities.invokeLater(() -> setupGameOverScreen());
                    }
                    collisionHandled = true;
                }
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
                recentlyCollidedCars.remove(npc);
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (currentState == PLAYING) {
            drawGameScreen(g);
        }
    }

    private void drawGameScreen(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();

        // Draw sky and background
        g2d.setColor(SKY_COLOR);
        g2d.fillRect(0, 0, width, height);
        if (background1 != null) {
            g2d.drawImage(background1, 50, 0, null);
        }

        // Draw ground/grass
        g2d.setColor(GRASS_COLOR);
        g2d.fillRect(0, (height / 2) + 11, width, height / 2);

        // Show collision warning
        if (showCollisionWarning && warningTimer > 0) {
            g2d.setColor(Color.RED);
            g2d.setFont(eightBitLarge);
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
        g2d.setFont(eightBitSmall);
        g2d.drawString("Speed: " + String.format("%.2f", speed), 10, 30);
        g2d.drawString("Collisions: " + collisionCount + "/" + MAX_COLLISIONS, 10, 50);
        int currentTime = (int)((System.currentTimeMillis() - gameStartTime) / 1000);
        g2d.drawString("Time: " + currentTime + "s", 10, 70);
        g2d.drawString("W: Speed Up, S: Slow Down", 10, 90);
        g2d.drawString("A: Move Left, D: Move Right", 10, 110);

        int roadCenterX = width / 2;
        int horizonY = height / 2;

        // Draw road segments
        for (int i = 0; i < NUM_SEGMENTS - 1; i++) {
            double segmentIndex = i + (position % 3);

            double depth1 = (double) i / NUM_SEGMENTS;
            double scale1 = Math.max(1.0 - depth1 * 0.7, 0.01);

            double depth2 = (double) (i + 1) / NUM_SEGMENTS;
            double scale2 = Math.max(1.0 - depth2 * 0.7, 0.01);

            int y1 = (int) (horizonY + (1 - depth1) * (height - horizonY));
            int y2 = (int) (horizonY + (1 - depth2) * (height - horizonY));

            int roadWidth1 = (int) (ROAD_WIDTH * scale1);
            int roadWidth2 = (int) (ROAD_WIDTH * scale2);

            int rumbleWidth1 = (int) (roadWidth1 * 1.2);
            int rumbleWidth2 = (int) (roadWidth2 * 1.2);

            boolean isEven = ((int) (segmentIndex / 3)) % 2 == 0;
            Color roadColor = isEven ? ROAD_LIGHT : ROAD_DARK;
            Color rumbleColor = isEven ? RUMBLE_WHITE : RUMBLE_RED;

            drawTrapezoid(g2d, rumbleColor,
                    roadCenterX - rumbleWidth1 / 2, y1, rumbleWidth1,
                    roadCenterX - rumbleWidth2 / 2, y2, rumbleWidth2);

            drawTrapezoid(g2d, roadColor,
                    roadCenterX - roadWidth1 / 2, y1, roadWidth1,
                    roadCenterX - roadWidth2 / 2, y2, roadWidth2);

            if (isEven && scale1 > 0.1) {
                int lineWidth1 = Math.max((int) (roadWidth1 * 0.05), 1);
                int lineWidth2 = Math.max((int) (roadWidth2 * 0.05), 1);

                drawTrapezoid(g2d, LINE_WHITE,
                        roadCenterX - lineWidth1 / 2, y1, lineWidth1,
                        roadCenterX - lineWidth2 / 2, y2, lineWidth2);
            }
        }

        drawNPCCars(g2d, width, height, roadCenterX, horizonY);
        if (car != null) {
            g2d.drawImage(car, playerCar.getxCoord(), playerCar.getyCoord(), null);
        }
    }

    private void drawNPCCars(Graphics2D g2d, int width, int height, int roadCenterX, int horizonY) {
        for (Car npc : npcCars) {
            double depth = npc.getRoadPosition() / NUM_SEGMENTS;

            if (depth < 0 || depth > 1) continue;

            double scale = Math.max(1.0 - depth * 0.7, 0.01);

            int y = (int) (horizonY + (1 - depth) * (height - horizonY));
            int roadWidth = (int) (ROAD_WIDTH * scale);
            int carX = (int) (roadCenterX + (npc.getLaneOffset() * roadWidth * 0.3));

            BufferedImage carImage = npc.getImage();
            if (carImage != null) {
                int carWidth = (int) (carImage.getWidth() * scale);
                int carHeight = (int) (carImage.getHeight() * scale);

                carX -= carWidth / 2;
                y -= carHeight;

                if (scale > 0.1 && y > horizonY) {
                    g2d.drawImage(carImage, carX, y, carWidth, carHeight, null);
                }
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
        if (currentState != PLAYING) return;

        // Speed logic using the configurable maxSpeed
        if (wPressed && sPressed) {
            speed = Math.max(speed - NATURAL_DECELERATION, MIN_SPEED);
        } else if (wPressed) {
            speed = Math.min(speed + SPEED_INCREMENT, maxSpeed);
        } else if (sPressed) {
            speed = Math.max(speed - BRAKE_INCREMENT, MIN_SPEED);
        } else {
            speed = Math.max(speed - NATURAL_DECELERATION, MIN_SPEED);
        }

        // Horizontal movement using configurable carMoveSpeed
        int currentX = playerCar.getxCoord();
        if (aPressed && !dPressed) {
            playerCar.setxCoord((int) Math.max(currentX - carMoveSpeed, CAR_MIN_X));
        } else if (dPressed && !aPressed) {
            playerCar.setxCoord((int) Math.min(currentX + carMoveSpeed, CAR_MAX_X));
        }

        updateNPCCars();
        checkPlayerCollisions();
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

        // Handle game controls only during gameplay
        if (currentState == PLAYING) {
            if (keyCode == 87) { // W
                wPressed = true;
            } else if (keyCode == 83) { // S
                sPressed = true;
            } else if (keyCode == 65) { // A
                aPressed = true;
            } else if (keyCode == 68) { // D
                dPressed = true;
            }
        }

// Handle menu navigation
        if (keyCode == 27) { // ESCAPE
            if (currentState == PLAYING) {
                setupStartScreen();
            } else if (currentState == SETTINGS_SCREEN) {
                setupStartScreen();
            }
        }

// Handle restart from game over screen
        if (keyCode == 82 && currentState == GAME_OVER) { // R
            startNewGame();
        }

// Handle enter key for start screen
        if (keyCode == 10 && currentState == START_SCREEN) { // ENTER
            startNewGame();
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == 87) { // W
            wPressed = false;
        } else if (keyCode == 83) { // S
            sPressed = false;
        } else if (keyCode == 65) { // A
            aPressed = false;
        } else if (keyCode == 68) { // D
            dPressed = false;
        }
    }
}