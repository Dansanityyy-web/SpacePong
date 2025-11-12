package PongGame2ALT;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private SpacePong game;

    public SettingsPanel(SpacePong game) {
        this.game = game;
        setBackground(Color.DARK_GRAY);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("SETTINGS", 300, 50);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Difficulty: Slow | Normal | Fast | Ultra", 200, 100);
        g.drawString("Max Score: 10 | 20 | 30", 200, 140);
        g.drawString("Back", 200, 180);
    }
}
