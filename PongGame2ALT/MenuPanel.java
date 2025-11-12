package PongGame2ALT;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class MenuPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

    private SpacePong game;
    private int menuState = 0; // 0=main,1=play,2=hi-score,3=settings
    private int hoveredIndex = -1; // for mouse hover & keyboard
    private int selectedIndex = 0; // for keyboard navigation

    // Buttons
    private ArrayList<MenuButton> buttons;
    private ArrayList<MenuButton> playButtons;
    private ArrayList<String> hiScores;
    private ArrayList<MenuButton> settingButtons;
    private String[] difficulties = {"Slow", "Normal", "Fast", "Ultra"};
    private int currentDifficulty = 1;
    private int[] maxScoreOptions = {5, 10, 15, 20, 25};
    private int maxScoreIndex = 1; // default 10

    // Moving stars
    private ArrayList<Star> stars = new ArrayList<>();

    public MenuPanel(SpacePong game) {
        this.game = game;
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();

        setupMenuButtons();
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

        // Initialize stars on resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                stars.clear();
                initStars();
            }
        });

        // Timer for moving stars + hover animation
        Timer timer = new Timer(30, e -> {
            updateStars();
            updateHoverAnimation();
            repaint();
        });
        timer.start();
    }

    // Initialize stars
    private void initStars() {
        Random rand = new Random();
        int width = getWidth();
        int height = getHeight();
        for (int i = 0; i < 50; i++) {
            stars.add(new Star(rand.nextInt(width), rand.nextInt(height), 2 + rand.nextInt(3)));
        }
    }

    // Setup menu buttons
    private void setupMenuButtons() {
        // Main menu
        buttons = new ArrayList<>();
        buttons.add(new MenuButton("Play", 300, 200, 200, 50));
        buttons.add(new MenuButton("Hi-Score", 300, 270, 200, 50));
        buttons.add(new MenuButton("Settings", 300, 340, 200, 50));
        buttons.add(new MenuButton("Exit", 300, 410, 200, 50));

        // Play submenu
        playButtons = new ArrayList<>();
        playButtons.add(new MenuButton("1 Player (AI)", 300, 300, 200, 50));
        playButtons.add(new MenuButton("2 Player", 300, 370, 200, 50));
        playButtons.add(new MenuButton("Back", 300, 440, 200, 50));

        // Hi-Score
        hiScores = new ArrayList<>();
        hiScores.add("1. P1 10  P2 3");
        hiScores.add("2. P1 8   P2 5");
        hiScores.add("3. P1 6   P2 2");
        hiScores.add("Reset");
        hiScores.add("Back");

        // Settings
        settingButtons = new ArrayList<>();
        for (int i = 0; i < difficulties.length; i++) {
            String suffix = (i == currentDifficulty) ? " (Selected)" : "";
            settingButtons.add(new MenuButton(difficulties[i] + suffix, 300, 200 + i * 60, 200, 50));
        }
        settingButtons.add(new MenuButton("Max Score: " + maxScoreOptions[maxScoreIndex], 300, 200 + difficulties.length * 60, 200, 50));
        settingButtons.add(new MenuButton("Back", 300, 200 + (difficulties.length + 1) * 60, 200, 50));
    }

    // Paint
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw stars
        g2.setColor(Color.WHITE);
        for (Star s : stars) {
            g2.fillOval(s.x, s.y, 2, 2);
        }

        ArrayList<MenuButton> currentButtons = getCurrentButtons();

        if (menuState == 2) { // Hi-Score
            g2.setFont(new Font("Arial", Font.BOLD, 24));
            for (int i = 0; i < hiScores.size(); i++) {
                String score = hiScores.get(i);
                if (score.equals("Reset") || score.equals("Back")) {
                    MenuButton temp = new MenuButton(score, 300, 350 + i * 60, 200, 50);
                    temp.hoverProgress = (i == hoveredIndex) ? 1f : 0f;
                    drawAnimatedButton(g2, temp);
                } else {
                    g2.setColor(Color.CYAN);
                    g2.drawString(score, 300, 150 + i * 40);
                }
            }
            return;
        }

        // Default menu painting
        for (MenuButton b : currentButtons) {
            drawAnimatedButton(g2, b);
        }
    }

    // Draw button with soft yellow hover
    private void drawAnimatedButton(Graphics2D g2, MenuButton b) {
        float progress = b.hoverProgress;

        // Background colors
        Color base = new Color(80, 80, 80);          // normal dark gray
        Color hover = new Color(255, 215, 128);     // soft yellow

        int r = (int) (base.getRed() + progress * (hover.getRed() - base.getRed()));
        int gC = (int) (base.getGreen() + progress * (hover.getGreen() - base.getGreen()));
        int bC = (int) (base.getBlue() + progress * (hover.getBlue() - base.getBlue()));
        Color backgroundColor = new Color(r, gC, bC);
        g2.setColor(backgroundColor);

        // Scale effect (smaller than before)
        int scaleW = (int) (b.width * 0.03 * progress);
        int scaleH = (int) (b.height * 0.03 * progress);
        g2.fillRect(b.x - scaleW / 2, b.y - scaleH / 2, b.width + scaleW, b.height + scaleH);

        // Text color
        Color textColor = (progress > 0) ? Color.BLACK : Color.WHITE;
        g2.setColor(textColor);
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        g2.drawString(b.text, b.x + 20, b.y + 35);
    }

    private ArrayList<MenuButton> getCurrentButtons() {
        return switch (menuState) {
            case 0 ->
                buttons;
            case 1 ->
                playButtons;
            case 3 ->
                settingButtons;
            default ->
                new ArrayList<>();
        };
    }

    // Update stars
    private void updateStars() {
        Random rand = new Random();
        int width = getWidth();
        int height = getHeight();
        for (Star s : stars) {
            s.y += s.speed;
            if (s.y > height) {
                s.y = 0;
                s.x = rand.nextInt(width);
            }
        }
    }

    // Hover animation
    private void updateHoverAnimation() {
        ArrayList<MenuButton> currentButtons = getCurrentButtons();
        for (int i = 0; i < currentButtons.size(); i++) {
            MenuButton b = currentButtons.get(i);
            if (i == hoveredIndex) {
                b.hoverProgress += 0.1f;
                if (b.hoverProgress > 1f) {
                    b.hoverProgress = 1f;
                }
            } else {
                b.hoverProgress -= 0.1f;
                if (b.hoverProgress < 0f) {
                    b.hoverProgress = 0f;
                }
            }
        }
    }

    // Mouse events
    @Override
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        hoveredIndex = -1;
        ArrayList<MenuButton> currentButtons = getCurrentButtons();
        for (int i = 0; i < currentButtons.size(); i++) {
            if (currentButtons.get(i).contains(p)) {
                hoveredIndex = i;
                selectedIndex = i;
                break;
            }
        }
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (menuState != 1) {
            triggerAction();
        } else if (selectedIndex == 2) {
            triggerAction();
        }
    }

    private void triggerAction() {
        ArrayList<MenuButton> currentButtons = getCurrentButtons();

        if (menuState == 0) { // Main menu
            if (hoveredIndex == 0) {
                menuState = 1;
            } else if (hoveredIndex == 1) {
                menuState = 2;
            } else if (hoveredIndex == 2) {
                menuState = 3;
            } else if (hoveredIndex == 3) {
                game.exitGame();
            }
            hoveredIndex = -1;
            selectedIndex = 0;
        } else if (menuState == 2) { // Hi-Score
            String clicked = hiScores.get(hoveredIndex);
            if (clicked.equals("Back")) {
                menuState = 0;
            } else if (clicked.equals("Reset")) {
                hiScores.clear();
            }
        } else if (menuState == 3) { // Settings
            if (hoveredIndex < difficulties.length) {
                currentDifficulty = hoveredIndex;
                for (int i = 0; i < difficulties.length; i++) {
                    String suffix = (i == currentDifficulty) ? " (Selected)" : "";
                    settingButtons.get(i).text = difficulties[i] + suffix;
                }
            } else if (hoveredIndex == settingButtons.size() - 2) { // Max Score
                maxScoreIndex++;
                if (maxScoreIndex >= maxScoreOptions.length) {
                    maxScoreIndex = 0;
                }
                settingButtons.get(hoveredIndex).text = "Max Score: " + maxScoreOptions[maxScoreIndex];
            } else if (hoveredIndex == settingButtons.size() - 1) {
                menuState = 0;
            }
        }
        repaint();
    }

    // Keyboard support
    @Override
    public void keyPressed(KeyEvent e) {
        ArrayList<MenuButton> currentButtons = getCurrentButtons();
        if (currentButtons.isEmpty()) {
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> {
                selectedIndex--;
                if (selectedIndex < 0) {
                    selectedIndex = currentButtons.size() - 1;
                }
            }
            case KeyEvent.VK_DOWN -> {
                selectedIndex++;
                if (selectedIndex >= currentButtons.size()) {
                    selectedIndex = 0;
                }
            }
            case KeyEvent.VK_ENTER -> {
                hoveredIndex = selectedIndex;
                triggerAction();
            }
            case KeyEvent.VK_SPACE -> {
                if (menuState == 1) {
                    if (selectedIndex == 0) {
                        game.startGame(false);
                    } else if (selectedIndex == 1) {
                        game.startGame(true);
                    } else if (selectedIndex == 2) {
                        menuState = 0;
                        hoveredIndex = -1;
                        selectedIndex = 0;
                    }
                }
            }
        }
        hoveredIndex = selectedIndex;
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    // Inner classes
    private static class MenuButton extends Rectangle {

        String text;
        float hoverProgress = 0f;

        public MenuButton(String text, int x, int y, int width, int height) {
            super(x, y, width, height);
            this.text = text;
        }
    }

    private static class Star {

        int x, y, speed;

        public Star(int x, int y, int speed) {
            this.x = x;
            this.y = y;
            this.speed = speed;
        }
    }
}
