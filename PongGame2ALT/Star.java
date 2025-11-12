package PongGame2ALT;

import java.awt.*;

public class Star {
    public float x, y, size, speed;
    public Color color;
    public int layer;

    public Star(float x, float y, float size, float speed, Color color, int layer) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = speed;
        this.color = color;
        this.layer = layer;
    }

    public void draw(Graphics2D g) {
        int halo = (int)(size * 4);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f + layer * 0.05f));
        g.setColor(new Color(255, 255, 255, Math.min(220, color.getAlpha())));
        g.fillOval(Math.round(x - halo/2), Math.round(y - halo/2), halo, halo);
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(color);
        g.fillOval(Math.round(x - size/2), Math.round(y - size/2), Math.max(1, Math.round(size)), Math.max(1, Math.round(size)));
    }
}
