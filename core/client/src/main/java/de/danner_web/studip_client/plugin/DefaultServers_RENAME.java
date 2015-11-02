package de.danner_web.studip_client.plugin;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import de.danner_web.studip_client.data.OAuthServer;

public class DefaultServers_RENAME extends PluginInformation{

    private PluginInformation info;
    
    public DefaultServers_RENAME(PluginInformation info){
        this.info = info;
    }
    
    @Override
    public String getName() {
        return info.getName();
    }

    @Override
    public String getVersion() {
        return info.getVersion();
    }

    @Override
    public URL getUpdateURL() {
        return info.getUpdateURL();
    }

    @Override
    public String getAuthor() {
        return info.getAuthor();
    }

    @Override
    public int getTriggerTime() {
        return info.getTriggerTime();
    }

    @Override
    public Class<Plugin> getPluginClass() {
        return info.getPluginClass();
    }

    @Override
    public Collection<OAuthServer> getServerList() {
        Collection<OAuthServer> servers = new ArrayList<>();
		//Add here OAuthServers
        return servers;
    }

    @Override
    public URL getWebsite() {
        return info.getWebsite();
    }

    @Override
    public boolean hasSettingsGui() {
        return info.hasSettingsGui();
    }

    @Override
    public boolean onInstall() {
        return info.onInstall();
    }

    @Override
    public boolean onUninstall() {
        return info.onUninstall();
    }

    @Override
    public boolean onUpdate(String oldVersion) {
        return info.onUpdate(oldVersion);
    }
}
