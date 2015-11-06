package de.danner_web.studip_client.plugins.file_downloader.treeModel;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This Class represents a FolderNode.
 * 
 * A folder has a ID, name, make date, change date and permissions, as well as a
 * list of subfolders and files.
 * 
 * @author Dominik Danner
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FolderNode extends InnerNode {

	private static final long serialVersionUID = 5830465905112906777L;

	public String folder_id;
	public String name;
	public Long mkdate;
	public Long chdate;
	public Permissions permissions;

	public static class Permissions implements Serializable {
		private static final long serialVersionUID = -5208417588966394663L;

		public boolean visible;
		public boolean writable;
		public boolean readable;
		public boolean extendable;
	}

	@XmlElementWrapper(name="subfolders")
	@XmlElement(name = "subfolder")
	public List<FolderNode> subfolders = new LinkedList<FolderNode>();
	@XmlElementWrapper(name="files")
	@XmlElement(name = "file")
	public List<DocumentLeaf> files = new LinkedList<DocumentLeaf>();

	/**
	 * Constructor for this Object.
	 */
	public FolderNode() {
	}

	/**
	 * Returns the lastest changeDate of all files in the tree
	 * 
	 * @return latest changedate
	 */
	public long getLastChdate() {
		long chdate = 0L;
		long tmpChdate;
		for (FolderNode folder : subfolders) {
			tmpChdate = folder.getLastChdate();
			if (tmpChdate > chdate) {
				chdate = tmpChdate;
			}
		}

		for (DocumentLeaf file : files) {
			tmpChdate = file.getLastChdate();
			if (tmpChdate > chdate) {
				chdate = tmpChdate;
			}
		}

		return chdate;
	}

	/**
	 * This method merges this folder recursively with the given folder.
	 * 
	 * @param folder
	 *            new folder to merge
	 * @return true if updateFlag is set somewhere, otherwise false
	 */
	public void update(FolderNode newFolderNode) {
		// only update, if the given folder_id is the same
		if (this.folder_id == newFolderNode.folder_id) {
			this.name = newFolderNode.name;
			this.mkdate = newFolderNode.mkdate;
			this.chdate = newFolderNode.chdate;
			this.permissions = newFolderNode.permissions;

			List<FolderNode> subfolders2 = new LinkedList<FolderNode>(
					subfolders);
			// Update subfolders recursively
			for (FolderNode folderNode : newFolderNode.subfolders) {
				subfolders2.remove(folderNode);
				if (!this.subfolders.contains(folderNode)) {
					folderNode.setDownloadActive(this.isDownloadActive());
					this.subfolders.add(folderNode);
				} else {
					FolderNode modify = this.subfolders.get(this.subfolders
							.indexOf(folderNode));
					modify.update(folderNode);
				}
			}

			// Delete Subfolders that are not on Server
			for (FolderNode folderNode : subfolders2) {
				folderNode.deleteDataStructure();
				subfolders.remove(folderNode);
			}

			List<DocumentLeaf> files2 = new LinkedList<DocumentLeaf>(files);

			// Update all Files in this folder
			for (DocumentLeaf documentLeaf : newFolderNode.files) {
				files2.remove(documentLeaf);
				if (!this.files.contains(documentLeaf)) {
					documentLeaf.setDownloadActive(this.isDownloadActive());
					this.files.add(documentLeaf);
				} else {
					DocumentLeaf modify = this.files.get(this.files
							.indexOf(documentLeaf));
					modify.update(documentLeaf);
				}
			}

			// Delete Files that are not on server
			for (DocumentLeaf documentLeaf : files2) {
				documentLeaf.deleteDataStructure();
				files.remove(documentLeaf);
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((folder_id == null) ? 0 : folder_id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FolderNode other = (FolderNode) obj;
		if (folder_id == null) {
			if (other.folder_id != null)
				return false;
		} else if (!folder_id.equals(other.folder_id))
			return false;
		return true;
	}

	@Override
	public String getName() {
		return super.getName() == null ? replaceChars(this.name) : super
				.getName();
	}

	@Override
	protected List<Node> getChildren() {
		List<Node> list = new LinkedList<Node>(subfolders);
		list.addAll(files);
		return list;
	}

}
