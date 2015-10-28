package de.danner_web.studip_client.view.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.kitfox.svg.app.beans.SVGIcon;

import de.danner_web.studip_client.utils.ResourceLoader;
import de.danner_web.studip_client.utils.Template;

public class MainMenuEntryPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1138457346982997533L;

	private static final int HEIGHT = 65;

	private JLabel text;

	private JLabel icon;

	private boolean selected;

	public MainMenuEntryPanel(ResourceBundle resourceBundle) {
		this("");

	}

	public MainMenuEntryPanel(String text) {
		this(text, null);
	}

	public MainMenuEntryPanel(String text, String iconURL) {
		super();
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.text = new JLabel(text);
		this.text.setForeground(Color.WHITE);
		this.text.setFont(new Font(this.text.getFont().getFamily(), Font.BOLD,
				this.text.getFont().getSize() + 2));

		this.icon = new JLabel();
		this.icon.setOpaque(false);
		this.icon.setPreferredSize(new Dimension(HEIGHT - 10, HEIGHT - 10));
		if (icon != null) {
			SVGIcon svgicon = ResourceLoader.getSVGIcon(iconURL);
			svgicon.setPreferredSize(new Dimension(HEIGHT - 30, HEIGHT - 30));
			this.icon.setIcon(svgicon);
		}
		this.icon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		this.add(this.icon);
		this.add(this.text);

		this.setBackground(Template.COLOR_ACCENT);

		repaint();
	}
	
	public void setText(String text){
		this.text.setText(text);
		repaint();
	}

	public void select(boolean select) {
		this.selected = select;
		this.setOpaque(select);
	}

	public boolean isSelected() {
		return selected;
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(this.getPreferredSize().width, HEIGHT);

	}

}
