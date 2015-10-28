package de.danner_web.studip_client.view.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.danner_web.studip_client.data.AcceptPluginMessage;
import de.danner_web.studip_client.data.PluginMessage;
import de.danner_web.studip_client.utils.ResourceLoader;
import de.danner_web.studip_client.utils.Template;
import de.danner_web.studip_client.view.components.buttons.ModernButton;

/**
 * This class constructs the news dialog with the clicked news on it, it renders
 * the new's body in html
 * 
 * @author Philipp
 *
 */
public class PluginMessageDetailsView extends JDialog {

	private static final long serialVersionUID = -1438615198174407287L;

	// Default size
	private static final int HEIGHT = 300;
	private static final int WIDTH = 500;

	private PluginMessage message;

	public PluginMessageDetailsView(final PluginMessage message) {
		// Hacks back the "x" button in Gnome3
		super(null, "", Dialog.ModalityType.MODELESS);
		this.message = message;

		// Set Icon
		try {
			BufferedImage image = ImageIO.read(ResourceLoader.getURL(Template.FAVICON));
			this.setIconImage(image);
		} catch (IOException e) {
			// Nichts tun
		}

		// Default size and center
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setSize(new Dimension(WIDTH, HEIGHT));
		setLocationRelativeTo(null);

		// Content
		setTitle("Ankündigungsdetails");
		add(createHeader(), BorderLayout.NORTH);
		add(createBody(), BorderLayout.CENTER);

		// AcceptButton
		if (message instanceof AcceptPluginMessage) {
			ModernButton acceptButton = new ModernButton("Ok");
			acceptButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					((AcceptPluginMessage) message).getAcceptListener().onClick(message);
					close();
				}
			});
			add(acceptButton, BorderLayout.SOUTH);
		}

		pack();
		setVisible(true);
	}

	/**
	 * private method to create the header info as JPanel for the deatil view of
	 * a news
	 * 
	 * @return JPanel with topic, author and time on it
	 */
	private JPanel createHeader() {
		JPanel header = new JPanel() {
			private static final long serialVersionUID = -7595565968707652304L;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				g.setColor(Template.COLOR_LIGHTER_GRAY);
				g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
			}
		};
		header.setBackground(Color.WHITE);
		header.setLayout(new BorderLayout());
		header.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel left = new JPanel();
		left.setBackground(Color.WHITE);
		left.add(new JLabel(message.getHeader()));

		header.add(left, BorderLayout.LINE_START);

		return header;
	}

	/**
	 * private method to create the body info as LightScrollPane for the deatil
	 * view of a news
	 * 
	 * @return LightScrollPane with body rendered html content
	 */
	private JScrollPane createBody() {
		JEditorPane body = new JEditorPane();
		body.setContentType("text/html");
		body.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		body.setMargin(new Insets(15, 15, 15, 15));
		body.setText(message.getText());

		// enable hyperlinks
		body.setEditable(false);
		body.setEnabled(true);
		body.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent hle) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.browse(hle.getURL().toURI());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		return new ModernScrollPane(body);
	}

	private void close() {
		this.dispose();
	}
}
