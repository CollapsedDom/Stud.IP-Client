package de.danner_web.studip_client.plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.data.PluginSettings;
import de.danner_web.studip_client.model.Context;
import de.danner_web.studip_client.utils.oauth.OAuthConnector;

public class PluginHandler implements Runnable {

	/**
	 * Logger dieser Klasse.
	 */
	private static Logger logger = LogManager.getLogger(PluginHandler.class);

	/**
	 * The Plugin that gets handled by this Handler.
	 */
	private Plugin plugin;

	/**
	 * Determines if the Handler has currently an active thread, that is running
	 */
	private boolean running;

	/**
	 * Saved PluginInformation to get back triggerTime
	 */
	private PluginInformation info;

	private Context context;

	/**
	 * This thread triggers the Plugin Execution
	 */
	private Thread thread;

	public PluginHandler(Context context, OAuthConnector connector,
			PluginInformation pluginInfo, PluginSettings pluginSettings) {
		this.context = context;
		this.info = pluginInfo;
		loadPlugin(connector, pluginInfo, pluginSettings);

		thread = new Thread(this);

	}

	private void loadPlugin(OAuthConnector connector, PluginInformation info,
			PluginSettings pluginSettings) {

		try {
			Class<Plugin> pluginClass = info.getPluginClass();

			logger.debug("Loaded Class " + pluginClass.getName());
			@SuppressWarnings("rawtypes")
			Constructor[] constr = pluginClass.getDeclaredConstructors();
			logger.debug("Extracted Constructor " + constr[0].getName());
			// TODO Check number of arguments
			this.plugin = (Plugin) constr[0].newInstance(this.context,
					connector, pluginSettings);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		logger.debug("add Plugin into private variable");

	}

	/**
	 * This thread triggers the Plugin and then waits a given time. After that
	 * the Plugin gets triggered again.
	 */
	@Override
	public void run() {
		running = true;
		while (running) {
			try {
				long wait = info.getTriggerTime();
				if (wait < 60) {
					wait = 60;
				}
				if (plugin != null) {
					plugin.doWork();
				}
				Thread.sleep(wait * 1000);
			} catch (InterruptedException e) {
				running = false;
			}
		}

	}

	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	public void softKill() {
		thread.interrupt();
		try {
			// Check if thread is really dead
			thread.join();
		} catch (InterruptedException e) {
			thread.interrupt();
		}
	}

	public boolean onActivate() {
		return plugin.onActivate();
	}

	public boolean onDeactivate() {
		return plugin.onDeactivate();
	}

	/**
	 * When pause is selected, the active thread gets killed and the Plugin gets
	 * informed.
	 * 
	 * @return true if no problem occurred, otherwise false;
	 */
	public boolean onPause() {
		softKill();
	    boolean success = plugin.onPause();
		return success;
	}

	/**
	 * When resume is selected, the thread gets started and the Plugin gets
	 * informed.
	 * 
	 * @return true if no problem occurred, otherwise false;
	 */
	public boolean onResume() {
		start();
		return plugin.onResume();
	}

	public JPanel getPluginSettingsView() {
		if (plugin == null) {
			return new JPanel();
		}
		return plugin.buildsettingsGUI();
	}

	public boolean isAlive() {
		return thread.isAlive();
	}

}
