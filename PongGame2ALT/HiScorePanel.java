package PongGame2ALT;

import javax.swing.*;
import java.awt.*;

public class HiScorePanel extends JPanel {
    private SpacePong game;

    public HiScorePanel(SpacePong game) {
        this.game = game;
        setBackground(Color.BLACK);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("HIGH SCORES", 300, 50);

        // Example scores
        g.drawString("P1: 10", 300, 100);
        g.drawString("P2: 3", 300, 140);
    }
}
