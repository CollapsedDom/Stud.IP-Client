package de.danner_web.studip_client.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;

import de.danner_web.studip_client.Starter;
import de.danner_web.studip_client.utils.VersionUtil;

public class UpdateModel extends Observable {
    
    private int progress = 1;
    private String status = "";
    
    private final static String updateURL() {
        return "http://studip-client.danner-web.de/update.php";
    }
    
    private final static String versionURL(String id, String version) {
        return "http://studip-client.danner-web.de/update.php" + "?id=" + id + "&version="
                + version;
    }
    
    /**
     * This Path points to the Location where the Client is installed
     */
    private Path clientLocation;
    
    // Names for settings, also in registry/XML file
    private static final String INSTALL_ID = "installID";
    private static final String AUTOUPDATE = "autoUpdate";
    private static final String VERSION = "client_version";
    
    /**
     * Info extracted from prefs
     */
    private String version;
    private String id;
    private boolean autoUpdate;
    private String clientApp = null;
    
    /**
     * Temporary Path to download update.jar and extract it
     */
    private Path tmpDir, extractedPath;
    
    public UpdateModel() {
        extractPrefs();
        
        clientLocation = (new File(System.getProperty("user.dir"))).toPath();
        clientApp = clientLocation + File.separator + "StudIP_Client.jar";
        
        try {
            tmpDir = Files.createTempDirectory("StudIPSyncUpdaterTMP");
            tmpDir.toFile().deleteOnExit();
            extractedPath = (new File(
                    tmpDir.toFile().getAbsolutePath() + File.separator + "extracted")).toPath();
            extractedPath.toFile().deleteOnExit();
        } catch (IOException e) {
        }
    }
    
    private void extractPrefs() {
        // Insert default values
        this.autoUpdate = true;
        this.version = "0";
        this.clientApp = "";
        this.id = "1";
        
        Preferences rootPref = Preferences.userNodeForPackage(Starter.class);
        
        // Load from prefs if possible
        try {
            String[] allKeys = rootPref.keys();
            
            for (String key : allKeys) {
                String value = rootPref.get(key, "");
                if (!value.equals("")) {
                    switch (key) {
                    case INSTALL_ID:
                        this.id = value;
                        break;
                    case AUTOUPDATE:
                        this.autoUpdate = Boolean.parseBoolean(value);
                        break;
                    case VERSION:
                        this.version = value;
                        break;
                    default:
                        break;
                    }
                }
            }
            
        } catch (BackingStoreException e) {
        }
    }
    
    public boolean isAutoUpdate() {
        return autoUpdate;
    }
    
    public int getProgress() {
        return progress;
    }
    
    public String getStatusText() {
        return status;
    }
    
    public boolean isNewerVersionAvailable() {
        int responseCode = -1;
        try {
            URL url = new URL(versionURL(id, version));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            responseCode = connection.getResponseCode();
        } catch (IOException e) {
            System.out.println("Error while fetching information about newer version.");
            e.printStackTrace();
            return false;
        }
        
        // Only if code is 200, update is available
        return responseCode == 200;
    }
    
    public boolean isFirstInstall() {
        return !new File(clientApp).exists();
    }
    
    private JarFile downloadUpdate() {
        
        JarFile updateJar = null;
        
        int responseCode;
        HttpURLConnection connection = null;
        
        // Request information
        responseCode = -1;
        try {
            URL url = new URL(updateURL());
            connection = (HttpURLConnection) url.openConnection();
            
            connection.connect();
            
            responseCode = connection.getResponseCode();
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
        // Only if code is 200 update is available
        if (responseCode == 200) {
            
            /* Copy it to tmp File */
            try {
                // Create tmp File and download File to it
                Path tmpFile = Files.createTempFile(tmpDir, "update", ".jar");
                Files.copy(connection.getInputStream(), tmpFile,
                        StandardCopyOption.REPLACE_EXISTING);
                updateJar = new JarFile(tmpFile.toFile());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return updateJar;
    }
    
    private boolean verifySignature(JarFile jar) {
        CertificateFactory factory;
        X509Certificate cert = null;
        try {
            factory = CertificateFactory.getInstance("X.509");
            InputStream is = getClass().getResourceAsStream("/publicCert.cer");
            cert = (X509Certificate) factory.generateCertificate(is);
        } catch (CertificateException e) {
            e.printStackTrace();
            return false;
        }
        
        try {
            verify(cert, jar);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    /**
     * First, retrieve the jar file from the URL passed in constructor. Then,
     * compare it to the expected X509Certificate. If everything went well and
     * the certificates are the same, no exception is thrown.
     */
    private void verify(X509Certificate cert, JarFile jar) throws IOException {
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
                    throw new SecurityException(
                            "The provider " + "is not signed by a " + "trusted signer");
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
            if (!((X509Certificate) certs[i + 1]).getSubjectDN()
                    .equals(((X509Certificate) certs[i]).getIssuerDN())) {
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
    
    private boolean extractAndCopyUpdate(JarFile jar) {
        
        // Extract update.jar to a subfolder in tmpDir
        try {
            unzip(extractedPath.toFile(), jar);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (verifyVersionNumber()) {
            try {
                moveDir(extractedPath, clientLocation);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    /**
     * Exract Zip File to the temporary folder
     * 
     * @throws IOException
     */
    private void unzip(File extractTo, JarFile jar) throws IOException {
        ZipEntry entry;
        Enumeration<? extends JarEntry> e = jar.entries();
        while (e.hasMoreElements()) {
            entry = (JarEntry) e.nextElement();
            if (entry.isDirectory()) {
                (new File(extractTo.getAbsolutePath() + File.separator + entry.getName())).mkdir();
            } else {
                File newFile = new File(
                        extractTo.getAbsolutePath() + File.separator + entry.getName());
                newFile.mkdirs();
                newFile.createNewFile();
                Files.copy(jar.getInputStream(entry), newFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
    
    /**
     * Checks weather the given Update is a update or not.
     * 
     * Implicit check for data structure due to the JarFile StudIP_Sync.jar
     * 
     * @return true, if update.zip contains a new Version of StudIP_Sync.jar,
     *         otherwise false.
     */
    private boolean verifyVersionNumber() {
        // Get Version of the new Client
        File newJar = new File(extractedPath + File.separator + "StudIP_Client.jar");
        String newVersion = getVersion(newJar);
        
        // Get Version of the old Client if installed
        String oldVersion = "-1";
        if (clientLocation != null) {
            File oldJar = new File(clientLocation.toFile().getAbsolutePath() + File.separator
                    + "StudIP_Client.jar");
            oldVersion = getVersion(oldJar);
        }
        
        if (oldVersion == null) {
            return true;
        }
        return VersionUtil.compareVersions(newVersion, oldVersion) > 0;
    }
    
    /**
     * Returns the Version of the JarFile.
     * 
     * Therefore the JarFile must be a StudIP_Sync.jar with a Starter.class in
     * it.
     * 
     * 
     * @param file
     * @return
     */
    @SuppressWarnings("resource")
    private String getVersion(File file) {
        JarFile jarfile = null;
        try {
            jarfile = new JarFile(file);
        } catch (IOException e) {
            return null;
        }
        
        Enumeration<JarEntry> entry = jarfile.entries();
        
        URL url = null;
        try {
            url = new URL("jar:file:" + file.getAbsolutePath() + "!/");
        } catch (MalformedURLException e1) {
            return null;
        }
        
        URL[] urls = { url };
        URLClassLoader cl = URLClassLoader.newInstance(urls);
        
        while (entry.hasMoreElements()) {
            JarEntry je = (JarEntry) entry.nextElement();
            if (!je.isDirectory() && je.getName().endsWith("Starter.class")) {
                // -6 because of .class
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                
                Class<?> c;
                try {
                    c = (Class<?>) cl.loadClass(className);
                    String version = (String) (c.getDeclaredField("CLIENT_VERSION"))
                            .get(new String());
                    return version;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }
    
    /**
     * This method recursivly copies a Directory to another.
     * 
     * @param source
     *            from
     * @param target
     *            to
     * @throws IOException
     *             Failure
     */
    private static void moveDir(Path source, Path target) throws IOException {
        File[] files = source.toFile().listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                File targetDir = new File(
                        target.toFile().getAbsolutePath() + File.separator + file.getName());
                targetDir.mkdirs();
                moveDir(file.toPath(), targetDir.toPath());
            } else {
                File targetDir = new File(
                        target.toFile().getAbsolutePath() + File.separator + file.getName());
                        
                // Workaround for updating the updater
                if (file.getName().equals("updater.jar")) {
                    targetDir = new File(
                            target.toFile().getAbsolutePath() + File.separator + "updater_new.jar");
                }
                // Create file if it does not exist
                if (!targetDir.exists()) {
                    targetDir.createNewFile();
                }
                // Copy file
                Files.copy(file.toPath(), targetDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
    
    /**
     * Restarts the StudIP Client.
     */
    public void launchAndExit() {
        extractedPath.toFile().delete();
        tmpDir.toFile().delete();
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                // Start Updater
                ProcessBuilder pb = new ProcessBuilder("java", "-Dname=StudIP_Client", "-jar",
                        clientApp);
                pb.redirectOutput(Redirect.INHERIT);
                pb.redirectError(Redirect.INHERIT);
                try {
                    pb.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        
        System.exit(0);
    }
    
    /**
     * This method downloads the new "update.zip" and invokes the extern
     * UpdateHandler.
     * 
     * If an error occurs, this method exits with the http error code.
     * 
     * @return error_code or nothing, because of System.exit(0)
     */
    public boolean updateClient() {
        
        // Download new files
        status = "Downloading new files ...";
        progress = 25;
        setChanged();
        notifyObservers();
        
        JarFile jar = downloadUpdate();
        
        if (jar == null) {
            System.err.println("ERROR: Could not download the update jar.");
            return false;
        }
        
        // Verify new files
        status = "Verifing Signature ...";
        progress = 50;
        setChanged();
        notifyObservers();
        
        if (!verifySignature(jar)) {
            System.err.println("ERROR: The signature of the update is not valid or not trusted.");
            return false;
        }
        
        // Extracting files
        status = "Extracting files ...";
        progress = 75;
        setChanged();
        notifyObservers();
        if (!extractAndCopyUpdate(jar)) {
            System.err.println("ERROR: Extracting content and copying to install path failed.");
            return false;
        }
        
        status = "Done.";
        progress = 100;
        setChanged();
        notifyObservers();
        
        return true;
    }
}
