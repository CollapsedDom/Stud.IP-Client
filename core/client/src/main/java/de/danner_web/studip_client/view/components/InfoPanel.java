package de.danner_web.studip_client.view.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JWindow;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import de.danner_web.studip_client.data.AcceptPluginMessage;
import de.danner_web.studip_client.data.ClickListener;
import de.danner_web.studip_client.data.PluginMessage;
import de.danner_web.studip_client.utils.Template;
import de.danner_web.studip_client.view.components.buttons.PluginMessageActionButton;

public class InfoPanel extends JWindow {

	private static final long serialVersionUID = 2073021697517588194L;

	private static final int PADDING_TOP_BOTTOM = 5;
	private static final int PADDING_LEFT_RIGHT = 8;
	public static final int WIDTH = 400;
	private boolean fromTop;
	
	public Point target;
	private Timer tm;

	private static final int MAX_CHARS_BODY = 280;

	private PluginMessage message;

	public InfoPanel(PluginMessage message, Point p,
			boolean fromTop) {
		super();
		this.fromTop = fromTop;
		this.message = message;
		buildGui(p);
	}

	public void moveToLocation(final Point p) {
		
		this.target = p;
		if (!fromTop) {
			p.setLocation(p.x, p.y - getPreferredSize().height);
		}
		if(tm != null && tm.isRunning()){
			tm.stop();
		}
		tm = new Timer(20, null);
		tm.setInitialDelay(0);
		tm.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				int diffY = p.y - getY();
				int diffX = p.x - getX();

				int stepY = 0;
				int stepX = 0;

				int defaultStepSize = 5;

				// If there is no difference either X and Y -> stop
				if (diffY == 0 && diffX == 0) {
					tm.stop();
					return;
				}

				int partsX = Math.abs(diffX) / defaultStepSize;
				if (partsX > 0) {
					stepX = diffX / partsX;
				} else {
					stepX = diffX;
				}
				int partsY = Math.abs(diffY) / defaultStepSize;
				if (partsY > 0) {
					stepY = diffY / partsY;
				} else {
					stepY = diffY;
				}

				Point step = new Point(getX() + stepX, getY() + stepY);

				setLocation(step);
				repaint();
			}
		});
		tm.start();
	}

	public boolean hasMessage(PluginMessage message) {
		return this.message.equals(message);
	}

	private void buildGui(Point p) {
		// Define Look
		setBackground(Template.COLOR_DARK);
		setAlwaysOnTop(true);

		// Set backgroundPane with rounded corner
		JPanel back = new JPanel();
		back.setBackground(Template.COLOR_DARK);
		back.setBorder(new EmptyBorder(PADDING_TOP_BOTTOM, PADDING_LEFT_RIGHT,
				PADDING_TOP_BOTTOM, PADDING_LEFT_RIGHT));
		back.setLayout(new BorderLayout());

		// Set content
		JLabel header = new JLabel(message.getHeader());
		header.setOpaque(false);
		header.setForeground(Color.WHITE);
		header.setFont(header.getFont().deriveFont(
				header.getFont().getStyle() | Font.BOLD));
		header.setFont(header.getFont().deriveFont(14.0f));

		// strip away html
		final Pattern REMOVE_TAGS = Pattern.compile("<.+?>");
		String bodyUnExcaped = message.getText();
		Matcher m = REMOVE_TAGS.matcher(bodyUnExcaped);
		String body = m.replaceAll("");
		
		if (body.length() > MAX_CHARS_BODY){
			body = body.substring(0, MAX_CHARS_BODY) + " [...]";
			message.setShowDetailView(true);
		}
			
		JTextPane textMessage = new JTextPane();
		textMessage.setText(body);
		textMessage.setEditable(false);
		textMessage.setOpaque(false);
		textMessage.setEnabled(true);
		textMessage.setForeground(Color.WHITE);
		textMessage.setPreferredSize(new Dimension(WIDTH
				- (2 * PADDING_LEFT_RIGHT), getContentHeight(body)));

		MouseAdapter mouse = new DefaultMouseAdapter();
		back.addMouseListener(mouse);
		textMessage.addMouseListener(mouse); // Workaround

		back.add(header, BorderLayout.NORTH);
		back.add(textMessage, BorderLayout.CENTER);

		if(message instanceof AcceptPluginMessage){
			PluginMessageActionButton acceptButton = new PluginMessageActionButton("Ok");
			acceptButton.addMouseListener(new AcceptMouseAdapter());
			back.add(acceptButton, BorderLayout.SOUTH);
		}
		
		setContentPane(back);

		if (!fromTop) {
			p.setLocation(p.x, p.y - getPreferredSize().height);
		}
		setLocation(p);

		pack();
		setVisible(true);
	}

	private static int getContentHeight(String content) {
		JEditorPane dummyEditorPane = new JEditorPane();
		dummyEditorPane.setSize(WIDTH - (2 * PADDING_LEFT_RIGHT),
				Short.MAX_VALUE);
		dummyEditorPane.setText(content);
		return dummyEditorPane.getPreferredSize().height;
	}

	class DefaultMouseAdapter extends MouseAdapter {

		@Override
		public void mouseReleased(MouseEvent e) {
			
			// Performs the method onClick() in MessageListener
			if (message.getMessageListener() != null) {
				for (ClickListener listener : message.getMessageListener()) {
					listener.onClick(message);
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			getContentPane().setBackground(Template.COLOR_DARK_GRAY);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			getContentPane().setBackground(Template.COLOR_DARK);
		}

	};
	
	class AcceptMouseAdapter extends DefaultMouseAdapter {

		@Override
		public void mouseReleased(MouseEvent e) {
			message.setShowDetailView(false);
			((AcceptPluginMessage) message).getAcceptListener().onClick(message);
			super.mouseReleased(e);
		}
	};
}
