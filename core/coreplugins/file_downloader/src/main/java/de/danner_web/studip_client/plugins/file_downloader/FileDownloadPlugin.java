package de.danner_web.studip_client.plugins.file_downloader;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JPanel;

import de.danner_web.studip_client.data.PluginSettings;
import de.danner_web.studip_client.model.Context;
import de.danner_web.studip_client.plugin.Plugin;
import de.danner_web.studip_client.plugins.file_downloader.view.SettingsView;
import de.danner_web.studip_client.utils.oauth.OAuthConnector;
import de.danner_web.studip_client.view.SettingsDialog;

/**
 * Plugin Class for the "Dokumenten Downloader".
 * 
 * @author Danner Dominik
 * 
 */
public class FileDownloadPlugin extends Plugin {

	/**
	 * Settings name where the target folder dir is saved.
	 */
	static final String SYNC_FOLDER = "sync_dir";

	/**
	 * The FileHanlder manages to download and store files from Stud.IP on the
	 * Computer
	 */
	private DefaultFileHandler fileHandler;

	/**
	 * Constructor for the Plugin
	 * 
	 * @param con
	 *            to the server
	 * @param settings
	 *            of this plugin
	 */
	public FileDownloadPlugin(Context context, OAuthConnector con, PluginSettings settings) {
		super(context, con, settings);

		// If not done yet: init settings for this plugin
		if (!settings.containsKey(SYNC_FOLDER)) {
			String homedir = System.getProperty("user.home") + File.separator + "StudIP Sync";
			settings.put(SYNC_FOLDER, homedir);
		}
		this.fileHandler = new DefaultFileHandler(context, con, settings);
	}

	/**
	 * When it is time to start the Plugin, this method gets called.
	 * 
	 * First the updateTree() method of the fileHanlder gets called to update
	 * the Metatree information. After that the result gets logged in the
	 * history.
	 */
	@Override
	public int doWork() {
		int numberOfFiles;
		try {
			numberOfFiles = fileHandler.updateTree();
		} catch (UpdateFailureException e) {
			context.appendHistory("Fehler beim Holen der Informationen.");
			return -1;
		}

		if (numberOfFiles > 0) {
			context.appendHistory("Es werden " + numberOfFiles + " Dateien heruntergeladen!");
		} else {
			context.appendHistory("Sync Ordner ist aktuell");
		}

		return 0;
	}

	@Override
	public JPanel buildsettingsGUI() {
		return new SettingsView(fileHandler);
	}

	@Override
	public boolean onActivate() {

		final SettingsDialog dialog = new SettingsDialog(buildsettingsGUI());
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dialog.dispose();
			}
		});
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		return true;
	}

	@Override
	public boolean onDeactivate() {
		fileHandler.getFileDownloader().stop();
		return true;
	}

	/**
	 * Stops the FileDownloader in the background.
	 */
	@Override
	public boolean onPause() {
		return fileHandler.getFileDownloader().stop();
	}

	/**
	 * Reactivates the FileDownloader in the background.
	 */
	@Override
	public boolean onResume() {
		return fileHandler.getFileDownloader().start();
	}

}
