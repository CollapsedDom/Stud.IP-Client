package de.danner_web.studip_client.view.components.buttons;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import de.danner_web.studip_client.utils.Template;

public class PluginMessageActionButton extends JButton {

	private static final long serialVersionUID = 3081968117025057228L;

	public PluginMessageActionButton(String string) {
		super(string);
		setBorder(new LineBorder(Color.WHITE));
		setForeground(Color.WHITE);
		setUI(new BasicButtonUI());
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (this.model.isRollover()) {
			this.setBackground(Color.WHITE);
			this.setForeground(Template.COLOR_DARK);
			setOpaque(true);
			setContentAreaFilled(true);
		} else {
			this.setForeground(Color.WHITE);
			setOpaque(false);
			setContentAreaFilled(false);
		}
		if (this.model.isPressed() | this.model.isSelected()
				| this.model.isArmed()) {
			this.setBackground(new Color(200, 200, 200));
			this.setForeground(Template.COLOR_DARK);
			setOpaque(true);
			setContentAreaFilled(true);
		}
	}
}
