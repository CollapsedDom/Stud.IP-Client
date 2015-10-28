package de.danner_web.studip_client.model;

import java.awt.Desktop;
import java.io.IOException;
import java.util.Collection;
import java.util.Observable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.data.OAuthServer;
import de.danner_web.studip_client.plugin.PluginInformation;
import de.danner_web.studip_client.utils.oauth.OAuthConnector;
import de.danner_web.studip_client.utils.oauth.ResponseCode;

public class LoginModel extends Observable {

	/**
	 * Logger dieser Klasse.
	 */
	private static Logger logger = LogManager.getLogger(LoginModel.class);

	private boolean isServerSelected;
	private boolean isAuthorized;
	private boolean isRequested;

	private OAuthServer selectedServer;
	private OAuthConnector con;
	private Collection<OAuthServer> servers;

	private PluginInformation info;

	LoginModel(PluginInformation info) {
		this.info = info;
		this.servers = info.getServerList();
	}
	
	public OAuthConnector getOAuthConnector() {
		return con;
	}

	public boolean isServerSelected() {
		return isServerSelected;
	}

	public boolean isAutherized() {
		return isAuthorized;
	}

	public boolean isRequested() {
		return isRequested;
	}

	public void resetSelectedServer() {
		isServerSelected = false;
	}

	public Collection<OAuthServer> getServers() {
		return servers;
	}

	public PluginInformation getPluginInformation() {
		return info;
	}

	/**
	 * Wählt einen Server aus und prüft die Verbindung indem der RequestToken
	 * angefordert wird.
	 * 
	 * 
	 * @param server
	 *            zu dem verbunden werden soll
	 * @return true, wenn kein Fehler aufgetreten ist. false, wenn keine
	 *         Verbindung zum Server zustandekommt
	 */
	public boolean selectServer(OAuthServer server) {
		logger.entry();
		selectedServer = server;
		isServerSelected = true;

		setChanged();
		notifyObservers();

		return logger.exit(isServerSelected);
	}

	/**
	 * Holt sich das RequestToken und die damit verbundene authurl
	 * 
	 * 
	 * @param userName
	 *            Des Accounts
	 * @param password
	 *            zum Account
	 * @return true, wenn kein Fehler aufgetreten ist. false, wenn UserName,
	 *         Password nicht übereinstimmt oder Serverseitig ein Fehler
	 *         auftritt
	 */
	public ResponseCode getRequestToken() {
		logger.entry();
		ResponseCode response = ResponseCode.INTERNAL_ERROR;
		if (isServerSelected) {
			logger.debug("Server: " + selectedServer.getName());

			con = new OAuthConnector(selectedServer);
			logger.debug("OAuth con " + con.toString());
			response = con.getRequestToken();

			if (response == ResponseCode.SUCCESS) {
				isRequested = true;
			}
			setChanged();
			notifyObservers();
		}
		return logger.exit(response);
	}

	/**
	 * Holt sich das AccessToken
	 * 
	 * 
	 * @param userName
	 *            Des Accounts
	 * @param password
	 *            zum Account
	 * @return true, wenn kein Fehler aufgetreten ist. false, wenn UserName,
	 *         Password nicht übereinstimmt oder Serverseitig ein Fehler
	 *         auftritt
	 */
	public ResponseCode getAccessToken() {
		logger.entry();

		ResponseCode response = ResponseCode.INTERNAL_ERROR;

		if (!isRequested)
			return response;

		response = con.getAccessToken();

		if (response == ResponseCode.SUCCESS) {
			isAuthorized = true;
		}

		setChanged();
		notifyObservers();
		return logger.exit(response);
	}

	public void openBrowser() {
		logger.entry();

		// Open Browser to authorize Stud.IP Client app
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(con.getAuthUrI());
			} catch (IOException e) {
			    // do not spam conole
			}
		}
	}
}
