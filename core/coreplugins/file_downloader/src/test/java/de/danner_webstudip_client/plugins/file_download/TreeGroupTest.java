package de.danner_webstudip_client.plugins.file_download;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.danner_web.studip_client.plugins.file_downloader.treeModel.CourseNode;
import de.danner_web.studip_client.plugins.file_downloader.treeModel.DocumentLeaf;
import de.danner_web.studip_client.plugins.file_downloader.treeModel.FolderNode;
import de.danner_web.studip_client.plugins.file_downloader.treeModel.SemesterNode;
import de.danner_web.studip_client.plugins.file_downloader.treeModel.TreeRootNode;


public class TreeGroupTest {

	/**
	 * Simple Method to delete a Path recursively.
	 * 
	 * @param path
	 *            to be deleted
	 * @return true if success otherwise false.
	 */
	private static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDirectory(files[i]);
					} else {
						files[i].delete();
					}
				}
			}
		}
		return (path.delete());
	}

	private File dir = new File(System.getProperty("user.home")
			+ File.separator + "StudIP - test");

	private TreeRootNode tree;
	private SemesterNode sem;
	private CourseNode course, course2;
	private FolderNode folder;
	private FolderNode subfolder;
	private DocumentLeaf doc;

	@Before
	public void createTree() {
		tree = new TreeRootNode();
		List<SemesterNode> semList = new ArrayList<SemesterNode>();

		sem = new SemesterNode();
		sem.semester_id = "sem1";
		sem.title = "sem1";
		course = new CourseNode();
		course.course_id = "c1";
		course.title = "course1";
		course2 = new CourseNode();
		course2.course_id = "c2";
		course2.title = "course2";
		folder = new FolderNode();
		folder.folder_id = "f1";
		folder.name = "folder1";
		subfolder = new FolderNode();
		subfolder.folder_id = "f2";
		subfolder.name = "subfolder1";
		doc = new DocumentLeaf();
		doc.document_id = "doc1";
		doc.filename = "file1";
		doc.name = "filename1";
		
		semList.add(sem);

		// Construct new Tree
		subfolder.files.add(doc);
		folder.subfolders.add(subfolder);
		course.folders.add(folder);
		sem.courses.add(course);
		sem.courses.add(course2);
		
		// Add full Semester
		tree.updateSemesters(semList);
		
		tree.setDownloadActive(true);

		tree.updateFileSystem(dir.getAbsolutePath());
	}

	@After
	public void cleanFileSystem() {
		deleteDirectory(dir);
	}

	@Test
	public void groupCourses() {
		course.changeGroupName("TestGroup");
		course2.changeGroupName("TestGroup");

		File groupeFolder = new File(sem.getAbsolutePath() + File.separator
				+ "TestGroup");
		File courseFolder = new File(sem.getAbsolutePath() + File.separator
				+ "TestGroup" + File.separator + course.getName());
		File course2Folder = new File(sem.getAbsolutePath() + File.separator
				+ "TestGroup" + File.separator + course2.getName());

		assertTrue(groupeFolder.exists());
		assertTrue(courseFolder.exists());
		assertTrue(course2Folder.exists());
		
		assertEquals("/home/dominik/StudIP - test/sem1/TestGroup/course1", course.getAbsolutePath());
		assertEquals("/home/dominik/StudIP - test/sem1", course.getPath());
		
		assertEquals("/home/dominik/StudIP - test/sem1/TestGroup/course2", course2.getAbsolutePath());
		assertEquals("/home/dominik/StudIP - test/sem1", course2.getPath());
		
		assertEquals("/home/dominik/StudIP - test/sem1/TestGroup/course1/folder1", folder.getAbsolutePath());
		assertEquals("/home/dominik/StudIP - test/sem1/TestGroup/course1", folder.getPath());
		
	}

	@Test
	public void resetGroupe() {
		groupCourses();
		course.changeGroupName(null);
		File groupeFolder = new File(sem.getAbsolutePath() + File.separator
				+ "TestGroup");
		File oldCourseFolder = new File(sem.getAbsolutePath() + File.separator
				+ "TestGroup" + File.separator + course.getName());
		File courseFolder = new File(sem.getAbsolutePath() + File.separator
				+ course.getName());
		File course2Folder = new File(sem.getAbsolutePath() + File.separator
				+ "TestGroup" + File.separator + course2.getName());

		assertTrue(groupeFolder.exists());
		assertTrue(courseFolder.exists());
		assertFalse(oldCourseFolder.exists());
		assertTrue(course2Folder.exists());
		
		assertEquals("/home/dominik/StudIP - test/sem1/course1", course.getAbsolutePath());
		assertEquals("/home/dominik/StudIP - test/sem1", course.getPath());
	}
	
	@Test
	public void changeOneGroup() {
		groupCourses();
		course.changeGroupName("TestGroup2");
		File groupeFolder = new File(sem.getAbsolutePath() + File.separator
				+ "TestGroup");
		File oldCourseFolder = new File(sem.getAbsolutePath() + File.separator
				+ "TestGroup" + File.separator + course.getName());
		File courseFolder = new File(sem.getAbsolutePath() + File.separator
				+ "TestGroup2" + File.separator + course.getName());
		File course2Folder = new File(sem.getAbsolutePath() + File.separator
				+ "TestGroup" + File.separator + course2.getName());

		assertTrue(groupeFolder.exists());
		assertTrue(courseFolder.exists());
		assertFalse(oldCourseFolder.exists());
		assertTrue(course2Folder.exists());
		
		assertEquals("/home/dominik/StudIP - test/sem1/TestGroup2/course1", course.getAbsolutePath());
		assertEquals("/home/dominik/StudIP - test/sem1", course.getPath());
	}

	@Test
	public void groupCoursesAndRename() {
		groupCourses();
		boolean success = course.changeName("Vorlesung");
		success &= course2.changeName("Übung");
		
		assertTrue(success);

		File courseFolder = new File(sem.getAbsolutePath() + File.separator
				+ "TestGroup" + File.separator + course.getName());
		File course2Folder = new File(sem.getAbsolutePath() + File.separator
				+ "TestGroup" + File.separator + course2.getName());

		assertTrue(courseFolder.exists());
		assertTrue(course2Folder.exists());
		
		assertEquals("/home/dominik/StudIP - test/sem1/TestGroup/Vorlesung", course.getAbsolutePath());
		assertEquals("/home/dominik/StudIP - test/sem1", course.getPath());
	}
	
	@Test
	public void groupAndHideCourses() {
		course.changeGroupName("TestGroup");
		course2.changeGroupName("TestGroup");
		course.setNodeHide(true);

		File groupeFolder = new File(sem.getAbsolutePath() + File.separator
				+ "TestGroup");
		File courseFolder = new File(sem.getAbsolutePath() + File.separator
				+ "TestGroup" + File.separator + folder.getName());
		File course2Folder = new File(sem.getAbsolutePath() + File.separator
				+ "TestGroup" + File.separator + course2.getName());

		assertTrue(groupeFolder.exists());
		assertTrue(courseFolder.exists());
		assertTrue(course2Folder.exists());
		
		assertEquals("/home/dominik/StudIP - test/sem1/TestGroup/course1", course.getAbsolutePath());
		assertEquals("/home/dominik/StudIP - test/sem1", course.getPath());
		
		assertEquals("/home/dominik/StudIP - test/sem1/TestGroup/course2", course2.getAbsolutePath());
		assertEquals("/home/dominik/StudIP - test/sem1", course2.getPath());
		
		assertEquals("/home/dominik/StudIP - test/sem1/TestGroup/folder1", folder.getAbsolutePath());
		assertEquals("/home/dominik/StudIP - test/sem1/TestGroup", folder.getPath());
		
	}

}
