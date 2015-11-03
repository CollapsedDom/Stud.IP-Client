package de.danner_web.studip_client.view.subframe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.kitfox.svg.app.beans.SVGIcon;

import de.danner_web.studip_client.model.SettingsModel;
import de.danner_web.studip_client.model.Model;
import de.danner_web.studip_client.utils.ResourceLoader;
import de.danner_web.studip_client.utils.Template;
import de.danner_web.studip_client.view.components.ModernScrollPane;

public class AboutView extends JPanel {

	private static final long serialVersionUID = 3113818346708185964L;

	private SettingsModel settings;

	private JPanel info;
	private JScrollPane legal;

	private JLabel version, logo;

	public AboutView(Model model) {
		this.settings = model.getSettingsModel();
		createView();
	}

	private void createView() {
		this.setBackground(Color.WHITE);
		this.setLayout(new BorderLayout());

		// Info panel
		info = new JPanel() {
			private static final long serialVersionUID = -5117047180924520551L;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				g.setColor(Template.COLOR_LIGHTER_GRAY);
				g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
			}
		};
		info.setBorder(new EmptyBorder(10, 10, 10, 10));
		info.setLayout(new BorderLayout(10, 10));
		info.setBackground(Color.WHITE);

		// Version number
		version = new JLabel("version " + settings.getVersion(), SwingConstants.CENTER);

		// Logo
		this.logo = new JLabel("Client", SwingConstants.CENTER);
		this.logo.setFont(this.logo.getFont().deriveFont(25.0f));
		this.logo.setOpaque(false);
		this.logo.setPreferredSize(new Dimension(243, 83));
		if (logo != null) {
			SVGIcon icon = ResourceLoader.getSVGIcon(Template.LOGO);
			icon.setPreferredSize(new Dimension(243, 83));
			this.logo.setIcon(icon);
		}

		info.add(logo, BorderLayout.PAGE_START);
		info.add(version, BorderLayout.PAGE_END);

		// Legal panel
		JTextArea legalText = new JTextArea("Der Stud.IP Client\n\n"
				+ "Das Herunterladen neuer Vorlesungsfolien und Übungsblätter aus verschiedenen Veranstaltungen gehört bei vielen Studierenden zum Tagesablauf. Der Stud.IP Client bietet eine Plattform, welche diese Dateien schnell, automatisch und direkt auf den Desktoprechner des Studierenden lädt. Außerdem informiert der Client über neue, wichtige Ankündigungen. Aufgrund des flexiblen Designs der Anwendung, sind verschieden Erweiterungen, wie eine Reaktionsmöglichkeit auf Nachrichten oder Foreneinträge, denkbar. Die Stud.IP Client Anwendung bietet so eine optimierte Verteilung von Information und Lehrmaterial mit Stud.IP.\n"
				+ "\n\n\n" + "Entwickler\n\n" + "Philipp Danner\n" + "Dominik Danner\n"
				+ "unterstützt von den Entwicklern des InteLeC-Zentrums der Uni Passau" + "\n\n\n" + "Lizenz:\n\n");
		legalText.setEditable(false);
		legalText.setLineWrap(true);
		legalText.setWrapStyleWord(true);
		legalText.setMargin(new Insets(15, 15, 15, 15));
		BufferedReader in = null;
		try {
			InputStream is = getClass().getResourceAsStream("/LICENSE.txt");
			in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = in.readLine();
			while (line != null) {
				legalText.append(line + "\n");
				line = in.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		JTextPane donatePane = new JTextPane();
		donatePane.setContentType("text/html");
		donatePane.setEditable(false);
		donatePane.setText("<img src='https://www.paypalobjects.com/de_DE/DE/i/btn/btn_donate_LG.gif' alt='Spenden'>");
		donatePane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.browse(new URI("http://studip-client.danner-web.de/donate/"));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		legal = new ModernScrollPane(legalText);

		add(info, BorderLayout.NORTH);
		add(legal, BorderLayout.CENTER);
		add(donatePane, BorderLayout.SOUTH);
	}
}
