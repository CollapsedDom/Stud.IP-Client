package de.danner_web.studip_client.plugins.hello_world;

import javax.swing.JPanel;

import de.danner_web.studip_client.data.PluginSettings;
import de.danner_web.studip_client.data.TextPluginMessage;
import de.danner_web.studip_client.model.Context;
import de.danner_web.studip_client.plugin.Plugin;
import de.danner_web.studip_client.utils.oauth.OAuthConnector;

public class HelloWorldPlugin extends Plugin {

	public HelloWorldPlugin(Context context, OAuthConnector con,
			PluginSettings settings) {
		super(context, con, settings);
	}

	@Override
	public int doWork() {
		context.appendPopup(new TextPluginMessage("Hello World!", "This is a Hello World Message", null));
		return 0;
	}

	@Override
	public JPanel buildsettingsGUI() {
		return null;
	}

	@Override
	public boolean onActivate() {

		return true;
	}

	@Override
	public boolean onDeactivate() {
		return true;
	}

	@Override
	public boolean onPause() {
		return true;
	}

	@Override
	public boolean onResume() {
		return true;
	}
}
