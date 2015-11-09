package de.danner_web.studip_client;

import de.danner_web.studip_client.model.SettingsModel;
import de.danner_web.studip_client.utils.ResourceLoader;
import de.danner_web.studip_client.utils.VersionUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import de.danner_web.studip_client.model.Model;
import de.danner_web.studip_client.view.ViewController;

public class Starter {

	/**
	 * Logger dieser Klasse.
	 */
	// private static Logger logger = LogManager.getLogger(Starter.class);

	public static String getClientVersion() {
		Properties prop = new Properties();
		try {
			InputStream resourceAsStream = ResourceLoader.getURL("version.properties").openStream();
			prop.load(resourceAsStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop.getProperty("version");
	}

	public static void onUpdate(SettingsModel settings) {
		String oldVersion = settings.getVersion();

		File updaterNew = new File("updater_new.jar");
		if (updaterNew.exists()) {
			try {
				Files.copy(updaterNew.toPath(), new File("updater.jar").toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Files.delete(updaterNew.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (VersionUtil.compareVersions(oldVersion, getClientVersion()) < 0) {

			/*
			 * Add Update for new version here
			 * 
			 * If you want to update the update.jar you can do this here!
			 */
			// if (VersionUtil.compareVersions(oldVersion, "0.0.1") < 0) {
			// // EXAMPLE database changes from 0.0.1 on: add new Param to the
			// // Map of SettingsModel
			// }
			// if (VersionUtil.compareVersions(oldVersion, "0.1.0") < 0) {
			// // EXAMPLE database changes from 0.1.0 on: add new Param to the
			// // Map of SettingsModel
			// }
			// ...
			settings.setVersion(getClientVersion());
		}
	}

	/**
	 * Entry Method for StudIP Client
	 * 
	 * @param args
	 *            no args are needed.
	 */
	public static void main(String[] args) {

		if (args.length == 1 && "-d".equals(args[0])) {
			System.out.println("Delete Preferences from this Application");
			Preferences rootPref = Preferences.userNodeForPackage(Starter.class);
			try {
				rootPref.removeNode();
			} catch (BackingStoreException e) {
				e.printStackTrace();
				System.exit(1);
			}
			System.exit(0);
		}

		if (args.length == 1 && "-v".equals(args[0])) {
			System.out.println(getClientVersion());
			System.exit(0);
		}

		// Workaround for Bug #JDK-7075600, does not work proper
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

		// Bootstrap
		Model model = new Model();
		SettingsModel settings = model.getSettingsModel();

		// Update install folder to provide portability
		settings.setInstallFolder(System.getProperty("user.dir"));

		// Check on every start if the Client got updated, if yes perform some
		// changes.
		Starter.onUpdate(model.getSettingsModel());

		model.getPluginModel().init();
		new ViewController(model);
	}
}
