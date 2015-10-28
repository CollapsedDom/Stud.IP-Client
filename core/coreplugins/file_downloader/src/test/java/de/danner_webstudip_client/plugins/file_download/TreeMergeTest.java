package de.danner_webstudip_client.plugins.file_download;

import static org.junit.Assert.*;

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

public class TreeMergeTest {
	
	private TreeRootNode tree;
	
	@Before
	public void createTree(){
		tree = new TreeRootNode();
	}
	
	@After
	public void cleanFileSystem(){
		
	}

	@Test
	public void AddItems() {
		List<SemesterNode> semList = new ArrayList<SemesterNode>();
		
		SemesterNode sem = new SemesterNode();
		sem.semester_id = "sem1";
		sem.title = "sem1";
		CourseNode course = new CourseNode();
		course.course_id = "c1";
		course.title = "course1";
		FolderNode folder = new FolderNode();
		folder.folder_id = "f1";
		folder.name = "folder1";
		FolderNode subfolder = new FolderNode();
		subfolder.folder_id = "f2";
		subfolder.name = "subfolder1";
		DocumentLeaf doc = new DocumentLeaf();
		doc.document_id = "doc1";
		doc.filename = "file1";
		doc.name = "filename1";
		semList.add(sem);
		
		// Construct new Tree
		subfolder.files.add(doc);
		folder.subfolders.add(subfolder);
		course.folders.add(folder);
		sem.courses.add(course);
		
		// Add full Semester
		assertFalse(tree.semesters.contains(sem));
		tree.updateSemesters(semList);
		
		assertTrue(tree.semesters.contains(sem));
		assertTrue(tree.semesters.get(0).courses.contains(course));
		assertTrue(tree.semesters.get(0).courses.get(0).folders.contains(folder));
		assertTrue(tree.semesters.get(0).courses.get(0).folders.get(0).subfolders.contains(subfolder));
		assertTrue(tree.semesters.get(0).courses.get(0).folders.get(0).subfolders.get(0).files.contains(doc));
	}

}
