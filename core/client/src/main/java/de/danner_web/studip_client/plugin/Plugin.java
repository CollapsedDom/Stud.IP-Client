package de.danner_web.studip_client.plugin;

import javax.swing.JPanel;

import de.danner_web.studip_client.data.PluginSettings;
import de.danner_web.studip_client.model.Context;
import de.danner_web.studip_client.utils.oauth.OAuthConnector;

/**
 * This class is the abstract built for a Plugin of the StudIP Sync Framework.
 * 
 * A plugin must implement all abstract function to work correctly
 * 
 * @author Dominik Danner
 * @author Philipp Danner
 *
 */
public abstract class Plugin {

	/**
	 * OAuthConnection for the plugin
	 */
	protected OAuthConnector con;

	/**
	 * PluginSettings for the plugin
	 */
	protected PluginSettings settings;

	/**
	 * Context to access the methods appendPopUp and appendHistory in model.
	 */
	protected Context context;

	/**
	 * Super Constructor for all plugins
	 * 
	 * A Plugin gets his corresponding OAuthConnector and its PluginSettings.
	 * This super constructor save both in protected variables.
	 * 
	 * @param con
	 *            Connection for the plugin
	 * @param settings
	 *            Settings of the plugin
	 */
	public Plugin(Context context, OAuthConnector con, PluginSettings settings) {
		this.context = context;
		this.con = con;
		this.settings = settings;
	}

	/**
	 * This is the main method of the plugin.
	 * 
	 * This method will be periodically invoked by the Framework.
	 * 
	 * @return errorCode
	 */
	public abstract int doWork();

	/**
	 * This method is invoked, if the user wants to configure the plugin.
	 * 
	 * The method hasSettingsGui() from the PluginInformation determines if this
	 * function gets invoked. The Window will be displayed for the user to
	 * configure the settings of the plugin.
	 * 
	 * @return A settings JWindow or null if not used
	 */
	public abstract JPanel buildsettingsGUI();

	/**
	 * This method gets invoked after activation of the plugin.
	 * 
	 * Activation means the authorization with the server. After this method the
	 * plugin must be in correct state for the method doWork() to get invoked.
	 * 
	 * @return true if no error, false otherwise
	 */
	public abstract boolean onActivate();

	/**
	 * This method gets invoked after deactivation of the plugin.
	 * 
	 * Deactivation means the doWork() function does not get invoked until the
	 * onActivate() method is called again.
	 * 
	 * @return true if no error, false otherwise
	 */
	public abstract boolean onDeactivate();

	/**
	 * This method gets invoked after pressing pause for all plugins
	 * 
	 * @return true if no error, false otherwise
	 */
	public abstract boolean onPause();

	/**
	 * This method gets invoked after pressing resume for all plugins
	 * 
	 * @return true if no error, false otherwise
	 */
	public abstract boolean onResume();

}
