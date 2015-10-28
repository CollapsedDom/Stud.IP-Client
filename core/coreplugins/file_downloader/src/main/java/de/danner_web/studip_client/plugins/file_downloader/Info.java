package de.danner_web.studip_client.plugins.file_downloader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import de.danner_web.studip_client.data.OAuthServer;
import de.danner_web.studip_client.plugin.Plugin;
import de.danner_web.studip_client.plugin.PluginInformation;

/**
 * PluginInformation for the "Documenten Downloader".
 * 
 * @author Danner Dominik
 * 
 */
public class Info extends PluginInformation {

	@Override
	public String getName() {
		return "Dokumenten Downloader";
	}

	@Override
	public String getVersion() {
		return "0.1 stable";
	}

	@Override
	public URL getUpdateURL() {
		return null;
	}

	@Override
	public String getAuthor() {
		return "Dominik Danner";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Plugin> getPluginClass() {
		return (Class<Plugin>) FileDownloadPlugin.class
				.asSubclass(Plugin.class);
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
			// TODO Auto-generated catch block
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
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onUninstall() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onUpdate(String oldVersion) {
		// TODO Auto-generated method stub
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
