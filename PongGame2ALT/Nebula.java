package PongGame2ALT;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.RadialGradientPaint;

public class Nebula {
    public float x, y, w, h, alpha, driftX, driftY;
    public Color color;

    public Nebula(float x, float y, float w, float h, Color color, float alpha, float driftX, float driftY) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.color = color;
        this.alpha = alpha;
        this.driftX = driftX;
        this.driftY = driftY;
    }

    public void draw(Graphics2D g) {
        int ix = Math.round(x - w / 2), iy = Math.round(y - h / 2);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        RadialGradientPaint rg = new RadialGradientPaint(
            new Point2D.Float(ix + w/2, iy + h/2),
            Math.max(w, h)/2f,
            new float[]{0f, 1f},
            new Color[]{new Color(color.getRed(), color.getGreen(), color.getBlue(), 180),
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), 0)}
        );
        g.setPaint(rg);
        g.fillOval(ix, iy, Math.round(w), Math.round(h));
        g.setComposite(AlphaComposite.SrcOver);
    }
}
