package PongGame2ALT;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class GamePanel extends JPanel implements ActionListener, KeyListener {

    // ----------------------------
    // Game mode / state flags
    // ----------------------------
    private boolean twoPlayer;
    private boolean singlePlayer; // default mode
    private boolean inMenu = true;
    private boolean inPlayOptions = false;
    private boolean inHiScore = false; // Hi-Score screen flag
    private boolean inSettings = false;
    private boolean paused = false;
    private boolean inPauseMenu = false; // pause menu active

    private boolean showResult = false;       // true when game ends
    private boolean playerWon = false;        // true if Player 1 wins
    private int winner = 0; // 0 = none, 1 = player1, 2 = player2
    private int menuGlowTimer = 0; // increments every frame

    private int pauseMenuSelection = 0;   // pause menu selection
    private int pausedMenuIndex = 0; // 0 = Continue, 1 = Exit to Menu
    private String resultText = "";
    private int selectedResultOption = 0;     // 0 = Try Again, 1 = Exit to Menu
    private boolean upPressed, downPressed, wPressed, sPressed;

    // ----------------------------
    // Game objects & rendering
    // ----------------------------
    private final int WIDTH = 800, HEIGHT = 600;
    private Timer timer;
    private Paddle paddle1, paddle2;
    private Ball ball;
    private ArrayList<Star> stars;
    private ArrayList<Nebula> nebulas;
    private Random random;

    // ----------------------------
    // Score & gameplay
    // ----------------------------
    private int player1Score = 0, player2Score = 0;
    private int scoreTimer = 0;
    private final int SCORE_DELAY = 60;
    private int maxScore = 10;

    private String[] menuOptions = {"Play", "Hi-Score", "Settings", "Exit"};
    private int selectedOption = 0;

    private String[] pauseOptions = {"Continue", "Exit to Menu"};
    private int selectedPauseOption = 0; // 0 = Continue, 1 = Exit

    private String[] playOptions = {"1 Player", "2 Player"};
    private int selectedPlayOption = 0;

    private String[] difficulties = {"Slow", "Normal", "Fast", "Ultra"};
    private int selectedDifficulty = 1;

    // ----------------------------
    // Leaderboard system
    // ----------------------------
    // ✅ This is where the leaderboard field is declared
    // Accessible to all methods in GamePanel
    private java.util.List<String[]> leaderboard = new ArrayList<>();

    private static final String LEADERBOARD_DIR = "data";
    private static final String LEADERBOARD_FILE = LEADERBOARD_DIR + File.separator + "leaderboard.txt";
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ----------------------------
    // Glow pulsing effect
    // ----------------------------
    private float glowPulse = 0f;
    private boolean pulseUp = true;

    public GamePanel() {
    setPreferredSize(new Dimension(WIDTH, HEIGHT));

    setFocusable(true);
    addKeyListener(this);
    setBackground(Color.BLACK);
    SwingUtilities.invokeLater(this::requestFocusInWindow);

        // --- INIT LEADERBOARD ---
    leaderboard.clear(); // clear in-memory list
    File dir = new File(LEADERBOARD_DIR);
    if (!dir.exists()) dir.mkdirs(); // ensure folder exists
    
    File file = new File(LEADERBOARD_FILE);
    if (file.exists()) {
        try {
            new BufferedWriter(new FileWriter(file, false)).close(); // truncate file
            System.out.println("Leaderboard cleared for fresh start.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    initGame();
    inMenu = true;

    timer = new Timer(16, this);
    timer.start();
}

    public void setSinglePlayer(boolean singlePlayer) {
        this.singlePlayer = singlePlayer;
        this.twoPlayer = !singlePlayer;
        resetRound();
        System.out.println("Game mode: " + (singlePlayer ? "Single Player" : "Two Player"));
    }

    private void initGame() {
    random = new Random();

    // Player 1 paddle (always red)
    paddle1 = new Paddle(50, HEIGHT / 2 - 40, 15, 80, 7);
    paddle1.setColor(Color.RED);

    // Player 2 paddle (blue, AI or human)
    paddle2 = new Paddle(WIDTH - 65, HEIGHT / 2 - 40, 15, 80, 7);
    paddle2.setColor(Color.BLUE);

    ball = new Ball(WIDTH / 2, HEIGHT / 2, 10, 5);

        stars = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            stars.add(new Star(
                    random.nextInt(WIDTH),
                    random.nextInt(HEIGHT),
                    2 + random.nextInt(3),
                    0.5f + random.nextFloat(),
                    Color.WHITE,
                    random.nextInt(3)
            ));
        }

        nebulas = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            nebulas.add(new Nebula(
                    random.nextInt(WIDTH),
                    random.nextInt(HEIGHT),
                    150 + random.nextInt(150),
                    100 + random.nextInt(100),
                    new Color(random.nextFloat(), random.nextFloat(), random.nextFloat()),
                    0.2f + random.nextFloat() * 0.3f,
                    0.1f + random.nextFloat() * 0.5f,
                    0.1f + random.nextFloat() * 0.5f
            ));
        }
        loadLeaderboard();
        paused = false;
    }
    private void addToLeaderboard(String name, int score) {
    if (leaderboard == null) leaderboard = new ArrayList<>();
    String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    leaderboard.add(new String[]{name, String.valueOf(score), date});

    // sort by highest score first
    leaderboard.sort((a, b) -> Integer.compare(Integer.parseInt(b[1]), Integer.parseInt(a[1])));

    // keep only top 10
    if (leaderboard.size() > 10)
        leaderboard = new ArrayList<>(leaderboard.subList(0, 10));
}

private void saveLeaderboard() {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter("leaderboard.txt"))) {
        for (String[] entry : leaderboard) {
            bw.write(String.join(",", entry));
            bw.newLine();
        }
        System.out.println("Leaderboard saved with " + leaderboard.size() + " entries");
    } catch (IOException e) {
        System.err.println("Error saving leaderboard: " + e.getMessage());
    }
}

private void loadLeaderboard() {
    leaderboard = new ArrayList<>();
    File f = new File(LEADERBOARD_FILE);
    if (!f.exists()) return;

    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
        String line;
        boolean firstLine = true;
        while ((line = br.readLine()) != null) {
            if (firstLine) { 
                firstLine = false; 
                if (line.startsWith("winner,")) continue; // skip header
            }
            String[] data = line.split(",");
            if (data.length >= 3) leaderboard.add(data);
        }
        System.out.println("Loaded " + leaderboard.size() + " leaderboard entries");
    } catch (IOException e) {
        System.err.println("Error loading leaderboard: " + e.getMessage());
    }
}



    private void saveScore(String winnerName, int score, String mode) {
    try {
        File dir = new File(LEADERBOARD_DIR);
        if (!dir.exists()) dir.mkdirs();

        File file = new File(LEADERBOARD_FILE);
        boolean newFile = !file.exists();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) { // append only
            if (newFile) {
                bw.write("winner,score,mode,timestamp");
                bw.newLine();
            }
            String timestamp = LocalDateTime.now().format(DT_FMT);
            String line = String.format("%s,%d,%s,%s", winnerName, score, mode, timestamp);
            bw.write(line);
            bw.newLine();
        }

        System.out.println("Saved leaderboard entry: " + winnerName + " / " + score + " / " + mode);
    } catch (IOException ex) {
        ex.printStackTrace();
    }
}



    public void resetGame() {
        player1Score = 0;
        player2Score = 0;
        resetRound();
        paused = false;
        inMenu = true;
        inPlayOptions = false;
        inHiScore = false;
        inSettings = false;
    }
    

    private void resetRound() {
        paddle1 = new Paddle(50, HEIGHT / 2 - 40, 15, 80, 7);
paddle1.setColor(Color.RED); // Player 1

paddle2 = new Paddle(WIDTH - 65, HEIGHT / 2 - 40, 15, 80, 7);
paddle2.setColor(Color.BLUE); // Player 2 or AI
        ball.reset(WIDTH / 2, HEIGHT / 2, 10, 5);
        scoreTimer = 0;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateSpace();
        updateGlowPulse();
        menuGlowTimer++;
        if (!paused && !inMenu && !inPlayOptions && !inHiScore && !inSettings) {
            updateGame();
        }
        repaint();
    }

    private void updateGlowPulse() {
        if (pulseUp) {
            glowPulse += 0.05f;
            if (glowPulse >= 1f) {
                pulseUp = false;
            }
        } else {
            glowPulse -= 0.05f;
            if (glowPulse <= 0f) {
                pulseUp = true;
            }
        }
    }

    private void updateSpace() {
        for (Star s : stars) {
            s.y += s.speedMultiplier;
            if (s.y > HEIGHT) {
                s.y = 0;
                s.x = random.nextInt(WIDTH);
            }
        }

        for (Nebula n : nebulas) {
            n.x += n.driftX;
            n.y += n.driftY;
            if (n.x < -n.w) {
                n.x = WIDTH;
            }
            if (n.y < -n.h) {
                n.y = HEIGHT;
            }
            if (n.x > WIDTH) {
                n.x = -n.w;
            }
            if (n.y > HEIGHT) {
                n.y = -n.h;
            }
        }
    }

    private void updateGame() {
        if (showResult) {
            return;
        }

        if (wPressed) {
            paddle1.moveUp();
        }
        if (sPressed) {
            paddle1.moveDown(HEIGHT);
        }

        if (twoPlayer) {
            if (upPressed) {
                paddle2.moveUp();
            }
            if (downPressed) {
                paddle2.moveDown(HEIGHT);
            }
        } else {
            int ballCenter = (int) (ball.y + ball.diameter / 2);
            int paddleCenter = paddle2.y + paddle2.getHeight() / 2;
            if (Math.abs(ballCenter - paddleCenter) > 5) {
                if (ballCenter > paddleCenter) {
                    paddle2.moveDown(HEIGHT);
                } else {
                    paddle2.moveUp();
                }
            }
        }

        ball.x += ball.xSpeed * ball.speedMultiplier;
        ball.y += ball.ySpeed * ball.speedMultiplier;

        if (ball.y <= 0 || ball.y >= HEIGHT - ball.diameter) {
            ball.ySpeed *= -1;
        }

        if (ball.getBounds().intersects(paddle1.getBounds())
                || ball.getBounds().intersects(paddle2.getBounds())) {
            ball.xSpeed *= -1;
            ball.speedMultiplier += 0.05;
        }

        if (scoreTimer == 0) {
            if (ball.x < 0) {
                player2Score++;
                scoreTimer = SCORE_DELAY;
            } else if (ball.x > WIDTH) {
                player1Score++;
                scoreTimer = SCORE_DELAY;
            }
        } else {
            scoreTimer--;
            if (scoreTimer == 0) {
                ball.reset(WIDTH / 2, HEIGHT / 2, 10, 5);
            }
        }

        // --- CHECK FOR WINNER ---
        if (player1Score >= maxScore || player2Score >= maxScore) {
            showResult = true;
            paused = false;
            inMenu = false;
            inPlayOptions = false;

            // Determine the result message
            if (twoPlayer) {
                if (player1Score >= maxScore) {
                    resultText = "Player 1 Win!";
                } else {
                    resultText = "Player 2 Win!";
                }
            } else {
                if (player1Score >= maxScore) {
                    resultText = "YOU WIN!";
                } else {
                    resultText = "YOU LOSE!\nAI Win!";
                }
            }

            // Freeze movement
            wPressed = sPressed = upPressed = downPressed = false;
            ball.xSpeed = 0;
            ball.ySpeed = 0;

            System.out.println("Game Over: " + resultText);
            
            String playerName;
            if (twoPlayer) {
                playerName = (player1Score > player2Score) ? "Player 1" : "Player 2";
            } else {
                playerName = (player1Score > player2Score) ? "Player" : "AI";
            }
            int finalScore = Math.max(player1Score, player2Score);
            addToLeaderboard(playerName, finalScore);
            saveLeaderboard();
            System.out.println("Leaderboard updated after game: " + playerName + " - " + finalScore);
                
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw nebulas
        for (Nebula n : nebulas) {
            n.draw(g2);
        }

        // Draw stars
        for (Star s : stars) {
            s.draw(g2);
        }

        // Draw appropriate screen
        if (inMenu) {
            drawMenu(g2);
        } else if (inPlayOptions) {
            drawPlayOptions(g2);
        } else if (inHiScore) {
            drawHiScore(g2);
        } else if (inSettings) {
            drawSettings(g2);
        } else {
            drawGame(g2);
        }

        // ----------------------------//
        // SHOW RESULT (WIN/LOSE/AI)   //
        // ----------------------------//
    if (showResult) {
        // Semi-transparent black overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        // Fonts
        Font mainFont = new Font("Consolas", Font.BOLD, 48);
        Font subFont = new Font("Consolas", Font.BOLD, 36);
        Font optionFont = new Font("Consolas", Font.BOLD, 32);

        // Colors
        Color winColor = new Color(0, 255, 0);    // bright green
        Color loseColor = new Color(255, 0, 0);   // red
        Color aiColor = new Color(255, 165, 0);   // orange

        if (twoPlayer) {
            String msg = (player1Score >= maxScore) ? "PLAYER 1 WIN!" : "PLAYER 2 WIN!";
            drawGlowingTextCenter(g2, msg, WIDTH / 2, HEIGHT / 2 - 100, winColor, Color.WHITE, mainFont);
        } else {
            if (player1Score >= maxScore) {
                drawGlowingTextCenter(g2, "YOU WIN!", WIDTH / 2, HEIGHT / 2 - 100, winColor, Color.WHITE, mainFont);
            } else {
                drawGlowingTextCenter(g2, "YOU LOSE!", WIDTH / 2, HEIGHT / 2 - 100, loseColor, Color.WHITE, mainFont);
                drawGlowingTextCenter(g2, "AI WIN!", WIDTH / 2, HEIGHT / 2 - 40, aiColor, Color.WHITE, subFont);
            }
        }

    // Draw result menu options (Try Again / Exit to Menu)
    String[] resultOptions = {"Try Again", "Exit to Menu"};
    for (int i = 0; i < resultOptions.length; i++) {
        Color glow = (i == selectedResultOption) ? Color.CYAN : Color.BLUE;
        Color main = (i == selectedResultOption) ? Color.WHITE : Color.LIGHT_GRAY;
        drawGlowingTextCenter(g2, resultOptions[i], WIDTH / 2, HEIGHT / 2 + 40 + i * 50, glow, main, optionFont);
    }
}

        // ----------------------------
        // PAUSED overlay
        // ----------------------------
        if (paused && !inMenu && !inPlayOptions && !inHiScore && !inSettings) {
            String[] pauseOptions = {"Continue", "Exit to Menu"};

            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, WIDTH, HEIGHT);

            g2.setFont(new Font("Arial", Font.BOLD, 48));
            String pausedText = "PAUSED";
            drawGlowingTextCenter(g2, pausedText, WIDTH / 2, HEIGHT / 2 - 80, Color.YELLOW, Color.WHITE, new Font("Arial", Font.BOLD, 48));

            g2.setFont(new Font("Arial", Font.BOLD, 28));
            for (int i = 0; i < pauseOptions.length; i++) {
                String option = pauseOptions[i];
                Color glow = (i == pausedMenuIndex) ? Color.CYAN : Color.BLUE;
                Color main = (i == pausedMenuIndex) ? Color.WHITE : Color.LIGHT_GRAY;
                drawGlowingTextCenter(g2, option, WIDTH / 2, HEIGHT / 2 - 20 + i * 50, glow, main, new Font("Arial", Font.BOLD, 28));
            }
        }
    }

    private void drawGame(Graphics2D g2) {
        paddle1.draw(g2);
        paddle2.draw(g2);
        ball.draw(g2);

        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.drawString("P1: " + player1Score, WIDTH / 2 - 100, 30);
        g2.drawString("P2: " + player2Score, WIDTH / 2 + 50, 30);
    }

    private void drawMenu(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        String title = "SPACE PONG";
        FontMetrics titleFm = g2.getFontMetrics();
        g2.drawString(title, (WIDTH - titleFm.stringWidth(title)) / 2, 150);

        Font font = new Font("Arial", Font.BOLD, 28);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < menuOptions.length; i++) {
            String text = menuOptions[i];
            int textWidth = fm.stringWidth(text);
            int x = (WIDTH - textWidth) / 2;
            int y = 250 + i * 60;

            if (i == selectedOption) {
                // pulsating glow effect
                float alpha = 0.4f + 0.6f * (float) Math.abs(Math.sin(menuGlowTimer * 0.05));
                g2.setColor(new Color(255, 255, 0, (int) (alpha * 255)));

                for (int dx = -3; dx <= 3; dx++) {
                    for (int dy = -3; dy <= 3; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2.drawString(text, x + dx, y + dy);
                        }
                    }
                }

                g2.setColor(Color.YELLOW); // main text
            } else {
                g2.setColor(Color.WHITE);
            }
            g2.drawString(text, x, y);
        }
    }

    private void drawPlayOptions(Graphics2D g2) {
        drawGlowingTextCenter(g2, "Select Mode", WIDTH / 2, 150, Color.YELLOW, Color.WHITE, new Font("Orbitron", Font.BOLD, 32));

        for (int i = 0; i < playOptions.length; i++) {
            String text = (i == selectedPlayOption) ? "> " + playOptions[i] + " <" : playOptions[i];
            Color glow = (i == selectedPlayOption) ? Color.YELLOW : Color.BLUE;
            Color main = (i == selectedPlayOption) ? Color.WHITE : Color.LIGHT_GRAY;
            drawGlowingTextCenter(g2, text, WIDTH / 2, 250 + i * 50, glow, main, new Font("Orbitron", Font.BOLD, 28));
        }

        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Press ENTER or SPACE to start • BACKSPACE to return", WIDTH / 2 - 200, 400);
    }

    private void drawResult(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 48));

        String msg;

        if (twoPlayer) {
            // For 2 Player Mode
            msg = (winner == 1) ? "Player 1 Win!" : "Player 2 Win!";
            g2.drawString(msg, WIDTH / 2 - g2.getFontMetrics().stringWidth(msg) / 2, HEIGHT / 2 - 100);
        } else {
            // For 1 Player Mode
            msg = playerWon ? "YOU WIN!" : "YOU LOSE!";
            g2.drawString(msg, WIDTH / 2 - g2.getFontMetrics().stringWidth(msg) / 2, HEIGHT / 2 - 100);

            if (!playerWon) {
                String aiMsg = "AI Win!";
                g2.drawString(aiMsg, WIDTH / 2 - g2.getFontMetrics().stringWidth(aiMsg) / 2, HEIGHT / 2 - 50);
            }
        }

        // Draw result options
        String[] options = {"Try Again", "Exit to Menu"};
        g2.setFont(new Font("Arial", Font.BOLD, 32));
        for (int i = 0; i < options.length; i++) {
            String prefix = (i == selectedResultOption) ? "> " : "";
            String suffix = (i == selectedResultOption) ? " <" : "";
            String text = prefix + options[i] + suffix;
            g2.drawString(text, WIDTH / 2 - g2.getFontMetrics().stringWidth(text) / 2,
                    HEIGHT / 2 + i * 50 + 20);
        }
    }

    private void drawGlowingTextCenter(Graphics2D g2, String text, int centerX, int y, Color glowColor, Color mainColor, Font font) {
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = centerX - textWidth / 2;

        float alpha = 0.3f + 0.5f * glowPulse;

        g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), (int) (alpha * 255)));
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                g2.drawString(text, x + dx, y + dy);
            }
        }

        g2.setColor(mainColor);
        g2.drawString(text, x, y);
    }

    private void drawSettings(Graphics2D g2) {
        drawGlowingText(g2, "Settings", WIDTH / 2 - 80, 150, Color.CYAN, Color.WHITE, new Font("Orbitron", Font.BOLD, 32));

        for (int i = 0; i < difficulties.length; i++) {
            Color glow = (i == selectedDifficulty) ? Color.YELLOW : Color.BLUE;
            Color main = (i == selectedDifficulty) ? Color.WHITE : Color.LIGHT_GRAY;
            drawGlowingText(g2, difficulties[i], WIDTH / 2 - 70, 250 + i * 50, glow, main, new Font("Orbitron", Font.BOLD, 28));
        }

        drawGlowingText(g2, "Max Score: " + maxScore, WIDTH / 2 - 70, 500, Color.CYAN, Color.WHITE, new Font("Orbitron", Font.BOLD, 24));
    }

    private void drawHiScore(Graphics2D g2) {
    drawGlowingText(g2, "Hi-Score", WIDTH / 2 - 80, 150, Color.YELLOW, Color.WHITE, new Font("Orbitron", Font.BOLD, 32));

    g2.setColor(Color.CYAN);
    g2.setFont(new Font("Arial", Font.PLAIN, 24));

    if (leaderboard.isEmpty()) {
        String msg = "No scores yet!";
        g2.drawString(msg, WIDTH / 2 - g2.getFontMetrics().stringWidth(msg) / 2, 250);
    } else {
        // Loop through leaderboard entries
        int y = 250;
        for (String[] entry : leaderboard) {
            // entry[0] = name, entry[1] = score, entry[2] = date
            String text = entry[0] + " - " + entry[1] + " (" + entry[2] + ")";
            g2.drawString(text, WIDTH / 2 - g2.getFontMetrics().stringWidth(text) / 2, y);
            y += 30; // space between entries
        }
    }

    g2.setFont(new Font("Arial", Font.PLAIN, 18));
    g2.setColor(Color.LIGHT_GRAY);
    g2.drawString("Press BACKSPACE to return", WIDTH / 2 - 120, HEIGHT - 50);
}


    private void drawGlowingText(Graphics2D g2, String text, int x, int y, Color glowColor, Color mainColor, Font font) {
        g2.setFont(font);
        float alpha = 0.3f + 0.5f * glowPulse;

        g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), (int) (alpha * 255)));
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                g2.drawString(text, x + dx, y + dy);
            }
        }

        g2.setColor(mainColor);
        g2.drawString(text, x, y);
    }

    private void startGame(boolean singlePlayer) {
        resetRound();
        this.singlePlayer = singlePlayer;
        this.twoPlayer = !singlePlayer;
        inMenu = false;
        inPlayOptions = false;
        paused = false;

        // DEBUG: Print which mode was selected
        System.out.println("Mode selected: " + (singlePlayer ? "Single Player" : "Two Player"));
    }
    
    

    private void handleResultInput(int key) {
        if (key == KeyEvent.VK_UP) {
            selectedResultOption = (selectedResultOption - 1 + 2) % 2; // navigate up
        } else if (key == KeyEvent.VK_DOWN) {
            selectedResultOption = (selectedResultOption + 1) % 2; // navigate down
        } else if (key == KeyEvent.VK_ENTER) {
            if (selectedResultOption == 0) {
                // --- TRY AGAIN ---
                showResult = false;
                paused = false;
                player1Score = 0;
                player2Score = 0;
                ball.reset(WIDTH / 2, HEIGHT / 2, 10, 5);

                // Keep same mode
                if (twoPlayer) {
                    System.out.println("Restarting 2 Player Game...");
                } else {
                    System.out.println("Restarting 1 Player Game...");
                }

            } else if (selectedResultOption == 1) {
                // --- EXIT TO MENU ---
                showResult = false;
                inMenu = true;
                paused = false;
                inPlayOptions = false;
                player1Score = 0;
                player2Score = 0;
                ball.reset(WIDTH / 2, HEIGHT / 2, 10, 5);
                System.out.println("Returning to Menu...");
            }
        }
    }
    

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_ESCAPE) {
            inMenu = true;
            inPlayOptions = false;
            inHiScore = false;
            inSettings = false;
            resetGame();
            return;
        }

        if (!inMenu && !inPlayOptions && !inHiScore && !inSettings) {
            if (key == KeyEvent.VK_W) {
                wPressed = true;
            }
            if (key == KeyEvent.VK_S) {
                sPressed = true;
            }
            if (key == KeyEvent.VK_UP) {
                upPressed = true;
            }
            if (key == KeyEvent.VK_DOWN) {
                downPressed = true;
            }
            if (key == KeyEvent.VK_P) {
                paused = !paused;
                inPauseMenu = paused; // show pause menu when paused
                pauseMenuSelection = 0; // default selection = Continue
            }
        }

        if (paused && inPauseMenu) {
            if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
                pauseMenuSelection = 1 - pauseMenuSelection; // toggle 0<->1
            }

            if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
                if (pauseMenuSelection == 0) { // Continue
                    paused = false;
                    inPauseMenu = false;
                } else if (pauseMenuSelection == 1) { // Exit
                    resetGame(); // back to main menu
                    
                }
            }
        }

        // If paused, handle PAUSED menu navigation
        if (paused) {
            if (key == KeyEvent.VK_UP) {
                pausedMenuIndex = (pausedMenuIndex - 1 + 2) % 2; // wrap around
            } else if (key == KeyEvent.VK_DOWN) {
                pausedMenuIndex = (pausedMenuIndex + 1) % 2;
            } else if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
                if (pausedMenuIndex == 0) { // Continue
                    paused = false;
                } else if (pausedMenuIndex == 1) { // Exit to Menu
                    resetGame(); // reset scores and round
                    showResult = false;
                    
                    // ✅ Add this line here
    loadLeaderboard(); // refresh leaderboard entries before returning to menu

    // existing line
    handleResultInput(e.getKeyCode());
    return;
                }
            }
        }

        if (inMenu) {
            if (key == KeyEvent.VK_UP) {
                selectedOption = (selectedOption - 1 + menuOptions.length) % menuOptions.length;
            }
            if (key == KeyEvent.VK_DOWN) {
                selectedOption = (selectedOption + 1) % menuOptions.length;
            }
            if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
                switch (selectedOption) {
                    case 0 -> { //Play
                        inMenu = false;
                        inPlayOptions = true; //If false, you won't start
                        selectedPlayOption = 0;
                        return;
                    }
                    case 1 -> { //Hi-Score
                        inMenu = false;
                        inHiScore = true;
                    }
                    case 2 -> { //Settings
                        inMenu = false;
                        inSettings = true;
                    }
                    case 3 -> //Exit
                        System.exit(0);
                }
            }
        }

        if (inPlayOptions) {
            if (key == KeyEvent.VK_UP) {
                selectedPlayOption = (selectedPlayOption - 1 + playOptions.length) % playOptions.length;
            }
            if (key == KeyEvent.VK_DOWN) {
                selectedPlayOption = (selectedPlayOption + 1) % playOptions.length;
            }

            if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
                boolean singlePlayerMode = (selectedPlayOption == 0); // 0 = 1 Player
                startGame(singlePlayerMode);
                inPlayOptions = false;
            }

            if (key == KeyEvent.VK_BACK_SPACE) {
                inPlayOptions = false;
                inMenu = true;
            }
        }

        // -------------------------
        // Result menu navigation
        // -------------------------
        if (showResult) {
            if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                selectedResultOption = 1 - selectedResultOption; // toggle between 0 and 1
            }

            if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
                if (selectedResultOption == 0) {  // Try Again
                    if (twoPlayer) {
                        // For 2 Player mode: reset scores and start new round directly
                        player1Score = 0;
                        player2Score = 0;
                        resetRound();
                        showResult = false;  // hide result menu
                    } else {
                        // For 1 Player mode: go back to mode selection
                        resetGame();
                        inMenu = false;
                        inPlayOptions = true;  // show 1P/2P select again
                        showResult = false;
                    }
                } else if (selectedResultOption == 1) { // Exit to Menu
                    resetGame();
                    showResult = false;  // hide result menu
                    handleResultInput(e.getKeyCode());
                    return;
                }
            }
        }
        if (inSettings) {
            if (key == KeyEvent.VK_UP) {
                selectedDifficulty = (selectedDifficulty - 1 + difficulties.length) % difficulties.length;
            }
            if (key == KeyEvent.VK_DOWN) {
                selectedDifficulty = (selectedDifficulty + 1) % difficulties.length;
            }
            if (key == KeyEvent.VK_LEFT) {
                maxScore = Math.max(1, maxScore - 1);
            }
            if (key == KeyEvent.VK_RIGHT) {
                maxScore++;
            }
            if (key == KeyEvent.VK_BACK_SPACE) {
                inSettings = false;
                inMenu = true;
            }
        }

        if (inHiScore && key == KeyEvent.VK_BACK_SPACE) {
            inHiScore = false;
            inMenu = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) {
            wPressed = false;
        }
        if (key == KeyEvent.VK_S) {
            sPressed = false;
        }
        if (key == KeyEvent.VK_UP) {
            upPressed = false;
        }
        if (key == KeyEvent.VK_DOWN) {
            downPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // Inner classes
    private static class Star {

        int x, y, size;
        float speedMultiplier;
        Color color;
        int type;

        public Star(int x, int y, int size, float speedMultiplier, Color color, int type) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speedMultiplier = speedMultiplier;
            this.color = color;
            this.type = type;
        }

        public void draw(Graphics2D g2) {
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
        }
    }
}