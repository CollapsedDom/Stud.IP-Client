package de.danner_web.studip_client.utils;

public class VersionUtil {

	/**
	 * This Method compares two version numbers of the following form:
	 * X.X.X.X...
	 * 
	 * @param v1
	 * @param v2
	 * @return -1: if v1 is smaller, 0: if v1 and v2 are the same, 1: otherwise
	 */
	public static int compareVersions(String v1, String v2) {

		if (v1 == null && v2 == null) {
			return 0;
		} else if (v1 == null) {
			return -1;
		} else if (v2 == null) {
			return 1;
		}

		String[] v1Parts = v1.split("[.]");
		String[] v2Parts = v2.split("[.]");

		int maxLength = Math.max(v1Parts.length, v2Parts.length);

		for (int i = 0; i < maxLength; i++) {
			int v1Int = 0;
			if (i < v1Parts.length) {
				try {
					v1Int = Integer.parseInt(v1Parts[i]);
				} catch (NumberFormatException e) {
				}

			}
			int v2Int = 0;
			if (i < v2Parts.length) {
				try {
					v2Int = Integer.parseInt(v2Parts[i]);
				} catch (NumberFormatException e) {
				}
			}

			if (v1Int == v2Int) {
				continue;
			} else {
				if (v1Int < v2Int) {
					return -1;
				} else {
					return 1;
				}
			}
		}
		return 0;
	}
}
