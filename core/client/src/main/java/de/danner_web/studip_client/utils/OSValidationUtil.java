package de.danner_web.studip_client.utils;

public class OSValidationUtil {
	
	private static String OS = null;

	private static String getOsName() {
		if (OS == null) {
			OS = System.getProperty("os.name");
		}
		return OS;
	}

	public static boolean isWindows() {
		return getOsName().startsWith("Windows");
	}

	public static boolean isMac() {
		// TODO Implement
		return false;
	}

	public static boolean isLinux() {
		return getOsName().startsWith("Linux");
	}

}
