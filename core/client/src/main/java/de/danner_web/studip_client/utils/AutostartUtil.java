package de.danner_web.studip_client.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.danner_web.studip_client.utils.OSValidationUtil;

public class AutostartUtil {

	private static Logger logger = LogManager.getLogger(AutostartUtil.class);

	// Windows specific
	private static final String SHORTCUT_NAME = "StudIP Client.lnk";
	private static final String EXE_NAME = "StudIP Client.exe";

	// Linux specific
	private static final String DESKTOP_FILE_NAME = "studip_client.desktop";
	private static final String SH_SCRIPT_NAME = "studip_client.sh";
	private static final String DESKTOP_PATH = ".config/autostart";

	// Mac specific

	public static boolean createAutoStartShortcut() {
		String installFolder = System.getProperty("user.dir");
		// Check OS
		if (OSValidationUtil.isLinux()) {
			// Check if sh File and desktop file are in the correct folder
			File shScriptFile = new File(installFolder + "/" + SH_SCRIPT_NAME);
			if (!shScriptFile.exists() || shScriptFile.isDirectory()) {
				logger.warn("\"" + SH_SCRIPT_NAME + "\" could not be found in "
						+ installFolder + ". No startup shortcut created.");
				return false;
			}

			// check if already set to autostart
			String userHome = System.getProperty("user.home");
			File desktopPath = new File(userHome + File.separator
					+ DESKTOP_PATH);
			File desktopTargetFile = new File(desktopPath.getAbsolutePath()
					+ File.separator + DESKTOP_FILE_NAME);
			if (!desktopPath.isDirectory()) {
				logger.warn("Autostart dir " + desktopPath.getAbsolutePath()
						+ "is no folder");
				return false;
			}
			if (desktopTargetFile.exists() && desktopTargetFile.isFile()) {
				logger.warn("Autostart file already exists");
				return true;
			}

			String desktopFileContent = "[Desktop Entry]\n"
					+ "Type=Application\n" + "Name[de_DE]=Stud.IP Client\n"
					+ "Name=Stud.IP Client\n"
					+ "Exec=" + userHome + File.separator + ".studip_client/studip_client.sh\n"
					+ "Icon=" + userHome + File.separator + ".studip_client/studip.ico\n"
					+ "Comment[de_DE]=Stud.IP Client\n"
					+ "Comment=Stud.IP Client\n" + "Terminal=false\n"
					+ "NoDisplay=false\n" + "X-GNOME-Autostart-enabled=true\n";
			FileWriter fw;
			try {
				fw = new FileWriter(desktopTargetFile);
				fw.write(desktopFileContent);
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}

			return true;
		} else if (OSValidationUtil.isMac()) {
			// TODO implement
			return false;
		} else if (OSValidationUtil.isWindows()) {
			// Check if "Studip Client.exe" exists
			File f = new File(installFolder + "/" + EXE_NAME);
			if (!f.exists() || f.isDirectory()) {
				logger.warn("\"" + EXE_NAME + "\" could not be found in "
						+ installFolder + ". No startup shortcut created.");
				return false;
			}

			try {
				File file = File.createTempFile(
						"studip_client_create_auostart_shortcut", ".vbs");
				file.deleteOnExit();
				FileWriter fw = new FileWriter(file);

				String vbs = "Set wsc = WScript.CreateObject(\"WScript.Shell\")\n"
						+ "Set lnk = wsc.CreateShortcut(wsc.SpecialFolders(\"startup\") & \"\\"
						+ SHORTCUT_NAME
						+ "\")\n"
						+ "lnk.targetpath = \""
						+ installFolder
						+ "\\"
						+ EXE_NAME
						+ "\"\n"
						+ "lnk.description = \"Stud.IP Client Autostart\"\n"
						+ "lnk.workingdirectory = \""
						+ installFolder
						+ "\"\n"
						+ "lnk.IconLocation = \""
						+ installFolder
						+ "\\"
						+ EXE_NAME + ", 0\"\n" + "lnk.save\n";
				fw.write(vbs);
				fw.close();
				Process p = Runtime.getRuntime().exec(
						"cscript //NoLogo " + file.getPath());
				BufferedReader input = new BufferedReader(
						new InputStreamReader(p.getInputStream()));
				input.readLine();
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		} else {
			logger.warn("This is not a windows, Linux or mac machine. No startup shortcut created.");
			return false;
		}
	}

	public static boolean deleteAutoStartShortcut() {
		// Check OS
		if (OSValidationUtil.isLinux()) {
			// check if set to autostart
			String userHome = System.getProperty("user.home");
			File desktopPath = new File(userHome + File.separator
					+ DESKTOP_PATH);
			File desktopTargetFile = new File(desktopPath.getAbsolutePath()
					+ File.separator + DESKTOP_FILE_NAME);
			if (!desktopPath.isDirectory()) {
				logger.warn("Autostart dir " + desktopPath.getAbsolutePath()
						+ "is no folder");
				return false;
			}
			if (!desktopTargetFile.exists() || !desktopTargetFile.isFile()) {
				logger.warn("Autostart file does not exist or is a folder");
				return true;
			}

			// Delete .desktop file to folder
			try {
				Files.delete(desktopTargetFile.toPath());
			} catch (IOException e) {
				logger.warn("Could not delete .desktop file to target Location "
						+ desktopTargetFile.getAbsolutePath());
				return false;
			}
			return true;
		} else if (OSValidationUtil.isMac()) {
			// TODO implement
			return false;
		} else if (OSValidationUtil.isWindows()) {

			// Get startup folder
			String result = "";
			try {
				File file = File.createTempFile("realhowto", ".vbs");
				file.deleteOnExit();
				FileWriter fw = new java.io.FileWriter(file);

				String vbs = "Set WshShell = WScript.CreateObject(\"WScript.Shell\")\n"
						+ "wscript.echo WshShell.SpecialFolders(\"startup\")\n"
						+ "Set WSHShell = Nothing\n";

				fw.write(vbs);
				fw.close();
				Process p = Runtime.getRuntime().exec(
						"cscript //NoLogo " + file.getPath());
				BufferedReader input = new BufferedReader(
						new InputStreamReader(p.getInputStream()));
				result = input.readLine();
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Delete shortcut if exists
			File f = new File(result + "/StudIP Client.lnk");
			if (f.exists() && !f.isDirectory()) {
				return f.delete();
			}
			return false;
		} else {
			logger.warn("This is not a windows, Linux or mac machine. No startup shortcut created.");
			return false;
		}
	}
}
