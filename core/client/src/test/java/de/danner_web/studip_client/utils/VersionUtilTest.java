package de.danner_web.studip_client.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class VersionUtilTest {
	
	@Test
	public void versionCompare() {
	    assertEquals(VersionUtil.compareVersions("1.0", "1.0.0"), 0);
	    assertEquals(VersionUtil.compareVersions("1.0", "1.0.1"), -1);
	    assertEquals(VersionUtil.compareVersions("1.0", "0.0.0"), 1);
	    assertEquals(VersionUtil.compareVersions("0.0.0.1", "1.0.0"), -1);
	    assertEquals(VersionUtil.compareVersions("0.1", "0"), 1);
	    assertEquals(VersionUtil.compareVersions("0.1", ""), 1);
	    assertEquals(VersionUtil.compareVersions("0.1", ".1"), 0);
	    assertEquals(VersionUtil.compareVersions("0..1", ".1"), -1);
	    assertEquals(VersionUtil.compareVersions(".", ".1"), -1);
	    assertEquals(VersionUtil.compareVersions(null, "1.1"), -1);
	    assertEquals(VersionUtil.compareVersions("1.0.0", null), 1);
	    assertEquals(VersionUtil.compareVersions(null, null), 0);
	}

}
