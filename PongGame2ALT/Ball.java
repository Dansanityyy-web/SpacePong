package PongGame2ALT;

import java.awt.*;

public class Ball {
    public float x, y;
    public int diameter;
    public float xSpeed, ySpeed;
    public float speedMultiplier = 1.0f;

    public Ball(float x, float y, int diameter, float speed) {
        this.x = x;
        this.y = y;
        this.diameter = diameter;
        this.xSpeed = speed;
        this.ySpeed = speed;
    }

    public void reset(float x, float y, int diameter, float speed) {
        this.x = x;
        this.y = y;
        this.diameter = diameter;
        this.xSpeed = speed;
        this.ySpeed = speed;
        this.speedMultiplier = 1.0f;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.fillOval((int)x, (int)y, diameter, diameter);
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, diameter, diameter);
    }
}
