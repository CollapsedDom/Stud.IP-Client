package de.danner_web.studip_client.plugins.file_downloader.treeModel;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This Class represents a CourseNode.
 * 
 * A course has a ID, course number and a title, as well as a list of Folders.
 * 
 * @author Dominik Danner
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseNode extends InnerNode {

	private static final long serialVersionUID = -4708003934139554204L;

	public String course_id;
	public String course_nr;
	public String course_type;
	public String title;

	@XmlElementWrapper(name = "folders")
	@XmlElement(name = "folder")
	public List<FolderNode> folders = new LinkedList<FolderNode>();

	/**
	 * Constructor for this Object.
	 */
	public CourseNode() {
	}

	/**
	 * Returns the lastest changeDate of all files in the tree
	 * 
	 * @return latest changedate
	 */
	public long getLastChdate() {
		long chdate = 0L;
		long tmpChdate;
		for (FolderNode folder : folders) {
			tmpChdate = folder.getLastChdate();
			if (tmpChdate > chdate) {
				chdate = tmpChdate;
			}
		}
		return chdate;
	}

	/**
	 * This method merges this course recursively with the given course.
	 * 
	 * @param course
	 *            new course to merge
	 * @return true if updateFlag is set somewhere, otherwise false
	 */
	public void update(CourseNode newCourseNode) {
		// only update, if the given course_id is the same
		if (this.course_id == newCourseNode.course_id) {
			this.course_nr = newCourseNode.course_nr;
			this.title = newCourseNode.title;

			List<FolderNode> folders2 = new LinkedList<FolderNode>(folders);

			for (FolderNode folderNode : newCourseNode.folders) {
				folders2.remove(folderNode);
				// if not exists create and copy subtree
				if (!this.folders.contains(folderNode)) {
					folderNode.setDownloadActive(this.isDownloadActive());
					this.folders.add(folderNode);
				} else {
					// is save because of contains
					final FolderNode modify = this.folders.get(this.folders.indexOf(folderNode));
					modify.update(folderNode);
				}
			}

			// Delete fodlers that are not on server
			for (FolderNode folderNode : folders2) {
				folderNode.deleteDataStructure();
				folders.remove(folderNode);
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((course_id == null) ? 0 : course_id.hashCode());
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
		CourseNode other = (CourseNode) obj;
		if (course_id == null) {
			if (other.course_id != null)
				return false;
		} else if (!course_id.equals(other.course_id))
			return false;
		return true;
	}

	@Override
	public String getName() {
		return super.getName() == null ? (replaceChars(course_nr) + " - " + replaceChars(title)
				+ (course_type == null ? "" : " (" + course_type + ")")) : super.getName();
	}

	@Override
	protected List<Node> getChildren() {
		LinkedList<Node> list = new LinkedList<Node>(folders);
		return list;
	}

}
