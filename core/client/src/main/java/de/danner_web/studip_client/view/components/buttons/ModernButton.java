package de.danner_web.studip_client.view.components.buttons;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import de.danner_web.studip_client.utils.Template;

public class ModernButton extends JButton {

	private static final long serialVersionUID = 3081968117025057228L;

	public ModernButton(String string) {
		super(string);
		setBorder(new CompoundBorder(new LineBorder(Template.COLOR_LIGHT_GRAY), new EmptyBorder(5, 5, 5, 5)));
		setForeground(Color.WHITE);
		setUI(new BasicButtonUI());
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (this.model.isRollover()) {
			this.setBackground(Template.COLOR_LIGHT_GRAY);
			this.setForeground(Template.COLOR_DARK);
		} else {
			this.setBackground(Template.COLOR_LIGHTER_GRAY);
			this.setForeground(Template.COLOR_DARK);
		}
		if (this.model.isPressed() | this.model.isSelected() | this.model.isArmed()) {
			this.setBackground(Template.COLOR_ACCENT);
			this.setForeground(Color.WHITE);
		}
	}
}
