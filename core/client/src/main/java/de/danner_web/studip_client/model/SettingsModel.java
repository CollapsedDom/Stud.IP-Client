package de.danner_web.studip_client.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Observable;

import de.danner_web.studip_client.Starter;
import de.danner_web.studip_client.plugin.PluginInformation;
import de.danner_web.studip_client.utils.AutostartUtil;

/**
 * This class represents the general settings of the application. It also
 * provides the ability to store these settings in a persistent way. This is
 * done by using the Preference API, which also handles multiuser support.
 * 
 * @author Philipp
 * 
 */
public class SettingsModel extends Observable {

	private Map<String, String> map = new HashMap<String, String>();

	private Preferences rootPref;

	private static Logger logger = LogManager.getLogger(SettingsModel.class);

	// Names for settings, also in registry/XML file
	private static final String INSTALL_ID = "installID";

	private static final String HIDDEN_START = "hidden";
	private static final String AUTOSTART = "autoStart";
	private static final String AUTOUPDATE = "autoUpdate";
	private static final String LOCALE = "locale";
	private static final String VERSION = "client_version";

	private static final String NOTIFICATION_ENABLED = "notificationEnabled";
	private static final String NOTIFICATION_DELETE_TIME = "notificationDeleteTime";
	private static final String NOTIFICATION_MAX_COUNT = "notificationMaxCount";
	private static final String NOTIFICATION_ORIENTATION = "notificationOrientation";
	private static final String NOTIFICATION_PADDING_BOTTOM = "notificationPaddingBottom";
	private static final String NOTIFICATION_PADDING_TOP = "notificationPaddingTop";


    private static final String INSTALL_FOLDER = "installFolder";
    private static final String ACTIVE_PLUGINS = "activePlugins";
    
    public enum NotificationOrientation {
        TOP, BOTTOM
    }

	SettingsModel() {

		// Insert default values
		map.put(HIDDEN_START, "false");
		map.put(AUTOSTART, "true");
		map.put(AUTOUPDATE, "true");
		map.put(LOCALE, Locale.getDefault(Category.DISPLAY).getLanguage());
		map.put(VERSION, Starter.CLIENT_VERSION);
		
		map.put(NOTIFICATION_ENABLED, "true");
        map.put(NOTIFICATION_DELETE_TIME, "5");
        map.put(NOTIFICATION_MAX_COUNT, "3");
        map.put(NOTIFICATION_ORIENTATION, NotificationOrientation.BOTTOM.toString());
        map.put(NOTIFICATION_PADDING_BOTTOM, "15");
        map.put(NOTIFICATION_PADDING_TOP, "45");

        map.put(INSTALL_FOLDER, "");
        map.put(INSTALL_ID, "1");
        
        rootPref = Preferences.userNodeForPackage(Starter.class);

		// Load from prefs if possible
		try {
			String[] allKeys = rootPref.keys();

			for (String key : allKeys) {
				String value = rootPref.get(key, "");
				if (!value.equals("")) {
					map.put(key, value);
				}
			}

		} catch (BackingStoreException e) {
			logger.warn("BackingStore is not available -> switched to default settings");
		}
		
		if(!updateShortcut()){
			map.put(AUTOSTART, "false");
		}

		if (map.get(INSTALL_ID).equals("1")) {
			map.put(INSTALL_ID, UUID.randomUUID().toString());
			save();
		}
	}

	private void save() {
		for (String key : map.keySet()) {
			rootPref.put(key, map.get(key));
		}
		logger.info("SettingsModel saved");
		setChanged();
		notifyObservers();
	}

	//
	// General
	//
	public String getInstallId() {
		return map.get(INSTALL_ID);
	}

	public void setInstallId(String id) {
		map.put(INSTALL_ID, id);
		save();
	}

	public String getInstallFolder() {
		return map.get(INSTALL_FOLDER);
	}

	public void setInstallFolder(String property) {
		map.put(INSTALL_FOLDER, property);
		save();
	}

	public String[] getActivePlugins() {
		String plugins = map.get(ACTIVE_PLUGINS);
		String[] pluginList = new String[0];
		if (plugins != null) {
			pluginList = plugins.split(",");
		}
		return pluginList;
	}

	public void saveActivePlugins(Collection<PluginInformation> set) {
		String list = "";
		for (PluginInformation pluginInformation : set) {
			list += pluginInformation.getName() + ",";
		}
		map.put(ACTIVE_PLUGINS, list);
		save();
	}

	public String getVersion() {
		return map.get(VERSION);
	}

	public void setVersion(String version) {
		map.put(VERSION, version);
		save();
	}

	public boolean isHidden() {
		return Boolean.parseBoolean(map.get(HIDDEN_START));
	}

	public void setHidden(boolean b) {
		map.put(HIDDEN_START, Boolean.toString(b));
		save();
	}

	public boolean isAutoStart() {
		return Boolean.parseBoolean(map.get(AUTOSTART));
	}

	public void setAutoStart(boolean b) {
		map.put(AUTOSTART, Boolean.toString(b));
		save();
		// If failure by activate -> rewrite old value
		if (!updateShortcut()) {
			map.put(AUTOSTART, Boolean.toString(!b));
			save();
		}
	}

	private boolean updateShortcut() {
		boolean success = false;
		if (isAutoStart()) {
			success = AutostartUtil.createAutoStartShortcut();
		} else {
			success = AutostartUtil.deleteAutoStartShortcut();
		}
		return success;
	}

	public boolean isAutoUpdate() {
		return Boolean.parseBoolean(map.get(AUTOUPDATE));
	}

	public void setAutoUpdate(boolean b) {
		map.put(AUTOUPDATE, Boolean.toString(b));
		save();
	}

	public Locale getLocale() {
		return new Locale(map.get(LOCALE));
	}

	public void setLocale(Locale locale) {
		map.put(LOCALE, locale.getLanguage());
		save();
	}

	//
	// Notification
	//
	public boolean isNotificationEnabled() {
		return Boolean.parseBoolean(map.get(NOTIFICATION_ENABLED));
	}

	public void setNotificationEnabled(boolean b) {
		map.put(NOTIFICATION_ENABLED, Boolean.toString(b));
		save();
	}

	public int getNotificationDeleteTime() {
		return Integer.parseInt(map.get(NOTIFICATION_DELETE_TIME));
	}

	public void setNotificationDeleteTime(int i) {
		map.put(NOTIFICATION_DELETE_TIME, Integer.toString(i));
		save();
	}

	public int getNotificationMaxCount() {
		return Integer.parseInt(map.get(NOTIFICATION_MAX_COUNT));
	}

	public void setNotificationMaxCount(int i) {
		map.put(NOTIFICATION_MAX_COUNT, Integer.toString(i));
		save();
	}

	public NotificationOrientation getNotificationOrientation() {
		return NotificationOrientation.valueOf(map
				.get(NOTIFICATION_ORIENTATION));
	}

	public void setNotificationOrientation(NotificationOrientation s) {
		map.put(NOTIFICATION_ORIENTATION, s.toString());
		save();
	}

	public int getNotificationPaddingBottom() {
		return Integer.parseInt(map.get(NOTIFICATION_PADDING_BOTTOM));
	}

    public void setNotificationPaddingTop(int i) {
        map.put(NOTIFICATION_PADDING_TOP, Integer.toString(i));
        save();
    }
    
    public int getNotificationPaddingTop() {
        return Integer.parseInt(map.get(NOTIFICATION_PADDING_TOP));
    }
}
