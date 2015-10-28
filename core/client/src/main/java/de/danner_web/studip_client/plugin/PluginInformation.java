package de.danner_web.studip_client.plugin;

import java.net.URL;
import java.util.Collection;

import de.danner_web.studip_client.data.OAuthServer;

public abstract class PluginInformation implements Comparable<PluginInformation> {
    
    /**
     * Returns the Name of this Plugin.
     * 
     * The name must be unique
     * 
     * @return name of plugin
     */
    public abstract String getName();
    
    /**
     * Returns the Version of this Plugin
     * 
     * @return version of plugin
     */
    public abstract String getVersion();
    
    /**
     * Returns the url to a json which provides information for version und .jar
     * file.
     * 
     * TODO genaue definition des json
     * 
     * @return url to json
     */
    public abstract URL getUpdateURL();
    
    /**
     * Returns the author of this Plugin
     * 
     * @return name of author
     */
    public abstract String getAuthor();
    
    /**
     * Returns the trigger time, in which this Plugin gets started.
     * 
     * @return trigger Time (seconds)
     */
    public abstract int getTriggerTime();
    
    /**
     * Returns the Implementation of Plugin.class of this Plugin.
     * 
     * @return Implementation of Plugin.class
     */
    public abstract Class<Plugin> getPluginClass();
    
    /**
     * Returns a Collection of tested and working OAuthServer with this Plugin.
     * 
     * @return Collection of OAuthServer
     */
    public abstract Collection<OAuthServer> getServerList();
    
    @Override
    public int compareTo(PluginInformation info) {
        return (this.getName().compareTo(info.getName())
                + this.getVersion().compareTo(info.getVersion()));
    }
    
    /**
     * Returns the website of the author of this Plugin
     * 
     * @return
     */
    public abstract URL getWebsite();
    
    /**
     * Informs if this Plugin provides Gui for its settings to configure.
     * 
     * @return true if there is a settings Gui, false otherwise
     */
    public abstract boolean hasSettingsGui();
    
    /**
     * This method is called when this Plugin gets installed.
     * 
     * @return true if no error occurs, false otherwise
     */
    public abstract boolean onInstall();
    
    /**
     * This method is called when this Plugin gets uninstalled.
     * 
     * @return true if no error occurs, false otherwise.
     */
    public abstract boolean onUninstall();
    
    /**
     * This method is called when this Plugin gets updated.
     * 
     * If the return value is false, all changes must be reversed.
     * 
     * @param oldVerison
     *            the String of the old version installed
     * @return true if no error occurs, false otherwise
     */
    public abstract boolean onUpdate(String oldVersion);
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        return result;
    }
    
    /**
     * Method to compare two PluginInformation
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PluginInformation other = (PluginInformation) obj;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        return true;
    }
    
}
