package de.danner_web.studip_client.plugins.file_downloader.treeModel;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * This abstract Class has all options for a Node that is a Leaf in the tree.
 * 
 * No special options are added.
 * 
 * @author Dominik Danner
 */
public abstract class Leaf extends Node implements Serializable {

	private static final long serialVersionUID = -7031661880525112366L;

	/**
	 * Implementation of the abstract method.
	 */
	public boolean updateFileSystem(String path) {
		this.myPath = path;
		return true;
	}

	/**
	 * Implementation of the abstract method.
	 */
	public List<Leaf> collectDocuments() {
		String subPath = this.getAbsolutePath();
		List<Leaf> updateFiles = new ArrayList<Leaf>();

		if (this.isDownloadActive()) {
			File file = new File(subPath);
			if (!(file.exists() && !file.isDirectory())) {
				updateFiles.add(this);
			}
		}
		return updateFiles;
	}

	/**
	 * Implementation of the abstract method.
	 */
	public MutableTreeNode toTreeNode() {
		return new DefaultMutableTreeNode(this, false);
	}

}
