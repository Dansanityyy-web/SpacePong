package PongGame2ALT;

import javax.swing.*;
import java.awt.*;

public class SpacePong extends JFrame {

    private GamePanel gamePanel;

    public SpacePong() {
        setTitle("Space Pong");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new GamePanel();
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void startGame(boolean singlePlayer) {
    if (gamePanel != null) { // <- safety check
        gamePanel.setSinglePlayer(singlePlayer);
        gamePanel.resetGame();
        gamePanel.requestFocusInWindow();
    }
}

    public void exitGame() {
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpacePong::new);
    }
}
