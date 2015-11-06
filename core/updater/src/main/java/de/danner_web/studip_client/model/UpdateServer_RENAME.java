package de.danner_web.studip_client.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

class UpdateServer_RENAME {

	/**
	 * This method returns the current version of the studip client as
	 * InputStream. Its implementation could be reading from Internet or File.
	 * 
	 * @return
	 */
	static InputStream getCurrentVersionAsInputStream() {
		try {
			return new FileInputStream(
					new File(".." + File.separator + "build" + File.separator + "currentversion_signed.jar"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method returns the url as string to the release update server. Id is
	 * needed only needed for statistics. Version should be the current version
	 * of the installed client.
	 * 
	 * @param id
	 *            installation id
	 * @param version
	 *            current version
	 * @return url to check for new version
	 * @throws MalformedURLException
	 */
	final static URL versionURL(String id, String version) throws MalformedURLException {
		return new URL("http://studip-client.danner-web.de/update_dev.php" + "?id=" + id + "&version=" + version);
	}

}
