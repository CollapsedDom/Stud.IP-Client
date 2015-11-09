package de.danner_web.studip_client.view;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.model.Model;
import de.danner_web.studip_client.utils.ResourceLoader;
import de.danner_web.studip_client.utils.Template;
import de.danner_web.studip_client.view.components.TrayIconPopMenu;

public class ViewController implements NavigationListener {

	/**
	 * Logger dieser Klasse.
	 */
	private static Logger logger = LogManager.getLogger(ViewController.class);

	private Model model;

	private LoginWindow loginWindow;

	private MainWindow mainWindow;

	private TrayIcon appTrayIcon;

	public ViewController(Model model) {
		this.model = model;

		boolean trayIcon = createTrayIcon();
		if (!model.getSettingsModel().isHidden()) {
			navigateToPlugins();
		} else {
			if (!trayIcon) {
				navigateToPlugins();
				// If no Tray Icon is set -> only minimize MainWindow
				mainWindow.setExtendedState(JFrame.ICONIFIED);
			}
		}
		new InfoController(model);
	}

	/**
	 * This method tries to create a TrayIcon
	 * 
	 * if success, a TrayIcon should appear at SystemTrayIcons otherwise this
	 * method exits with false
	 * 
	 * @return true, if a TrayIcon was successfully created, otherwise false
	 */
	private boolean createTrayIcon() {
		boolean trayIconOn = false;
		if (!SystemTray.isSupported()) {
			navigateToSettings();
			// deactivate close
		} else {
			// Set Icon
			BufferedImage image = null;
			try {
				image = ImageIO.read(ResourceLoader.getURL(Template.FAVICON));
			} catch (IOException e) {
				// Nichts tun
			}

			Properties prop = System.getProperties();
			if (("gnome").equals(prop.getProperty("sun.desktop"))) {
				logger.info("This Machine uses Gnome Desktop Environment");
			} else {

				TrayIconPopMenu menu = new TrayIconPopMenu(model);
				menu.addNavigationListener(this);

				int trayIconWidth = new TrayIcon(image).getSize().width;
				appTrayIcon = new TrayIcon(image.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH));
				appTrayIcon.setPopupMenu(menu);
				appTrayIcon.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {
							navigateToPlugins();
						}
					}
				});

				try {
					SystemTray.getSystemTray().add(appTrayIcon);
				} catch (AWTException e) {
					logger.info("TrayIcon could not be added.");
				}
				trayIconOn = true;
			}
		}
		return trayIconOn;
	}

	/**
	 * Schließt alle offenen Fenster und zeigt das LoginWindow an
	 */
	private void navigateToLogin() {
		logger.entry();

		if (mainWindow != null) {
			mainWindow.detacheView();
			mainWindow.dispose();
			mainWindow = null;
		}

		loginWindow = new LoginWindow(model);
		loginWindow.addNavigationListener(this);
		logger.exit();
	}

	private void navigateToSettings() {
		logger.entry();

		if (loginWindow != null) {
			loginWindow.detacheView();
			loginWindow.dispose();
			loginWindow = null;
		}
		if (mainWindow == null) {
			mainWindow = new MainWindow(model);
			mainWindow.addNavigationListener(this);
		}
		mainWindow.switchToSettings();
		logger.exit();
	}

	private void navigateToPlugins() {
		logger.entry();

		if (loginWindow != null) {
			loginWindow.detacheView();
			loginWindow.dispose();
			loginWindow = null;
		}

		if (mainWindow == null) {
			mainWindow = new MainWindow(model);
			mainWindow.addNavigationListener(this);
		}
		mainWindow.switchToPlugins();

		logger.exit();
	}

	private void navigateToLogs() {
		logger.entry();

		if (loginWindow != null) {
			loginWindow.detacheView();
			loginWindow.dispose();
			loginWindow = null;
		}

		if (mainWindow == null) {
			mainWindow = new MainWindow(model);
			mainWindow.addNavigationListener(this);
		}
		mainWindow.switchToLogs();

		logger.exit();
	}

	private void navigateToAbout() {
		logger.entry();

		if (loginWindow != null) {
			loginWindow.detacheView();
			loginWindow.dispose();
			loginWindow = null;
		}

		if (mainWindow == null) {
			mainWindow = new MainWindow(model);
			mainWindow.addNavigationListener(this);
		}
		mainWindow.switchToAbout();

		logger.exit();
	}

	/**
	 * Wird aufgerufen, wenn in einer View eine Action performed wird.
	 */
	public void actionPerformed(NavigationAction a) {
		logger.entry(a);
		switch (a) {
		case LOGIN_TO_SETTINGS:
			navigateToSettings();
			break;
		case LOGIN_TO_ABOUT:
			navigateToAbout();
			break;
		case LOGIN_TO_PLUGINS:
			navigateToPlugins();
			break;
		case LOGIN_TO_LOGS:
			navigateToLogs();
			break;
		case MAIN_TO_LOGIN:
			navigateToLogin();
			break;
		case INFO_TO_SETTINGS:
			navigateToSettings();
			break;
		case INFO_TO_PLUGINS:
			navigateToPlugins();
			break;
		case INFO_TO_LOGS:
			navigateToLogs();
			break;
		case INFO_TO_ABOUT:
			navigateToAbout();
			break;
		case CLOSE:
			SystemTray.getSystemTray().remove(appTrayIcon);
			System.exit(0);
			break;
		case CLOSE_MAIN:
			if (appTrayIcon == null) {
				// If no Tray Icon is set -> only minimize MainWindow
				mainWindow.setExtendedState(JFrame.ICONIFIED);
			} else {
				if (mainWindow != null) {
					mainWindow.detacheView();
					mainWindow.dispose();
					mainWindow = null;
				}
			}
			break;
		default:
			logger.debug("An error occured: unknown Action " + a.toString());
			break;
		}
		logger.exit();
	}

}
