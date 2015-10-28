package de.danner_web.studip_client.plugins.hello_world;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import de.danner_web.studip_client.data.OAuthServer;
import de.danner_web.studip_client.plugin.Plugin;
import de.danner_web.studip_client.plugin.PluginInformation;

public class Info extends PluginInformation {

    @Override
    public String getName() {
        return "Hello World Plugin";
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
        return "[Author name]";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Plugin> getPluginClass() {
        return (Class<Plugin>) HelloWorldPlugin.class.asSubclass(Plugin.class);
    }

    @Override
    public Collection<OAuthServer> getServerList() {
        Collection<OAuthServer> servers = new ArrayList<>();
        servers.add(new OAuthServer("[Uni Name]", "", "", "", "", "", "entwickler@uni-xyz.de"));
        return servers;
    }

    @Override
    public URL getWebsite() {
        URL url = null;
        try {
            url = new URL("http://www.uni-xyz.de");
        } catch (MalformedURLException e) {
        }
        return url;
    }

    @Override
    public boolean hasSettingsGui() {
        return false;
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

    @Override
    public int getTriggerTime() {
        return 5;
    }
}
