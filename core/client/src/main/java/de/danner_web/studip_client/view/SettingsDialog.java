package de.danner_web.studip_client.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import de.danner_web.studip_client.utils.ResourceLoader;
import de.danner_web.studip_client.utils.Template;
import de.danner_web.studip_client.view.components.buttons.ModernButton;

public class SettingsDialog extends JDialog {

	private static final long serialVersionUID = -7495155995090510875L;

	public SettingsDialog(JPanel settingsView) {
		// Hacks back the "x" button in Gnome3
		super(null, "", Dialog.ModalityType.MODELESS);

		// Set Icon
		try {
			BufferedImage image = ImageIO.read(ResourceLoader.getURL(Template.FAVICON));
			this.setIconImage(image);
		} catch (IOException e) {
			// Nichts tun
		}

		settingsView.setBackground(Color.WHITE);

		// Dialog settings
		this.setBackground(Color.WHITE);
		this.setLayout(new BorderLayout());
		this.setMinimumSize(settingsView.getMinimumSize());
		if(settingsView.getMinimumSize().equals(settingsView.getMaximumSize())){
			this.setResizable(false);
		}
		this.setMaximumSize(settingsView.getMaximumSize());
		this.setPreferredSize(settingsView.getPreferredSize());
		this.setModal(true);
		
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		this.add(settingsView, BorderLayout.CENTER);

		// Add OK Button
		ModernButton ok = new ModernButton("OK");
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SettingsDialog.this.dispatchEvent(new WindowEvent(SettingsDialog.this, WindowEvent.WINDOW_CLOSING));
			}
		});
		this.add(ok, BorderLayout.SOUTH);

		// Show
		pack();
	}
}
