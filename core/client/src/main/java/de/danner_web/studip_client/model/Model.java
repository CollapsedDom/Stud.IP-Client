package de.danner_web.studip_client.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.data.LogItem;
import de.danner_web.studip_client.data.PluginMessage;

public class Model extends Observable {

	/**
	 * Logger dieser Klasse.
	 */
	private static Logger logger = LogManager.getLogger(Model.class);

	private static final int MAX_LOG_MESSAGES = 100;

	/**
	 * Speichert das zur Lokalisierung der Clientanwendung verwendete
	 * <code>Locale</code>.
	 */
	private Locale currentLocale;

	private PluginModel pluginModel;

	private SettingsModel settings;

	private List<LogItem> loggingList;

	public Model() {
		settings = new SettingsModel();
		pluginModel = new PluginModel(new Context(this), settings);
		currentLocale = settings.getLocale();
		loggingList = new ArrayList<LogItem>();
	}

	public List<LogItem> getLogList() {
		return loggingList;
	}

	public PluginModel getPluginModel() {
		return pluginModel;
	}
	
	public SettingsModel getSettingsModel(){
		return settings;
	}

	public Locale getCurrentLocale() {
		return currentLocale;
	}

	public void setCurrentLocale(Locale locale) {
		this.currentLocale = locale;
		settings.setLocale(locale);

		setChanged();
		notifyObservers();
	}

	public ResourceBundle getResourceBundle(Locale locale) {
		if (locale == null) {
			throw logger.throwing(new IllegalArgumentException(
					"Locale must not be null!"));
		}
		return ResourceBundle.getBundle("ResourceBundle", locale);
	}
	

	public void appendHistory(String name, String content) {
		logger.entry(name);
		if (name == null) {
			throw logger.throwing(new IllegalArgumentException(
					"Pluginname and content should not be null"));
		}

		Date date = new Date(System.currentTimeMillis());

		if (loggingList.size() >= MAX_LOG_MESSAGES) {
			loggingList.remove(0);
		}
		LogItem item = new LogItem(date, name, content);
		loggingList.add(item);

		setChanged();
		notifyObservers(item);

		logger.exit();
	}

	/**
	 * 
	 * @param header
	 *            Überschrift des Popups
	 * @param text
	 *            Nachricht
	 */
	public void appendPopup(PluginMessage message) {
		logger.entry();
		if (message == null) {
			throw logger.throwing(new IllegalArgumentException(
					"Message should not be null"));
		}

		setChanged();
		// Direkt an die PopupView übergeben
		notifyObservers(message);
		logger.exit();
	}
}
