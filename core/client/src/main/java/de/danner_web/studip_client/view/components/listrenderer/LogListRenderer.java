package de.danner_web.studip_client.view.components.listrenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import de.danner_web.studip_client.data.LogItem;
import de.danner_web.studip_client.utils.Template;

public class LogListRenderer extends JPanel implements
		ListCellRenderer<LogItem> {

	private static final long serialVersionUID = -3708927617569014332L;

	public static final String HTML_1 = "<html><body style='width: ";
	public static final String HTML_2 = "px'>";
	public static final String HTML_3 = "</html>";

	private final JLabel time;
	private final JLabel content;

	int width;

	public LogListRenderer(int width) {
		this.width = width;

		setOpaque(true);
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		setLayout(new BorderLayout());

		time = new JLabel("", SwingConstants.RIGHT);
		time.setForeground(Color.LIGHT_GRAY);

		content = new JLabel();
		content.setForeground(Template.COLOR_DARK_GRAY);

		add(time, BorderLayout.NORTH);
		add(content, BorderLayout.SOUTH);
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends LogItem> list, LogItem value, int index,
			boolean isSelected, boolean cellHasFocus) {

		// Set time
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		Date dt = new Date(Long.valueOf(value.getDate().getTime()));
		String formatedTime = sdf.format(dt);
		time.setText(formatedTime);

		// Set content
		String text = HTML_1 + String.valueOf(width) + HTML_2
				+ value.getPluginName() + ": " + value.getContent() + HTML_3;
		content.setText(text);
		return this;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Template.COLOR_LIGHTER_GRAY);
		g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
	}
}
