package de.danner_web.studip_client.data;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.Starter;
import de.danner_web.studip_client.plugin.PluginInformation;

public class PluginSettings {

	private Map<String, String> settings;
	private PluginInformation info;

	private Preferences rootPref = Preferences
			.userNodeForPackage(Starter.class);
	private static Logger logger = LogManager.getLogger(PluginSettings.class);

	public PluginSettings(PluginInformation info) {
		this.info = info;
		load();
	}

	public void clear() {
		settings.clear();
		save();
	}

	public boolean containsKey(Object arg0) {
		return settings.containsKey(arg0);
	}

	public boolean containsValue(Object arg0) {
		return settings.containsValue(arg0);
	}

	public String get(String arg0) {
		return settings.get(arg0);
	}

	public boolean isEmpty() {
		return settings.isEmpty();
	}

	public String put(String key, String value) {
		String old = settings.put(key, value);
		save();
		return old;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void putAll(Map arg0) {
		settings.putAll(arg0);
		save();
	}

	public String remove(String key) {
		String old = settings.remove(key);
		save();
		return old;
	}

	public int size() {
		return settings.size();
	}

	private void save() {
		Preferences subPrefsForPlugin = rootPref.node("plugins").node(info.getName());
		subPrefsForPlugin = subPrefsForPlugin.node("settings");

		for (String key : settings.keySet()) {
			subPrefsForPlugin.put(key, settings.get(key));
		}

		logger.debug("PluginSettings of" + info.getName()
				+ " successfully saved");
	}

	private void load() {
		Preferences subPrefsForPlugin = rootPref.node("plugins").node(info.getName());
		subPrefsForPlugin = subPrefsForPlugin.node("settings");

		settings = new HashMap<String, String>();
		try {
			String[] allKeys = subPrefsForPlugin.keys();

			for (String key : allKeys) {
				String value = subPrefsForPlugin.get(key, "");
				if (!value.equals("")) {
					settings.put(key, value);
				}
			}
		} catch (BackingStoreException e) {
			logger.warn("BackingStore is not available -> no plugin settings are loaded");
		}

		logger.debug("PluginSettings of" + info.getName()
				+ " successfully loaded");
	}
}
