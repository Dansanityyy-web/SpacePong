package PongGame2ALT;

import java.awt.*;

public class Paddle {

    // Position and size
    public int x, y;
    public int width, height;

    // Movement speed
    private int speed;

    // Paddle color
    private Color color = Color.WHITE; // default

    // Constructor
    public Paddle(int x, int y, int width, int height, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    // Set paddle color
    public void setColor(Color c) {
        this.color = c;
    }

    public Color getColor() {
        return color;
    }

    // Movement logic (up/down)
    public void move(int dy, int panelHeight) {
        y += dy * speed;
        if (y < 0) y = 0;
        else if (y + height > panelHeight) y = panelHeight - height;
    }

    public void moveUp() {
        y -= speed;
        if (y < 0) y = 0;
    }

    public void moveDown(int panelHeight) {
        y += speed;
        if (y + height > panelHeight) y = panelHeight - height;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void aiMove(int targetY, int panelHeight, int speed) {
        if (targetY < y + height / 2) y -= speed;
        else if (targetY > y + height / 2) y += speed;

        if (y < 0) y = 0;
        if (y + height > panelHeight) y = panelHeight - height;
    }

    // Draw paddle with its assigned color
    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }

    // Getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
