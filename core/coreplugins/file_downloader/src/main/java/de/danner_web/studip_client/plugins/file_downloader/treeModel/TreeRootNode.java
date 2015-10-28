package de.danner_web.studip_client.plugins.file_downloader.treeModel;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="TreeRootNode")
public class TreeRootNode extends InnerNode {

	public TreeRootNode() {
	}

	private static final long serialVersionUID = -2398207662947619268L;

	@XmlElementWrapper(name="semesters")
	public LinkedList<SemesterNode> semesters = new LinkedList<SemesterNode>();

	@XmlElementWrapper(name="groups")
	public LinkedList<GroupNode> groups = new LinkedList<GroupNode>();

	/**
	 * This method merges this tree recursively with the given tree.
	 * 
	 * @param tree
	 *            new tree to merge
	 * @return true if updateFlag is set somewhere, otherwise false
	 */
	public void updateSemesters(List<SemesterNode> newSemesterNodes) {
		if (newSemesterNodes != null) {
			for (SemesterNode newSemesterNode : newSemesterNodes) {
				newSemesterNode.updateFather(this);
				// if not existing add course (with subtree)
				if (!this.semesters.contains(newSemesterNode)) {
					newSemesterNode.setDownloadActive(this.isDownloadActive());
					this.semesters.add(newSemesterNode);
				} else {
					// is save, because of contains
					SemesterNode modify = this.semesters.get(this.semesters
							.indexOf(newSemesterNode));
					modify.update(newSemesterNode);
				}
			}
		}
	}

	/**
	 * This method merges this tree recursively with the given tree.
	 * 
	 * @param tree
	 *            new tree to merge
	 * @return true if updateFlag is set somewhere, otherwise false
	 */
	public void updateGroups(List<GroupNode> newGroupNodes) {
		if (newGroupNodes != null) {
			for (GroupNode groupNode : newGroupNodes) {
				// if not existing add course (with subtree)
				if (!this.groups.contains(groupNode)) {
					groupNode.setDownloadActive(this.isDownloadActive());
					this.groups.add(groupNode);
				} else {
					// is save, because of contains
					GroupNode modify = this.groups.get(this.groups
							.indexOf(groupNode));
					modify.update(groupNode);
				}
			}
		}
	}

	/**
	 * Returns the lastest changeDate of all files in the tree
	 * 
	 * @return latest changedate
	 */
	public long getLastChdate() {
		long chdate = 0L;
		long tmpChdate;
		for (SemesterNode semester : semesters) {
			tmpChdate = semester.getLastChdate();
			if (tmpChdate > chdate) {
				chdate = tmpChdate;
			}
		}

		return chdate;
	}

	public MutableTreeNode toTreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);
		for (SemesterNode semester : semesters) {
			node.add(semester.toTreeNode());
		}
		return node;
	}

	protected List<File> getChildrenAsFile() {
		ArrayList<File> files = new ArrayList<File>();
		for (SemesterNode semester : semesters) {
			files.add(new File(semester.getPath() + File.separator
					+ semester.getName()));
		}
		return files;
	}

	@Override
	protected List<Node> getChildren() {
		LinkedList<Node> list = new LinkedList<Node>(semesters);
		list.addAll(groups);
		return list;
	}

	public String getName() {
		return super.getName() == null ? replaceChars("error") : super.getName();
	}
	
	/**
	 * Deactivate Hide node
	 */
	@Override
	public boolean setNodeHide(boolean hide) {
		return false;
	}
	
	/**
	 * deactivate Folder for root node
	 */
	@Override
	public boolean updateFileSystem(String path) {
		boolean success = true;
		this.myPath = path;
		if (this.isDownloadActive()) {
			for (Node node : this.getChildren()) {
				success &= node.updateFileSystem(path);
			}
		}
		return success;
	}
	
	/**
	 * deactivate Folder for root node
	 */
	public List<Leaf> collectDocuments() {
		List<Leaf> updateFiles = new ArrayList<Leaf>();

		// if not needed or not existing: create directory
		File folder = new File(this.myPath);
		if (!(folder.exists() && folder.isDirectory())) {
			folder.mkdirs();
		}

		for (Node node : this.getChildren()) {
			updateFiles.addAll(node.collectDocuments());
		}
		return updateFiles;
	}

}
