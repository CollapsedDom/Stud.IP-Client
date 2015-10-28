package de.danner_webstudip_client.plugins.file_download;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	TreeHideTest.class,
	TreeMergeTest.class,
	TreeRenameTest.class,
	TreeGroupTest.class
	})
public class AllTests {

}
