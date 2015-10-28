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

public class TreeHideTest {
	

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
	
	private File dir = new File(System.getProperty("user.home") + File.separator + "StudIP - test");
	
	private TreeRootNode tree;
	private SemesterNode sem, sem2;
	private CourseNode course, course2;
	private FolderNode folder;
	private FolderNode subfolder;
	private DocumentLeaf doc;
	
	@Before
	public void createTree(){
		tree = new TreeRootNode();
		List<SemesterNode> semList = new ArrayList<SemesterNode>();
		
		sem = new SemesterNode();
		sem.semester_id = "sem1";
		sem.title = "sem1";
		sem2 = new SemesterNode();
		sem2.semester_id = "sem2";
		sem2.title = "course1";
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
		semList.add(sem2);
		
		// Construct new Tree
		subfolder.files.add(doc);
		folder.subfolders.add(subfolder);
		course.folders.add(folder);
		sem.courses.add(course);
		sem2.courses.add(course2);
		
		// Add full Semester
		tree.updateSemesters(semList);
		
		tree.setDownloadActive(true);
		
		tree.updateFileSystem(dir.getAbsolutePath());
	}
	
	@After
	public void cleanFileSystem(){
		deleteDirectory(dir);
	}

	@Test
	public void hideSemester() {
		sem2.setNodeHide(true);
		File courseFolder = new File(dir + File.separator + course2.title);
		assertTrue(courseFolder.exists());
	}
	
	@Test
	public void hideSemesterRename(){
		sem.setNodeHide(true);
		File courseFolder = new File(dir + File.separator + course.title + "(0)");
		assertTrue(courseFolder.exists());
		assertTrue(dir.listFiles().length == 2);
	}

}
