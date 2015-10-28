package de.danner_web.studip_client.model;

import static java.nio.file.StandardCopyOption.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.data.OAuthServer;
import de.danner_web.studip_client.data.PluginSettings;
import de.danner_web.studip_client.plugin.DefaultServers;
import de.danner_web.studip_client.plugin.PluginHandler;
import de.danner_web.studip_client.plugin.PluginInformation;
import de.danner_web.studip_client.utils.oauth.OAuthConnector;

public class PluginModel extends Observable {

    /**
     * Logger dieser Klasse.
     */
    private static Logger logger = LogManager.getLogger(PluginModel.class);

    /**
     * This List contains all PluginInformation that are loaded (Jar File in
     * folder)
     */
    private List<PluginInformation> allPlugininfo = new ArrayList<PluginInformation>();

    /**
     * This List contains a subset of all PluginInformation loaded. Only signed
     * and verified Plugins are included.
     */
    private List<PluginInformation> verifiedPluginInfo = new ArrayList<PluginInformation>();
    private Map<PluginInformation, PluginHandler> activePlugins = new HashMap<PluginInformation, PluginHandler>();

    private LoginModel loginModel;
    private SettingsModel generalSettings;
    private OAuthServerModel oauthServerModel;

    private Context context;

    /**
     * Simple Method to delete a Path recursivly.
     * 
     * @param path
     *            to be deleted
     * @return true if success otherwise false.
     */
    static private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    /**
     * Initiate the PluginModel.
     * 
     * 
     */
    PluginModel(Context context, SettingsModel generalSettings) {
        this.context = context;
        this.generalSettings = generalSettings;
        this.oauthServerModel = new OAuthServerModel(this, generalSettings.getInstallId());
    }

    public void init() {
        loadPluginsFromFolder();
        restartActivePlugins();
    }

    /**
     * Return Status of all Plugins.
     * 
     * This includes the PluginInformation (e.g. version number, ...) and the
     * activation status.
     * 
     * @return
     */
    public Collection<PluginInformation> getPlugins() {
        return allPlugininfo;
    }

    /**
     * Check if given Plugin is verified
     */
    public boolean isVerified(PluginInformation info) {
        return verifiedPluginInfo.contains(info);
    }

    /**
     * Returns
     * 
     * @return
     */
    public LoginModel getLoginModel() {
        return loginModel;
    }

    /**
     * Returns
     * 
     * @return
     */
    public OAuthServerModel getOAuthServerModel() {
        return oauthServerModel;
    }

    /**
     * Give back active status of PluginInfo info
     * 
     * @param info
     *            PluginInformation
     * @return true, if active else false
     */
    public synchronized boolean isactivePlugin(PluginInformation info) {
        return (activePlugins.containsKey(info));
    }

    /**
     * This method restarts all Plugins that are listed in Prefs to be active.
     * 
     * A new PluginHandler is created, except there is already one.
     */
    private void restartActivePlugins() {
        String[] activePluginNames = generalSettings.getActivePlugins();
        for (int i = 0; i < activePluginNames.length; i++) {
            for (PluginInformation info : allPlugininfo) {
                if (info.getName().equals(activePluginNames[i]) && !activePlugins.containsKey(info)) {
                    // Add Plugin to Active list, if not already in
                    // activePlugins
                    OAuthServer server = oauthServerModel.getOAuthServer(info);
                    OAuthConnector con = new OAuthConnector(server);
                    PluginSettings settings = new PluginSettings(info);
                    PluginHandler handler = new PluginHandler(this.context.createContextForPlugin(info.getName()), con,
                            info, settings);
                    activePlugins.put(info, handler);
                    handler.start();
                }
            }
        }
    }

    /**
     * This method checks and loads the Plugin
     * 
     * First this function checks if the given File is a JarFile. Then it checks
     * if this JarFile has the specified structure for a SSF Plugin. If all is
     * correct the PluginInformation.Class gets loaded and returned.
     * 
     * @param pluginFile
     *            File of the Plugin.
     * @return PluginInformation of the Plugin or null if not valid JarFil
     */
    private PluginInformation ceckAndLoadJarFile(File pluginFile) {
        logger.entry();
        JarFile jarfile = null;
        try {
            jarfile = new JarFile(pluginFile);
        } catch (IOException e) {
            logger.debug("Could not load .jar File");
        }

        if (jarfile != null) {
            Enumeration<JarEntry> entry = jarfile.entries();

            URL url = null;
            try {
                url = new URL("jar:file:" + pluginFile.getAbsolutePath() + "!/");
            } catch (MalformedURLException e1) {
                logger.debug("Could not load urls File");
            }

            if (url != null) {

                URL[] urls = { url };
                URLClassLoader cl = URLClassLoader.newInstance(urls);

                while (entry.hasMoreElements()) {
                    JarEntry je = (JarEntry) entry.nextElement();
                    logger.debug("JarEntry je: " + je.getName());
                    if (!je.isDirectory() && je.getName().endsWith("Info.class")) {
                        // -6 because of .class
                        String className = je.getName().substring(0, je.getName().length() - 6);
                        className = className.replace('/', '.');
                        try {
                            Class<?> c = cl.loadClass(className);
                            logger.debug("Loaded Class " + className);
                            if (c.getGenericSuperclass().equals(PluginInformation.class)) {
                                /*
                                 * Is save, because the Info.class must extend
                                 * PluginInformation and PluginInformation has
                                 * one public default constructor
                                 */
                                @SuppressWarnings("unchecked")
                                Constructor<PluginInformation>[] constr = (Constructor<PluginInformation>[]) c
                                        .getConstructors();
                                logger.debug("Extracted Constructor " + constr[0].getName());
                                PluginInformation information = (PluginInformation) constr[0].newInstance();
                                return logger.exit(information);
                            } else {
                                logger.debug("Info.class not of Type PluginInformation");
                            }
                        } catch (ClassNotFoundException e1) {
                            e1.printStackTrace();
                        } catch (SecurityException e1) {
                            e1.printStackTrace();
                        } catch (InstantiationException e1) {
                            e1.printStackTrace();
                        } catch (IllegalAccessException e1) {
                            e1.printStackTrace();
                        } catch (IllegalArgumentException e1) {
                            e1.printStackTrace();
                        } catch (InvocationTargetException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        logger.debug(je.getName() + " is a directory or not 'Info.class'");
                    }
                }
            } else {
                logger.debug("Could not load directory in Jar file");
            }
        } else {
            logger.debug("Could not load Jar file");
        }
        try {
			jarfile.close();
		} catch (IOException e) {
		}
        return logger.exit(null);
    }

    /**
     * Load Plugins from Folder
     * 
     * This methode loads PluginInformation from given installation folder
     * 
     * All Plugins have to be installed first, so creating a new PluginModel
     * only loads previously installed Plugins. When a plugin is installed it is
     * stored in the folder called "plugins" beside the executable Jar file. The
     * Plugin needs at least a jar file containing the PluginInformation Class.
     * 
     * Folder Structure like:
     * 
     * APPLICATION_HOME/
     * 
     * APPLICATION_HOME/executable.jar
     * 
     * APPLICATION_HOME/plugins/
     * 
     * APPLICATION_HOME/plugins/PLUGIN_NAME/filesyncplugin.jar/
     * 
     * APPLICATION_HOME/plugins/PLUGIN_NAME/filesyncplugin.jar/pluginInformation
     * .class
     * 
     * @return
     */
    private boolean loadPluginsFromFolder() {
        logger.entry();
        if (generalSettings.getInstallFolder() != "") {
            // Check for folder named 'plugins', if not existing, create one
            File[] folder = new File(generalSettings.getInstallFolder()).listFiles(new FileFilter() {

                @Override
                public boolean accept(File a) {
                    return a.isDirectory() && a.getName().equals("plugins");
                }
            });

            logger.debug("Search for 'plugins' Folder in " + generalSettings.getInstallFolder());

            if (folder.length < 1) {
                new File("plugins/").mkdir();
                logger.debug("create new Folder plugin");
            }

            // Search all Folders in 'plugins' for jar files
            File[] pluginFolder = new File("plugins/").listFiles();

            for (File file : pluginFolder) {
                if (file.isDirectory()) {
                    logger.debug("Search jar File in " + file.getName());
                    // for each plugin folder
                    File[] plugin = file.listFiles(new FilenameFilter() {

                        @Override
                        public boolean accept(File dir, String name) {
                            /**
                             * Only accepts jar Files
                             */
                            return name.endsWith(".jar");
                        }
                    });

                    // Only accept one jar File in plugin directory
                    if (plugin.length == 1) {
                        logger.debug("Found one Jar File in folder " + file.getName());
                        /*
                         * Only one JarFile was found, so plugin[0] is the Jar
                         * file.
                         */
                        loadPlugin(plugin[0]);
                    } else {
                        logger.debug("More then one Jar File in Plugin folder");
                    }
                }
            }
        }
        return logger.exit(true);
    }

    /**
     * This method loads a Plugin.
     * 
     * First invokes checkAndLoadPlugin(). Then the returned Plugin is added to
     * allPluginInfo.
     * 
     * @param plugin
     *            File of the plugin
     * @return PluginInformation or null if not valid Plugin structure
     */
    private PluginInformation loadPlugin(File plugin) {
        logger.entry(plugin);
        PluginInformation information = ceckAndLoadJarFile(plugin);
        if (information != null) {
            if (!allPlugininfo.contains(information)) {
                allPlugininfo.add(information);
                try {
                    JarFile jarfile = new JarFile(plugin);
                    if (verifySignature(jarfile)) {
                        verifiedPluginInfo.add(information);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                logger.debug("add new PluginInfo into List");
                return logger.exit(information);
            } else {
                logger.info("Cannot load plugin " + information.getName() + information.getVersion() + " twice!");
            }
        }
        return logger.exit(null);
    }

    /**
     * Installs a new Plugin.
     * 
     * @param myPath
     *            to the plugin.jar file.
     * @return false, if any error occurs (e.g. file does not exist) else true
     */
    public boolean installPlugin(File plugin) {
        logger.entry(plugin);
        boolean success = false;
        PluginInformation info = null;

        if (plugin.exists() && plugin.isFile()) {
            info = ceckAndLoadJarFile(plugin);
            String folderName = info.getName();
            File pluginFolder = new File("plugins/" + folderName);
            if (pluginFolder.exists()) {
                // TODO issue #9 reinstall -> used to update a Plugin
            }
            pluginFolder.mkdirs();
            File destinationFile = new File(pluginFolder.getAbsolutePath() + "/" + plugin.getName());
            try {
                logger.debug("Try to Copy file: " + plugin.getName() + " from " + plugin.getAbsolutePath() + " to "
                        + pluginFolder.getAbsolutePath());
                Files.copy(plugin.toPath(), destinationFile.toPath(), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                logger.info("Cannot copy Plugin to destination Folder");
                return false;
            }

            info = loadPlugin(plugin);
            if (info != null && !info.onInstall()) {
                deleteDirectory(destinationFile);
            } else {
                logger.debug("could not load Plugin");
            }
        } else {
            logger.debug("The File at the path is not a File");
        }
        setChanged();
        notifyObservers(info);
        return logger.exit(success);
    }

    // public boolean updatePlugin(PluginInformation info) {
    // logger.entry(info);
    // if (info == null) {
    // logger.throwing(new IllegalArgumentException(
    // "info must not be null"));
    // } else if (!allPlugininfo.contains(info)) {
    // logger.throwing(new IllegalArgumentException(
    // "info must be part of allPluginInfo"));
    // }
    //
    // /* delete in Application structure */
    // boolean active = isactivePlugin(info);
    // if (active) {
    // deactivatePlugin(info);
    // activePlugins.remove(info);
    // }
    // allPlugininfo.remove(info);
    //
    // /* Download new jar File and install */
    // File updatedPlugin = null;
    // // TODO download .jar
    //
    // boolean success = installPlugin(updatedPlugin);
    // // TODO update jarfile
    //
    // if (success) {
    // // reload PluginInformation (works because PluginInformation equals
    // // only in PluginName)
    // PluginInformation newInfo = allPlugininfo.get(allPlugininfo
    // .indexOf(info));
    //
    // // call Method .onUpdate()
    // if (newInfo == null) {
    // success = false;
    // } else {
    // success = newInfo.onUpdate(info.getVersion());
    // }
    //
    // if (success) {
    // // TODO delete old jar
    // if (active) {
    // // request new authorization
    // authorizePlugin(info);
    // }
    // // restart handler if active
    // } else { // No success with onUpdate method
    // // TODO reload old Version
    // }
    // } else { // Not succes installing new Plugin
    // // TODO reload old Version
    // }
    //
    // return logger.exit(success);
    // }

    /**
     * Removes a Plugin
     * 
     * The Plugin gets deactivated und the installation folder gets deleted
     * 
     * @param info
     * @return
     */
    public boolean removePlugin(PluginInformation info) {
        logger.entry(info);
        if (info == null) {
            throw logger.throwing(new IllegalArgumentException("info must not be null"));
        }
        if (isactivePlugin(info)) {
            deactivatePlugin(info);
        }
        if (allPlugininfo.contains(info)) {
            allPlugininfo.remove(info);
        }

        boolean success = info.onUninstall();
        if (success) {
            String path = "plugins/" + info.getName();
            deleteDirectory(new File(path));
        }

        setChanged();
        notifyObservers(info);
        return logger.exit(success);
    }

    /**
     * 
     * @return
     */
    public boolean deleteDefaultServer() {
        logger.entry();

        for (PluginInformation info : activePlugins.keySet()) {
            deactivatePlugin(info);
        }
        oauthServerModel.deleteOAuthServer(null);

        setChanged();
        notifyObservers();
        return logger.exit(true);
    }

    /**
     * Activates a Plugin.
     * 
     * When a Plugin gets activated, a new PluginHandler is created. This
     * PluginHandler is a Thread, that executes the doWork method of the Plugin.
     * This Thread is registered in a local Map and can be mapped to the
     * corresponding PluginInformation.
     * 
     * Active Status of Plugins will be saved in the preferences of the
     * Application, so next startup activation will be restored.
     * 
     * @param info
     *            which Plugin should be activated
     * @return false, if any error occur, else true
     */
    public boolean authorizePlugin(PluginInformation info) {
        logger.entry();

        // If plugin is verified, load with default server
        if (verifiedPluginInfo.contains(info)) {
            if (oauthServerModel.isDefaultOAuthServerSet()) {

                // Default server is already set -> load plugin
                OAuthConnector con = new OAuthConnector(oauthServerModel.getOAuthServer(info));
                return activateNewPlugin(info, con);
            } else {

                // Create default server login form with injected info to force
                // startup of the plugin
                this.loginModel = new LoginModel(new DefaultServers(info));
            }
        } else {
            loginModel = new LoginModel(info);
        }
        setChanged();
        notifyObservers(loginModel);

        return logger.exit(true);
    }

    /**
     * Activates a new Plugin with the given info and the corresponding server
     * from the loginModel
     * 
     * @param info
     *            which Plugin to activate
     * @return true if success, otherwise false
     */
    public boolean activateNewPlugin(PluginInformation info, OAuthConnector con) {
        logger.entry();
        // Delete Login Model
        loginModel = null;

        OAuthServer server = con.getAuthorizedOAuthServer();
        if (con != null && server != null && !server.isAuthorized()) {
            logger.debug("The given OAuthServer is not available or not authorized");
            return false;
        }

        // Replace DefaultServers with original one in allPluginInfo list
        if (info instanceof DefaultServers) {
            for (PluginInformation i : allPlugininfo) {
                if (info.compareTo(i) == 0) {
                    info = i;
                }
            }
        }

        // Save new server to prefs
        oauthServerModel.saveOAuthServer(info, server);

        // Load settings from prefs
        PluginSettings settings = new PluginSettings(info);

        // Set active state in programm and prefs
        PluginHandler handler = new PluginHandler(this.context.createContextForPlugin(info.getName()), con, info,
                settings);

        boolean success = handler.onActivate();
        if (success) {
            activePlugins.put(info, handler);
            handler.start();
            generalSettings.saveActivePlugins(activePlugins.keySet());

        } else {
            // delete token and secret for Connection of plugin
            if (!verifiedPluginInfo.contains(info)) {
                // specific oauthserver for this plugin
                oauthServerModel.deleteOAuthServer(info);
            }
        }

        setChanged();
        notifyObservers();
        return logger.exit(success);
    }

    /**
     * Deactivates a Plugin.
     * 
     * If a plugin is registered as activated, it can also be deactivated. In
     * this case the PluginHandler will be stopped and after that deleted.
     * 
     * Active Status of Plugins will be saved in the preferences of the
     * Application, so next startup activation will be restored.
     * 
     * @param info
     *            which plugin should be deactivated
     * @return false, if any error occur, else true
     */
    public boolean deactivatePlugin(PluginInformation info) {

        // delete pluginHandler and delete from active map
        boolean success = activePlugins.get(info).onDeactivate();
        activePlugins.get(info).softKill();
        activePlugins.remove(info);

        // delete PluginName from prefs
        generalSettings.saveActivePlugins(activePlugins.keySet());

        // delete token and secret for Connection of plugin
        if (!verifiedPluginInfo.contains(info)) {
            // specific oauthserver for this plugin
            oauthServerModel.deleteOAuthServer(info);
        }

        setChanged();
        notifyObservers();
        return success;
    }

    /**
     * This method returns the SettingsPanel of a Plugin.
     * 
     * If the Plugin is not active, there is no need for configuration (and in
     * some Plugin no ability to configure). Therefore this method will return a
     * empty JPanel
     * 
     * @param info
     * @return
     */
    public JPanel getSettingsView(PluginInformation info) {
        if (info == null) {
            logger.throwing(new IllegalArgumentException("info must not be null"));
        }
        JPanel panel;
        if (isactivePlugin(info)) {
            panel = activePlugins.get(info).getPluginSettingsView();
        } else {
            panel = new JPanel();
        }
        return panel;
    }

    /**
     * Pauses all Plugins.
     * 
     * @return if is paused
     */
    public boolean pause() {
        logger.entry();
        boolean success = true;
        for (PluginHandler handler : activePlugins.values()) {
            success &= handler.onPause();
        }

        setChanged();
        notifyObservers();
        return logger.exit(success);
    }

    public boolean isRunning() {
        boolean running = false;
        for (PluginHandler handler : activePlugins.values()) {
            running |= handler.isAlive();
        }
        return logger.exit(running);
    }

    public synchronized boolean isPluginWorking(PluginInformation info) {
        PluginHandler handler = activePlugins.get(info);
        if (handler == null) {
            return false;
        } else {
            return handler.isAlive();
        }
    }

    /**
     * After pausing all Plugins they can be resumed.
     * 
     * @return if resumed
     */
    public boolean resume() {
        logger.entry();
        boolean success = true;
        for (PluginHandler handler : activePlugins.values()) {
            success &= handler.onResume();
        }

        setChanged();
        notifyObservers();
        return logger.exit(success);
    }

    private boolean verifySignature(JarFile jar) throws IOException {
        CertificateFactory factory;
        X509Certificate cert = null;
        try {
            factory = CertificateFactory.getInstance("X.509");
            // TODO check if file is in correct place
            InputStream is = getClass().getResourceAsStream("/publicCert.cer");
            cert = (X509Certificate) factory.generateCertificate(is);
        } catch (CertificateException e) {
            return false;
        }

        try {
            verify(jar, cert);
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }

    /**
     * First, retrieve the jar file from the URL passed in constructor. Then,
     * compare it to the expected X509Certificate. If everything went well and
     * the certificates are the same, no exception is thrown.
     */
    private void verify(JarFile jar, X509Certificate cert) throws IOException {
        // Sanity checking
        if (cert == null) {
            throw new IllegalArgumentException("Cert must not be null");
        }

        Vector<JarEntry> entriesVec = new Vector<JarEntry>();

        // Ensure the jar file is signed.
        Manifest man = jar.getManifest();
        if (man == null) {
            throw new SecurityException("Jar File not signed");
        }

        // Ensure all the entries' signatures verify correctly
        byte[] buffer = new byte[8192];
        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            JarEntry je = (JarEntry) entries.nextElement();

            // Skip directories.
            if (je.isDirectory())
                continue;
            entriesVec.addElement(je);
            InputStream is = jar.getInputStream(je);

            // Read in each jar entry. A security exception will
            // be thrown if a signature/digest check fails.
            @SuppressWarnings("unused")
            int n;
            while ((n = is.read(buffer, 0, buffer.length)) != -1) {
                // Don't care
            }
            is.close();
        }

        // Get the list of signer certificates
        Enumeration<JarEntry> e = entriesVec.elements();

        while (e.hasMoreElements()) {
            JarEntry je = (JarEntry) e.nextElement();

            // Every file must be signed except files in META-INF.
            Certificate[] certs = je.getCertificates();
            if ((certs == null) || (certs.length == 0)) {
                if (!je.getName().startsWith("META-INF"))
                    throw new SecurityException("The provider " + "has unsigned " + "class files.");
            } else {
                // Check whether the file is signed by the expected
                // signer. The jar may be signed by multiple signers.
                // See if one of the signers is 'targetCert'.
                int startIndex = 0;
                X509Certificate[] certChain;
                boolean signedAsExpected = false;

                while ((certChain = getAChain(certs, startIndex)) != null) {
                    if (certChain[0].equals(cert)) {
                        // Stop since one trusted signer is found.
                        signedAsExpected = true;
                        break;
                    }
                    // Proceed to the next chain.
                    startIndex += certChain.length;
                }

                if (!signedAsExpected) {
                    throw new SecurityException("The provider " + "is not signed by a " + "trusted signer");
                }
            }
        }
    }

    /**
     * Extracts ONE certificate chain from the specified certificate array which
     * may contain multiple certificate chains, starting from index
     * 'startIndex'.
     */
    private static X509Certificate[] getAChain(Certificate[] certs, int startIndex) {
        if (startIndex > certs.length - 1)
            return null;

        int i;
        // Keep going until the next certificate is not the
        // issuer of this certificate.
        for (i = startIndex; i < certs.length - 1; i++) {
            if (!((X509Certificate) certs[i + 1]).getSubjectDN().equals(((X509Certificate) certs[i]).getIssuerDN())) {
                break;
            }
        }
        // Construct and return the found certificate chain.
        int certChainSize = (i - startIndex) + 1;
        X509Certificate[] ret = new X509Certificate[certChainSize];
        for (int j = 0; j < certChainSize; j++) {
            ret[j] = (X509Certificate) certs[startIndex + j];
        }
        return ret;
    }

}
