package de.danner_web.studip_client.view.components.buttons;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;

import javax.swing.JToggleButton;
import javax.swing.Timer;

import de.danner_web.studip_client.utils.Template;

public class ModernToggleButton extends JToggleButton {

    private static final long serialVersionUID = -8803496625745769864L;
    private int buttonX;

    private final int TARGET_MAX = 18;
    private final int TARGET_MIN = 0;
    private final int CIRECLE_RADIUS = 8;
    private final int LINE_HEIGHT = 8;

    public ModernToggleButton(boolean enabled) {
        super();

        if (enabled) {
            buttonX = TARGET_MAX;
        } else {
            buttonX = TARGET_MIN;
        }
        super.setSelected(enabled);
        this.setPreferredSize(new Dimension(CIRECLE_RADIUS * 2 + TARGET_MAX, 20));
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(new Dimension(CIRECLE_RADIUS * 2 + TARGET_MAX, preferredSize.height));
    }

    public void setSelected(boolean selected) {
        super.setSelected(selected);
        final Timer tm = new Timer(20, null);
        tm.setInitialDelay(0);
        tm.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int target;
                if (isSelected()) {
                    target = TARGET_MAX;
                } else {
                    target = TARGET_MIN;
                }
                int diffX = target - buttonX;
                int stepX = 0;
                int defaultStepSize = 2;

                // If there is no difference either X -> stop
                if (diffX == 0) {
                    tm.stop();
                    return;
                }
                int partsX = Math.abs(diffX) / defaultStepSize;
                if (partsX > 0) {
                    stepX = diffX / partsX;
                } else {
                    stepX = diffX;
                }
                buttonX += stepX;
                repaint();
            }
        });
        tm.start();
    }

    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(getColor((float) buttonX / (float) TARGET_MAX, Template.COLOR_LIGHT_GRAY, Template.COLOR_ACCENT_LIGHT));
        g.fillRect(CIRECLE_RADIUS, getHeight() / 2 - (LINE_HEIGHT / 2), getWidth() - CIRECLE_RADIUS * 2, LINE_HEIGHT);

        // Left round
        Ellipse2D.Double pLeft = new Ellipse2D.Double(CIRECLE_RADIUS - LINE_HEIGHT / 2,
                getHeight() / 2 - LINE_HEIGHT / 2, LINE_HEIGHT, LINE_HEIGHT);
        g2d.fill(pLeft);

        // Right round
        Ellipse2D.Double pRight = new Ellipse2D.Double(getWidth() - CIRECLE_RADIUS - LINE_HEIGHT / 2,
                getHeight() / 2 - LINE_HEIGHT / 2, LINE_HEIGHT, LINE_HEIGHT);
        g2d.fill(pRight);

        // Track
        g.setColor(getColor((float) buttonX / (float) TARGET_MAX, Template.COLOR_GRAY, Template.COLOR_ACCENT));

        Ellipse2D.Double circle = new Ellipse2D.Double(buttonX, getHeight() / 2 - CIRECLE_RADIUS, CIRECLE_RADIUS * 2,
                CIRECLE_RADIUS * 2);
        g2d.fill(circle);

    }

    public Color getColor(float f, Color c1, Color c2) {
        f = Math.min(1.0f, f);
        int red = (int) (f * c2.getRed() + (1 - f) * c1.getRed());
        int green = (int) (f * c2.getGreen() + (1 - f) * c1.getGreen());
        int blue = (int) (f * c2.getBlue() + (1 - f) * c1.getBlue());
        return new Color(red, green, blue);
    }
}