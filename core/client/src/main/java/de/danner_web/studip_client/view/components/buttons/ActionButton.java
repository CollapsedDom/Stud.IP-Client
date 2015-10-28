package de.danner_web.studip_client.view.components.buttons;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JButton;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.app.beans.SVGIcon;

import de.danner_web.studip_client.utils.ResourceLoader;
import de.danner_web.studip_client.utils.Template;

public class ActionButton extends JButton {

    private static final long serialVersionUID = 660817787015769043L;

    // private Image imgDefault, imgHover, imgInactive;
    private SVGIcon icon;

    private static final int HEIGHT = 28;
    private static final int WIDTH = HEIGHT;

    private boolean active = false;

    public enum ActionType {
        DELETE, ADD, SETTINGS, ACTIVATE
    }

    public ActionButton(ActionType type) {
        super();
        this.setContentAreaFilled(false);
        this.setBorderPainted(false);
        this.setFocusPainted(false);

        // Load icon for type
        switch (type) {
        case DELETE:
            icon = ResourceLoader.getSVGIcon(Template.PLUGIN_DELETE);
            break;
        case SETTINGS:
            icon = ResourceLoader.getSVGIcon(Template.PLUGIN_SETTINGS);
            break;
        case ADD:
            icon = ResourceLoader.getSVGIcon(Template.PLUGIN_ADD);
            break;
        case ACTIVATE:
            icon = ResourceLoader.getSVGIcon(Template.PLUGIN_ACTIVATE);
            break;
        default:
            break;
        }

        icon.setPreferredSize(getPreferredSize());
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    public boolean isActivated() {
        return active;
    }

    private void setColor(Color color) {
        SVGElement node = SVGCache.getSVGUniverse().getDiagram(icon.getSvgURI()).getRoot();
        try {
            if (node.hasAttribute("fill", AnimationElement.AT_XML)) {
                node.setAttribute("fill", AnimationElement.AT_XML, String.format("#%06x", (0xFFFFFF & color.getRGB())));
            }
        } catch (SVGElementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (icon == null) {
            g.drawString("error", 0, 0);
        } else {
            if (this.model.isRollover() || active) {
                setColor(Template.COLOR_ACCENT);
                icon.paintIcon(this, g, 0, 0);
            } else if (!isEnabled()) {
                setColor(Template.COLOR_LIGHT_GRAY);
                icon.paintIcon(this, g, 0, 0);
            } else {
                setColor(Template.COLOR_DARK_GRAY);
                icon.paintIcon(this, g, 0, 0);
            }
        }
    }
}