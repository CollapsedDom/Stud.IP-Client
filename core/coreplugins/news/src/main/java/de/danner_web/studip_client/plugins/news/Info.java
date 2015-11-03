package de.danner_web.studip_client.plugins.news;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import de.danner_web.studip_client.data.OAuthServer;
import de.danner_web.studip_client.plugin.Plugin;
import de.danner_web.studip_client.plugin.PluginInformation;

/**
 * Info class for the news plugin. Needed routes on studip server: GET/PUT
 * ssf-core/news/
 * 
 * @author Philipp
 *
 */
public class Info extends PluginInformation {

	@Override
	public String getName() {
		return "Ankündigungen";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public URL getUpdateURL() {
		return null;
	}

	@Override
	public String getAuthor() {
		return "Philipp Danner";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Plugin> getPluginClass() {
		return (Class<Plugin>) NewsPlugin.class.asSubclass(Plugin.class);
	}

	@Override
	public Collection<OAuthServer> getServerList() {
		Collection<OAuthServer> servers = new ArrayList<>();
		return servers;
	}

	@Override
	public URL getWebsite() {
		URL url = null;
		try {
			url = new URL("http://www.danner-web.de");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url;
	}

	@Override
	public boolean hasSettingsGui() {
		return true;
	}

	@Override
	public boolean onInstall() {
		return true;
	}

	@Override
	public boolean onUninstall() {
		return true;
	}

	@Override
	public boolean onUpdate(String oldVersion) {
		return true;
	}

	/**
	 * Trigger Time: 5 min
	 */
    @Override
    public int getTriggerTime() {
        return 300;
    }

}
