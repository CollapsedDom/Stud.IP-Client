package de.danner_web.studip_client.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.ColorUIResource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.model.Model;
import de.danner_web.studip_client.utils.ResourceLoader;
import de.danner_web.studip_client.utils.Template;
import de.danner_web.studip_client.view.components.MainMenuEntryPanel;
import de.danner_web.studip_client.view.subframe.AboutView;
import de.danner_web.studip_client.view.subframe.GeneralSettingsView;
import de.danner_web.studip_client.view.subframe.LogView;
import de.danner_web.studip_client.view.subframe.PluginView;

public class MainWindow extends JFrame implements Observer {

	private static final long serialVersionUID = -5495609127207155396L;

	/**
	 * Logger dieser Klasse.
	 */
	private static Logger logger = LogManager.getLogger(MainWindow.class);

	private static final String PREFIX = "de.danner_web.studip_client.view.MainWindow.";

	private static final String MAIN_WINDOW_TITLE = PREFIX + "MainWindowTitle";
	private static final String MENU_SETTINGS_LABEL = PREFIX + "menuSettings";
	private static final String MENU_LOGGER_LABEL = PREFIX + "menuLogger";
	private static final String MENU_ABOUT_LABEL = PREFIX + "menuAbout";
	private static final String MENU_PLUGINS_LABEL = PREFIX + "menuPlugins";

	public static final int WINDOW_HIGHT = 580;
	public static final int WINDOW_WIDTH = 800;
	public static final int NAVIGATION_WIDTH = 220;

	private JSplitPane splitPane;

	private MainMenuEntryPanel generalSettings, about, log, plugins;

	private Model model;
	private ResourceBundle resourceBundle;

	private EventListenerList listeners = new EventListenerList();

	MainWindow(Model model) {
		super();

		this.model = model;
		this.resourceBundle = model.getResourceBundle(model.getCurrentLocale());

		UIManager.put("ComboBox.background", new ColorUIResource(Color.WHITE));
		
		// Set JTree icons
		UIManager.put("Tree.collapsedIcon", ResourceLoader.getSVGIcon(Template.TREE_COLLAPSED));
		UIManager.put("Tree.expandedIcon", ResourceLoader.getSVGIcon(Template.TREE_EXPANDED));
		UIManager.put("Tree.leafIcon", ResourceLoader.getSVGIcon(Template.TREE_DOCUMENT));
		UIManager.put("Tree.leafIconInactive", ResourceLoader.getSVGIcon(Template.TREE_DOCUMENT_INACTIVE));
		UIManager.put("Tree.openIcon", ResourceLoader.getSVGIcon(Template.TREE_FOLDER_OPEN));
		UIManager.put("Tree.openIconInactive", ResourceLoader.getSVGIcon(Template.TREE_FOLDER_OPEN_INACTIVE));
		UIManager.put("Tree.closedIcon", ResourceLoader.getSVGIcon(Template.TREE_FOLDER));
		UIManager.put("Tree.closedIconInactive", ResourceLoader.getSVGIcon(Template.TREE_FOLDER_INACTIVE));

		// Set JLabel font default non bold
		Font oldLabelFont = UIManager.getFont("Label.font");
		UIManager.put("Label.font", oldLabelFont.deriveFont(Font.PLAIN));
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		createView();
		updateLocale();
		model.addObserver(this);
		model.getPluginModel().addObserver(this);
	}

	private void createView() {
		logger.entry();

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				fireNavigationEvent(NavigationAction.CLOSE_MAIN);
			}
		});

		// Set Icon
		try {
			BufferedImage image = ImageIO.read(ResourceLoader
					.getURL(Template.FAVICON));
			this.setIconImage(image);
		} catch (IOException e) {
			// Nichts tun
		}

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(createNavigationPanel());
		splitPane.setRightComponent(new GeneralSettingsView(model));

		splitPane.setDividerSize(1);
		splitPane.setEnabled(false);

		this.add(splitPane);

		setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HIGHT));
		setSize(getPreferredSize());
		setResizable(false);

		pack();
		// setLocationByPlatform(true); -> setzt Fenster in Gnome ins obere
		// linke Eck
		setLocationRelativeTo(null);
		setVisible(true);
		logger.exit();

	}

	private JPanel createNavigationPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Template.COLOR_DARK);

		plugins = new MainMenuEntryPanel(getLocalized(MENU_PLUGINS_LABEL),
				Template.MENU_PLUGINS);
		plugins.setPreferredSize(new Dimension(NAVIGATION_WIDTH, 45));
		plugins.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				switchToPlugins();
			}
		});

		generalSettings = new MainMenuEntryPanel(
				getLocalized(MENU_SETTINGS_LABEL), Template.MENU_SETTINGS);
		generalSettings.setPreferredSize(new Dimension(NAVIGATION_WIDTH, 45));
		generalSettings.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				switchToSettings();
			}
		});

		log = new MainMenuEntryPanel(getLocalized(MENU_LOGGER_LABEL),
				Template.MENU_LOGGER);
		log.setPreferredSize(new Dimension(NAVIGATION_WIDTH, 45));
		log.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				switchToLogs();
			}
		});

		about = new MainMenuEntryPanel(getLocalized(MENU_ABOUT_LABEL),
				Template.MENU_ABOUT);
		about.setPreferredSize(new Dimension(NAVIGATION_WIDTH, 45));
		about.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				switchToAbout();
			}
		});

		panel.add(plugins);
		plugins.select(true);
		generalSettings.select(false);
		about.select(false);
		log.select(false);
		plugins.select(false);

		panel.add(plugins);
		panel.add(generalSettings);
		panel.add(log);
		panel.add(about);

		return panel;
	}

	void addNavigationListener(NavigationListener listener) {
		if (listener != null) {
			this.listeners.add(NavigationListener.class, listener);
		}
	}

	public void switchToSettings() {
		generalSettings.select(true);
		plugins.select(false);
		log.select(false);
		about.select(false);
		// TODO detacheView
		splitPane.setRightComponent(new GeneralSettingsView(model));
		repaint();
	}

	public void switchToPlugins() {
		generalSettings.select(false);
		plugins.select(true);
		log.select(false);
		about.select(false);
		// TODO detacheView
		splitPane.setRightComponent(new PluginView(model));
		repaint();
	}

	public void switchToLogs() {
		generalSettings.select(false);
		plugins.select(false);
		log.select(true);
		about.select(false);
		// TODO detacheView
		splitPane.setRightComponent(new LogView(model));
		repaint();
	}

	public void switchToAbout() {
		generalSettings.select(false);
		plugins.select(false);
		log.select(false);
		about.select(true);
		// TODO detacheView
		splitPane.setRightComponent(new AboutView(model));
		repaint();
	}

	private void fireNavigationEvent(NavigationAction action) {
		for (NavigationListener listener : listeners
				.getListeners(NavigationListener.class)) {
			listener.actionPerformed(action);
		}
	}

	public void detacheView() {
		model.deleteObserver(this);
		model.getPluginModel().deleteObserver(this);
		// TODO detacheView
	}

	private String getLocalized(String key) {
		String val = resourceBundle.getString(key);
		try {
			return new String(val.getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return val;
	}

	private void updateLocale() {
		this.resourceBundle = model.getResourceBundle(model.getCurrentLocale());
		this.setTitle(getLocalized(MAIN_WINDOW_TITLE));
		generalSettings.setText(getLocalized(MENU_SETTINGS_LABEL));
		plugins.setText(getLocalized(MENU_PLUGINS_LABEL));
		log.setText(getLocalized(MENU_LOGGER_LABEL));
		about.setText(getLocalized(MENU_ABOUT_LABEL));
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.entry();
		if (o == model.getPluginModel()
				&& model.getPluginModel().getLoginModel() != null
				&& model.getPluginModel().getLoginModel() == arg) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					fireNavigationEvent(NavigationAction.MAIN_TO_LOGIN);
				}
			});
		} else if (o == model) {
			updateLocale();
		}

		logger.exit();

	}

}
