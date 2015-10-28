package de.danner_web.studip_client.view.components.buttons;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;

import de.danner_web.studip_client.utils.ResourceLoader;
import de.danner_web.studip_client.utils.Template;

public class BackButton extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6151848541312977281L;

	private Image arrow_back, arrow_back_hover;

	public BackButton() {
		super();
		this.setContentAreaFilled(false);
		this.setBorderPainted(false);
		this.setFocusPainted(false);

		try {
			arrow_back = ImageIO.read(ResourceLoader.getURL(Template.BACK_BUTTON));
			arrow_back_hover = ImageIO.read(ResourceLoader.getURL(Template.BACK_BUTTON_HOVER));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension dim = new Dimension(45, 45);
		if (arrow_back != null && arrow_back_hover != null) {
			dim.height = arrow_back.getHeight(null);
			dim.width = arrow_back.getWidth(null);
		}
		return dim;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (arrow_back == null || arrow_back_hover == null) {
			g.drawString("back", 0, 0);
		} else {
			if (this.model.isRollover()) {
				g.drawImage(arrow_back_hover, 0, 0, null);
			} else {
				g.drawImage(arrow_back, 0, 0, null);
			}
		}

	}

}