package de.danner_web.studip_client.view.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

import de.danner_web.studip_client.utils.Template;

public class ModernSlider extends JSlider {

    private static final long serialVersionUID = 8477677251129229112L;

    public ModernSlider() {
        this(HORIZONTAL, 0, 100, 50);
    }

    public ModernSlider(int orientation) {
        this(HORIZONTAL, 0, 100, 50);
    }

    public ModernSlider(int min, int max) {
        this(HORIZONTAL, min, max, (min + max) / 2);
    }

    public ModernSlider(int min, int max, int value) {
        this(HORIZONTAL, min, max, value);
    }

    public ModernSlider(int orientation, int min, int max, int value) {
        this.orientation = JSlider.HORIZONTAL;
        sliderModel = new DefaultBoundedRangeModel(value, 0, min, max);
        sliderModel.addChangeListener(changeListener);
        updateUI();
        init();
    }

    public ModernSlider(BoundedRangeModel brm) {
        this.orientation = JSlider.HORIZONTAL;
        setModel(brm);
        sliderModel.addChangeListener(changeListener);
        updateUI();
        init();
    }

    private void init() {
        this.setUI(new CustomSliderUI(this));
        this.setFocusable(false);
    }

    @Override
    public void setOrientation(int o) {
        // Not supported
    }

    private class CustomSliderUI extends BasicSliderUI {

        final int THUMB_SIZE = 13;

        public CustomSliderUI(JSlider b) {
            super(b);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(THUMB_SIZE, THUMB_SIZE);
        }

        @Override
        public void paintTrack(Graphics g) {
            Color saved_color = g.getColor();

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int height = (thumbRect.height / 5 == 0) ? 1 : thumbRect.height / 5;
            int width;

            // Blue track
            width = trackRect.width - (trackRect.width - thumbRect.x);
            Point a = new Point(trackRect.x, trackRect.y + 1);
            a.translate(0, (trackRect.height / 2) - (height / 2));
            g.setColor(Template.COLOR_ACCENT);
            g.fillRect(a.x, a.y, width, height);

            // Left round
            Ellipse2D.Double pLeft = new Ellipse2D.Double(a.x - height/2, a.y, height, height);
            g2d.fill(pLeft);

            // Gray Track
            width = trackRect.width - thumbRect.x;
            a = new Point(thumbRect.x + (THUMB_SIZE / 2), trackRect.y + 1);
            a.translate(0, (trackRect.height / 2) - (height / 2));
            g.setColor(Template.COLOR_LIGHTER_GRAY);
            g.fillRect(a.x, a.y, width, height);

            // Right round
            Ellipse2D.Double pRight = new Ellipse2D.Double(a.x + width - height/2, a.y, height, height);
            g2d.fill(pRight);

            g.setColor(saved_color);
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Template.COLOR_ACCENT);

            Ellipse2D.Double circle = new Ellipse2D.Double(thumbRect.x,
                    thumbRect.y + (thumbRect.height / 2) - (THUMB_SIZE / 2), THUMB_SIZE, THUMB_SIZE);
            g2d.fill(circle);
        }
    }
}
