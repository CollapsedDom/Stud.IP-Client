package de.danner_web.studip_client.view.components;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javax.swing.event.EventListenerList;

import de.danner_web.studip_client.model.Model;
import de.danner_web.studip_client.model.PluginModel;
import de.danner_web.studip_client.view.NavigationAction;
import de.danner_web.studip_client.view.NavigationListener;

public class TrayIconPopMenu extends PopupMenu implements Observer {

	private EventListenerList listeners = new EventListenerList();

	/**
	* 
	*/
	private static final long serialVersionUID = -1020285027923039859L;

	private Model model;

	private PluginModel pluginModel;

	private static final String SETTINGS_LABEL = "de.danner_web.studip_client.view.MainWindow.menuSettings";
	private static final String HISTORY_LABEL = "de.danner_web.studip_client.view.MainWindow.menuLogger";
	private static final String PLUGINS_LABEL = "de.danner_web.studip_client.view.MainWindow.menuPlugins";
	private static final String ABOUT_LABEL = "de.danner_web.studip_client.view.MainWindow.menuAbout";
	private static final String PAUSE_LABEL = "de.danner_web.studip_client.view.PopupMenu.pause";
	private static final String RESUME_LABEL = "de.danner_web.studip_client.view.PopupMenu.resume";
	private static final String EXIT_LABEL = "de.danner_web.studip_client.view.PopupMenu.exit";

	private ResourceBundle resourceBundle;

	private MenuItem settingsItem, pluginItem, history, aboutItem, pauseItem,
			exitItem;

	public TrayIconPopMenu(Model model) {
		this.model = model;
		this.pluginModel = model.getPluginModel();
		this.resourceBundle = model.getResourceBundle(model.getCurrentLocale());

		model.addObserver(this);
		pluginModel.addObserver(this);
		createMe();
	}

	private void createMe() {

		// Create a popup menu components
		settingsItem = new MenuItem();
		pluginItem = new MenuItem();
		history = new MenuItem();
		aboutItem = new MenuItem();
		pauseItem = new MenuItem();
		exitItem = new MenuItem();

		updateText();

		// Add components to popup menu
		add(pluginItem);
		add(settingsItem);
		add(history);
		add(aboutItem);
		addSeparator();
		add(pauseItem);
		addSeparator();
		add(exitItem);
		
		settingsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireNavigationEvent(NavigationAction.INFO_TO_SETTINGS);
			}
		});

		pluginItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireNavigationEvent(NavigationAction.INFO_TO_PLUGINS);
			}
		});

		history.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireNavigationEvent(NavigationAction.INFO_TO_LOGS);
			}
		});

		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireNavigationEvent(NavigationAction.INFO_TO_ABOUT);
			}
		});

		pauseItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pluginModel.isRunning()) {
					pluginModel.pause();
				} else {
					pluginModel.resume();
				}
			}
		});

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireNavigationEvent(NavigationAction.CLOSE);
			}
		});
	}

	public void addNavigationListener(NavigationListener listener) {
		if (listener != null) {
			this.listeners.add(NavigationListener.class, listener);
		}
	}

	private void fireNavigationEvent(NavigationAction action) {
		for (NavigationListener listener : listeners
				.getListeners(NavigationListener.class)) {
			listener.actionPerformed(action);
		}
	}

	private String getLocalized(String key) {
		return resourceBundle.getString(key);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == pluginModel) {
			updateText();
			// TODO Update Text
			// TODO Add new menuItem for Plugins ...
		} else if (o == model && arg instanceof Locale) {
			// Change Language
			resourceBundle = model.getResourceBundle(model.getCurrentLocale());
			updateText();
		}
	}

	private void updateText() {
		settingsItem.setLabel(getLocalized(SETTINGS_LABEL));
		pluginItem.setLabel(getLocalized(PLUGINS_LABEL));
		history.setLabel(getLocalized(HISTORY_LABEL));
		aboutItem.setLabel(getLocalized(ABOUT_LABEL));
		if (model.getPluginModel().isRunning()) {
			pauseItem.setLabel(getLocalized(PAUSE_LABEL));
		} else {
			pauseItem.setLabel(getLocalized(RESUME_LABEL));
		}
		exitItem.setLabel(getLocalized(EXIT_LABEL));
	}

	public void detacheView() {
		model.deleteObserver(this);
		pluginModel.deleteObserver(this);
	}

}
