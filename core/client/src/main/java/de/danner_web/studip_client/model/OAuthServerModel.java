package de.danner_web.studip_client.model;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Observable;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.Starter;
import de.danner_web.studip_client.data.OAuthServer;
import de.danner_web.studip_client.plugin.PluginInformation;

public class OAuthServerModel extends Observable {

    private static Logger logger = LogManager.getLogger(OAuthServerModel.class);

    private Preferences rootPref = Preferences.userNodeForPackage(Starter.class);
    private PluginModel pluginModel;
    private String installId;

    OAuthServerModel(PluginModel pluginModel, String installId) {
        this.pluginModel = pluginModel;
        this.installId = installId;
    }

    public boolean isDefaultOAuthServerSet() {
        Preferences node = rootPref;
        node = node.node("server");
        return !node.get(encrypt("accessTokenSecret", "accessTokenSecret"), "").equals("");
    }

    public OAuthServer getOAuthServer(PluginInformation pluginInfo) {
        Preferences node = rootPref;
        if (!pluginModel.isVerified(pluginInfo)) {
            node = rootPref.node("plugins").node(pluginInfo.getName());
        }
        node = node.node("server");

        // Base properties
        String name = decrypt(node.get(encrypt("name", "name"), ""), "name");
        String consumerKey = decrypt(node.get(encrypt("consumerKey", "consumerKey"), ""), "consumerKey");
        String consumerSecret = decrypt(node.get(encrypt("consumerSecret", "consumerSecret"), ""), "consumerSecret");
        String studipBaseUrl = decrypt(node.get(encrypt("studipBaseUrl", "studipBaseUrl"), ""), "studipBaseUrl");
        String accessUrl = decrypt(node.get(encrypt("accessUrl", "accessUrl"), ""), "accessUrl");
        String requestUrl = decrypt(node.get(encrypt("requestUrl", "requestUrl"), ""), "requestUrl");
        String authorizationUrl = decrypt(node.get(encrypt("authorizationUrl", "authorizationUrl"), ""), "authorizationUrl");
        String apiUrl = decrypt(node.get(encrypt("apiUrl", "apiUrl"), ""), "apiUrl");node.get(encrypt("", "apiUrl"), "");
        String contactEmail = decrypt(node.get(encrypt("contactEmail", "contactEmail"), ""), "contactEmail");

        // Additional properties
        String accessToken = decrypt(node.get(encrypt("accessToken", "accessToken"), ""), "accessToken");
        String accessTokenSecret = decrypt(node.get(encrypt("accessTokenSecret", "accessTokenSecret"), ""), "accessTokenSecret");

        OAuthServer server = new OAuthServer(name, consumerKey, consumerSecret, studipBaseUrl, "", apiUrl,
                contactEmail);
        server.setAuthorizationUrl(authorizationUrl);
        server.setRequestUrl(requestUrl);
        server.setAccessUrl(accessUrl);

        if (!accessToken.equals("") || !accessTokenSecret.equals("")) {
            server.setAccessToken(accessToken);
            server.setAccessTokenSecret(accessTokenSecret);
        } else {
            logger.warn("Internal Error: loaded OAuthServer " + server.getName() + " is not authorized.");
        }
        logger.info("OAuthServer loaded");

        return server;
    }

    public boolean deleteOAuthServer(PluginInformation pluginInfo) {
        Preferences node = rootPref;

        if (pluginInfo == null) {
            logger.debug("pluginInfo is null, delete default server");
        } else if (!pluginModel.isVerified(pluginInfo)) {
            node = rootPref.node("plugins").node(pluginInfo.getName());
        } else {
            return false;
        }
        node = node.node("server");

        try {
            node.removeNode();
        } catch (BackingStoreException e) {
            return false;
        }
        logger.debug("Deleted OAuthServer");
        return true;
    }

    public boolean saveOAuthServer(PluginInformation pluginInfo, OAuthServer server) {
        if (server == null) {
            throw logger.throwing(new IllegalArgumentException("server must not be null"));
        }

        if (pluginInfo == null) {
            throw logger.throwing(new IllegalArgumentException("plugininfo must not be null"));
        }

        Preferences node = rootPref;
        if (!pluginModel.isVerified(pluginInfo)) {
            node = rootPref.node("plugins").node(pluginInfo.getName());
        }
        node = node.node("server");

        // Base properties
        node.put(encrypt("name", "name"), encrypt(server.getName(), "name"));
        node.put(encrypt("consumerKey","consumerKey"), encrypt(server.getConsumerKey(), "consumerKey"));
        node.put(encrypt("consumerSecret", "consumerSecret"), encrypt(server.getConsumerSecret(), "consumerSecret"));
        node.put(encrypt("studipBaseUrl", "studipBaseUrl"), encrypt(server.getStudipBaseUrl(), "studipBaseUrl"));
        node.put(encrypt("accessUrl", "accessUrl"), encrypt(server.getAccessUrl(), "accessUrl"));
        node.put(encrypt("requestUrl", "requestUrl"), encrypt(server.getRequestUrl(), "requestUrl"));
        node.put(encrypt("authorizationUrl", "authorizationUrl"), encrypt(server.getAuthorizationUrl(), "authorizationUrl"));
        node.put(encrypt("apiUrl", "apiUrl"), encrypt(server.getApiUrl(), "apiUrl"));
        node.put(encrypt("contactEmail", "contactEmail"), encrypt(server.getContactEmail(), "contactEmail"));

        // Additional properties
        if (server.getAccessToken() != null && server.getAccessTokenSecret() != null) {
            node.put(encrypt("accessToken", "accessToken"), encrypt(server.getAccessToken(), "accessToken"));
            node.put(encrypt("accessTokenSecret", "accessTokenSecret"), encrypt(server.getAccessTokenSecret(), "accessTokenSecret"));
        } else {
            logger.warn("Internal Error: the given OAuthServer " + server.getStudipBaseUrl() + " is not authorized.");
        }
        logger.info("OAuthServer saved");
        return true;
    }

    private String encrypt(String plainText, String salt) {
        String encryptedValue = plainText;
        try {
            Key key = generateKey(salt);
            Cipher chiper = Cipher.getInstance("AES");
            chiper.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = chiper.doFinal(plainText.getBytes());
            encryptedValue = Base64.encodeBase64String(encVal);
        } catch (Exception e) {
            logger.warn("Somthing went wrong in preference encryption. Saved in plaintext.");
        }
        return encryptedValue;
    }

    private String decrypt(String encryptedText, String salt) {
        String decryptedValue = encryptedText;
        try {
            Key key = generateKey(salt);
            Cipher chiper = Cipher.getInstance("AES");
            chiper.init(Cipher.DECRYPT_MODE, key);
            byte[] decordedValue = Base64.decodeBase64(encryptedText);
            byte[] decValue = chiper.doFinal(decordedValue);
            decryptedValue = new String(decValue);
        } catch (Exception e) {
            logger.warn("Somthing went wrong in preference decryption. Loaded as plaintext.");
        }
        return decryptedValue;
    }

    private Key generateKey(String salt) throws Exception {
        byte[] key = (installId + salt).getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16); // use only first 128 bit
        return new SecretKeySpec(key, "AES");
    }
}
