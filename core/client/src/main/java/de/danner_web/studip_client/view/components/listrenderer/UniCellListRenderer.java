package de.danner_web.studip_client.view.components.listrenderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import de.danner_web.studip_client.data.OAuthServer;
import de.danner_web.studip_client.utils.Template;

public class UniCellListRenderer extends JLabel implements
		ListCellRenderer<OAuthServer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UniCellListRenderer() {
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends OAuthServer> list, OAuthServer value, int index,
			boolean isSelected, boolean cellHasFocus) {
		setText(value.toString());
		setOpaque(true);
		setPreferredSize(new Dimension(100, 45));
		setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

		Color background;
		Color foreground;

		// check if this cell represents the current DnD drop location
		JList.DropLocation dropLocation = list.getDropLocation();
		if (dropLocation != null && !dropLocation.isInsert()
				&& dropLocation.getIndex() == index) {

			background = Color.WHITE;
			foreground = Color.RED;

			// check if this cell is selected
		} else if (isSelected) {
			background = Template.COLOR_ACCENT;
			foreground = Color.WHITE;

			// unselected, and not the DnD drop location
		} else {
			background = Color.WHITE;
			foreground = Color.BLACK;
		}

		setBackground(background);
		setForeground(foreground);

		return this;
	}
}
