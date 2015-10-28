package de.danner_web.studip_client.plugins.file_downloader.treeModel;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * This abstract Class has all options for a Node that has children.
 * 
 * All algorithms and methods are overwritten to enhance it to deal with
 * children.
 * 
 * Also a new Option is added. You can hide a Node. If a Node is hidden no
 * folder gets created on the file system and all its children gets located on
 * folder higher in the folder hierarchy.
 * 
 * @author Dominik Danner
 */
public abstract class InnerNode extends Node implements Serializable {

	private static final long serialVersionUID = -8723702687576164614L;

	@XmlAttribute
	protected boolean hideNode = false;

	protected List<File> getChildrenAsFile() {
		List<File> files = new ArrayList<File>();
		for (Node node : this.getChildren()) {
			files.add(new File(node.getAbsolutePath()));
		}
		return files;
	}

	@Override
	public void updateFather(Node father) {
		super.updateFather(father);
		for (Node child : this.getChildren()) {
			child.updateFather(this);
		}
	}

	/**
	 * Abstract Getter method to get a list of all its children as nodes.
	 * 
	 * @return list of all children.
	 */
	protected abstract List<Node> getChildren();

	/**
	 * Getter mehtod to get the hidden status
	 * 
	 * @return true if this Node is set to hidden, otherwise false.
	 */
	public boolean isNodeHide() {
		return hideNode;
	}

	@Override
	public void setDownloadActive(boolean active) {
		super.setDownloadActive(active);

		for (Node node : this.getChildren()) {
			node.setDownloadActive(active);
		}
	}

	@Override
	public boolean changeName(String newName) {
		boolean success = super.changeName(newName);

		if (success) {
			for (Node node : this.getChildren()) {
				node.updateFileSystem(this.getSubPath());
			}
		}
		return success;
	}

	@Override
	public boolean changeGroupName(String newName) {
		boolean success = super.changeGroupName(newName);

		if (success) {
			for (Node node : this.getChildren()) {
				node.updateFileSystem(this.getSubPath());
			}
		}
		return success;
	}

	/**
	 * Setter Method for the hide option.
	 * 
	 * If set to hide, all children gets copied one folder higher in the folder
	 * hierarchy. If needed the file/folder gets renamed.
	 * 
	 * If hide is disabled, all its own children gets copied down the folder
	 * hierarchy.
	 * 
	 * @param hide
	 *            option
	 * @return true if no error occurs, otherwise false
	 */
	public boolean setNodeHide(boolean hide) {
		/*
		 * Restore hidden Mode to get correct Pathes to my own folder and the
		 * folder above.
		 * 
		 * Later: set the hidden Flag to the correct value.
		 */
		this.hideNode = true;
		boolean success = true;
		File aboveFolder = new File(this.getSubPath());
		File myFolder = new File(this.getAbsolutePath());
		this.hideNode = hide;

		if (hide) {
			if (aboveFolder.exists() && aboveFolder.isDirectory()) {
				// copy Files from Folder one folder higher
				for (File file : aboveFolder.listFiles()) {
					// First check for rename Files
					for (Node child : this.getChildren()) {
						if (child.getName().equals(file.getName())) {
							boolean renameSuccess = false;
							String[] tokens = child.getName().split(
									"\\.(?=[^\\.]+$)");
							Path test = null;
							if (tokens.length == 1) {
								test = findFileName(
										new File(this.getPath()).toPath(),
										tokens[0]);
							} else if (tokens.length == 2) {
								test = findFileName(
										new File(this.getPath()).toPath(),
										tokens[0], tokens[1]);
							} else {
								success = false;
							}

							renameSuccess = child.changeName(test.getFileName()
									.toString());
							if (!renameSuccess) {
								success = false;
							}
						}
					}
				}
			}

			if (myFolder.exists() && myFolder.isDirectory()) {
				for (File file : myFolder.listFiles()) {
					// Second move files
					if (success && file.exists() && aboveFolder.exists()
							&& aboveFolder.isDirectory()) {
						try {
							// Copy File
							Files.move(file.toPath(), aboveFolder.toPath()
									.resolve(file.getName()),
									StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							success &= false;
						}
					}
				}
			}

			if (success) {
				myFolder.delete();
			}

		} else {
			List<File> toCopy = this.getFilesToCopy();

			// Copy all files (from Subtree) to a new folder below. First check,
			// if the file must be moved to the folder below.
			myFolder.mkdir();
			for (File file : toCopy) {
				if (file.exists() && myFolder.exists()) {
					try {
						// Copy File
						Files.move(file.toPath(),
								myFolder.toPath().resolve(file.getName()),
								StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						success &= false;
					}
				}
			}
		}

		success &= this.updateFileSystem(this.getPath());
		return success;
	}

	/**
	 * Helper method to determine all Files that should exist on the level of
	 * this Node when it gets showed again.
	 * 
	 * This includes all Files from subnodes, when the subnode is also hidden.
	 * 
	 * @return list of files that must be copied down to the new created folder.
	 */
	protected List<File> getFilesToCopy() {
		List<File> toCopy = new LinkedList<File>();
		// Collect all Files that must get copied down.
		for (Node child : this.getChildren()) {
			if (child instanceof Leaf
					|| (child instanceof InnerNode && !((InnerNode) child)
							.isNodeHide())) {
				toCopy.add(new File(child.getAbsolutePath()));
			}
			if (child instanceof InnerNode && ((InnerNode) child).isNodeHide()) {
				toCopy.addAll(((InnerNode) child).getFilesToCopy());
			}
		}
		return toCopy;
	}

	/**
	 * Implementation of the abstract method.
	 */
	public boolean updateFileSystem(String path) {
		boolean success = true;
		this.myPath = path;
		String subPath = this.getSubPath();
		if (this.isDownloadActive()) {
			// if not needed or not existing: create directory
			File folder = new File(subPath);
			if (!(folder.exists() && folder.isDirectory())) {
				success = folder.mkdirs();
			}

			for (Node node : this.getChildren()) {
				success &= node.updateFileSystem(subPath);
			}
		}
		return success;
	}

	/**
	 * Implementation of the abstract method.
	 */
	public List<Leaf> collectDocuments() {
		String subPath = this.getSubPath();
		List<Leaf> updateFiles = new ArrayList<Leaf>();

		if (this.isDownloadActive()) {
			// if not needed or not existing: create directory
			File folder = new File(subPath);
			if (!(folder.exists() && folder.isDirectory())) {
				folder.mkdirs();
			}

			for (Node node : this.getChildren()) {
				updateFiles.addAll(node.collectDocuments());
			}
		}
		return updateFiles;
	}

	/**
	 * Implementation of the abstract method.
	 */
	public MutableTreeNode toTreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);
		for (Node child : this.getChildren()) {
			node.add(child.toTreeNode());
		}
		return node;
	}

	@Override
	public String getSubPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.getSubPath());
		if (!this.isNodeHide()) {
			sb.append(File.separator + this.getName());
		}
		return sb.toString();
	}

}
