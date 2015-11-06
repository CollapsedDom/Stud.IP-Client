package de.danner_web.studip_client.plugins.file_downloader.treeModel;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import javax.swing.tree.MutableTreeNode;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This abstract Class has all options for a Node in the Meta tree of folder and
 * files.
 * 
 * One file or folder has following options
 * 
 * 1. Name: You can rename the folder/file on disk (if you do not like the names
 * from StudIP)
 * 
 * 2. Hide: You can hide the folders on disk (if you do not like the folder
 * structure from StudIP)
 * 
 * 3. Download: You can mark a folder/file to be download or not (if you do not
 * want some useless files from StudIP)
 * 
 * @author Dominik Danner
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Node implements Serializable {

	private static final long serialVersionUID = 5917036776503465138L;

	// Options
	@XmlAttribute
	private boolean downloadIsActive = false;
	@XmlAttribute
	private String alternateName = null;
	@XmlAttribute
	private String groupName = null;
	@XmlAttribute
	protected String myPath;
	@XmlTransient
	protected Node father;

	// Helper Functions
	private static final String BAD_REGEX = "[\\\\/]+";

	/**
	 * This static method replaces all invalid chars from the input.
	 * 
	 * After this method the given String can be used as a valid Name for an
	 * folder or file in Windows and Linux OS.
	 * 
	 * @param input
	 *            String that must be checked and replaced.
	 * @return valid String for OS.
	 */
	protected static final String replaceChars(String input) {
		if (input == null) {
			return null;
		} else {
			return input.replaceAll(BAD_REGEX, "_").trim();
		}
	}

	/**
	 * This static method deletes the whole directory given by the File path.
	 * 
	 * All files in this directory gets deleted recursively.
	 * 
	 * @param path
	 *            that gets deleted
	 * @return true if no error occurs, otherwise false.
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

	/**
	 * This static method creates a new Name for a file.
	 * 
	 * First it checks if a file with a given name already exists. If there is
	 * such file, this method changes the new name to "basename(i).extension"
	 * where iterates up. The first unused name that gets detected will be
	 * returned.
	 * 
	 * @param dir
	 *            in which directory no file with the given name should exist.
	 * @param baseName
	 *            of the file to be renamed
	 * @param extension
	 *            of the file to be renamed
	 * @return new Path to an (not yet existing) file that has a unique name.
	 */
	protected static Path findFileName(final Path dir, final String baseName,
			final String extension) {
		Path ret = Paths.get(dir.toString(),
				String.format("%s.%s", baseName, extension));
		if (!Files.exists(ret))
			return ret;

		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			ret = Paths.get(dir.toString(),
					String.format("%s(%d).%s", baseName, i, extension));
			if (!Files.exists(ret))
				return ret;
		}
		throw new IllegalStateException("What the...");
	}

	/**
	 * This static method creates a new Name for a folder.
	 * 
	 * First it checks if a folder with a given name already exists. If there is
	 * such folder, this method changes the new name to "basename(i)" where
	 * iterates up. The first unused name that gets detected will be returned.
	 * 
	 * @param dir
	 *            in which directory no folder with the given name should exist.
	 * @param baseName
	 *            of the folder to be renamed
	 * @return new Path to an (not yet existing) folder that has a unique name.
	 */
	protected static Path findFileName(final Path dir, final String baseName) {
		Path ret = Paths.get(dir.toString(), String.format("%s", baseName));
		if (!Files.exists(ret))
			return ret;

		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			ret = Paths.get(dir.toString(),
					String.format("%s(%d)", baseName, i));
			if (!Files.exists(ret))
				return ret;
		}
		throw new IllegalStateException("What the...");
	}

	/**
	 * This method returns the Path to the Node.
	 * 
	 * @return path to the Node.
	 */
	public String getPath() {
		return myPath;
	}

	/**
	 * This method updates the pointer to the father of this Node.
	 * 
	 * @param father
	 *            to be pointed at
	 */
	public void updateFather(Node father) {
		this.father = father;
	}

	/**
	 * This method deletes the data structure of this Node on the file system.
	 */
	public void deleteDataStructure() {
		deleteDirectory(new File(this.getPath() + File.separator
				+ this.getName()));
	}

	/**
	 * Getter for downloadIsActive.
	 * 
	 * @return download status of the Node.
	 */
	public boolean isDownloadActive() {
		return downloadIsActive;
	}

	/**
	 * Getter for the Name. T
	 * 
	 * This can also be the alternate name set by the user
	 * 
	 * @return name of the Node.
	 */
	public String getName() {
		return alternateName != null ? replaceChars(alternateName) : null;
	}

	/**
	 * Getter for the Group of the Node.
	 * 
	 * Each Node has the option to get grouped. Then a new folder with the group
	 * Name get created and all Nodes with that group are copied together.
	 * 
	 * @return grepName or null if there is no group name set.
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Setter for downloadIsActive.
	 * 
	 * If set to true the complete walkingpath to the tree root gets also
	 * activated.
	 * 
	 * @param active
	 *            status of this Node.
	 */
	public void setDownloadActive(boolean active) {
		superDownloadActivate(active);
		if (active && father != null) {
			// Father is off type Node. This method is called.
			father.superDownloadActivate(active);
		}
	}

	public void superDownloadActivate(boolean active) {
		this.downloadIsActive = active;
	}

	/**
	 * Setter for name.
	 * 
	 * If newName is null this means that the original name should be used
	 * again.
	 * 
	 * If the newName is a valid name the file/folder gets renamed.
	 * 
	 * @param newName
	 *            to which the Node should be renamed
	 * @return true if no error occurs, otherwise false
	 */
	public boolean changeName(String newName) {
		String oldName = this.getName();
		boolean success = false;
		if (newName == null) {
			// reset
			this.alternateName = null;
			success = this.changeName(oldName);
		} else {
			// set
			if (!newName.matches(BAD_REGEX)) {
				// Check if a File with the new Name already exists

				File shouldExist = new File(this.getAbsolutePath());
				this.alternateName = newName;
				File newFile = new File(this.getAbsolutePath());

				// There must exist a file with the oldName and no File with the
				// newName must exist.
				if (shouldExist.exists() && !newFile.exists()) {
					// Rename File
					success = shouldExist.renameTo(newFile);
				} else {
					success = true;
				}
			}
		}

		// Restore old Name at failure
		if (!success) {
			this.alternateName = oldName;
		}

		return success;
	}

	/**
	 * Setter for the group name.
	 * 
	 * If newName is null this means that the group option is disabled and the
	 * folder/file should appear at its original place.
	 * 
	 * If the newName is a valid name the group gets set and the file/folder
	 * gets copied into the correct group folder.
	 * 
	 * @param newName
	 *            to which the group should be set
	 * @return true if no error occurs, otherwise false
	 */
	public boolean changeGroupName(String newName) {
		boolean success = true;
		// get destination with old groupName
		File oldDestination = new File(this.getAbsolutePath());
		this.groupName = newName;

		if (newName != null) {
			// create Folder, if not already there
			File groupFolder = new File(this.getPath() + File.separator
					+ newName);
			if (!groupFolder.exists()) {
				success |= groupFolder.mkdir();
			}
		}

		// no getAbsolutePath gets new String with new groupName
		File newDestination = new File(this.getAbsolutePath());

		try {
			// Copy File
			Files.move(oldDestination.toPath(), newDestination.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			success &= false;
		}

		return success;
	}

	/**
	 * Abstract method to get a List of Documents to be download.
	 * 
	 * @return list of Leafs that must be download.
	 */
	public abstract List<Leaf> collectDocuments();

	/**
	 * Abstract method to update the corresponding data structure on the file
	 * system.
	 * 
	 * @param path
	 *            where this Node is located.
	 * @return true if no error occurs, otherwise false.
	 */
	public abstract boolean updateFileSystem(String path);

	/**
	 * Abstract method to create a MutableTreeNode for representation in the
	 * option dialog.
	 * 
	 * @return new MutableTreeNode Object.
	 */
	public abstract MutableTreeNode toTreeNode();

	/**
	 * Abstract method to determine the latest change date.
	 * 
	 * @return latest change date of this Node and its Children.
	 */
	public abstract long getLastChdate();

	/**
	 * This method returns the absolute path to the file/folder that Node
	 * represents.
	 * 
	 * If a InnerNode has set the Option Hide, this method gives back something
	 * wrong, but then this method is irrelevant, because a hidden folder does
	 * not have an absolute Path.
	 * 
	 * @return absolute Path as String.
	 */
	public String getAbsolutePath() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getPath());
		if (this.getGroupName() != null) {
			sb.append(File.separator + this.getGroupName());
		}
		sb.append(File.separator + this.getName());
		return sb.toString();
	}

	@Override
	public String toString() {
		return this.getName();
	}

	/**
	 * This method returns a String where all Objects under this Node must be
	 * saved.
	 * 
	 * @return String with the subPath
	 */
	public String getSubPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getPath());
		if (this.getGroupName() != null) {
			sb.append(File.separator + this.getGroupName());
		}
		return sb.toString();
	}

}
