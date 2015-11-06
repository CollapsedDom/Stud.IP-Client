package de.danner_web.studip_client.plugins.file_downloader.treeModel;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This Class represents a SemesterNode.
 * 
 * A semester has a ID, title and a description, as well as a list of courses
 * registered to that semester.
 * 
 * @author Dominik Danner
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SemesterNode extends InnerNode {

	private static final long serialVersionUID = -6756919944979024647L;

	public String semester_id;
	public String title;
	public Long start;
	public String description;

	@XmlElementWrapper(name="courses")
	@XmlElement(name = "course")
	public List<CourseNode> courses = new LinkedList<CourseNode>();

	/**
	 * Constructor for this Object.
	 * 
	 * Guarantees that this Object has an unique id.
	 * 
	 * @param semester_id
	 */
	public SemesterNode() {
	}

	/**
	 * Returns the lastest changeDate of all files in the tree
	 * 
	 * @return latest changedate
	 */
	public long getLastChdate() {
		long chdate = 0L;
		long tmpChdate;
		for (CourseNode course : courses) {
			tmpChdate = course.getLastChdate();
			if (tmpChdate < chdate) {
				chdate = tmpChdate;
			}
		}

		return chdate;
	}

	/**
	 * This method merges this tree recursively with the given tree.
	 * 
	 * @param tree
	 *            new tree to merge
	 * @return true if updateFlag is set somewhere, otherwise false
	 */
	public void update(SemesterNode newSemesterNode) {
		// only update, if the givven semester_id is the same
		if (this.equals(newSemesterNode)) {
			this.title = newSemesterNode.title;
			this.description = newSemesterNode.description;

			if (!newSemesterNode.courses.isEmpty()) {
				// update subTree
				List<CourseNode> courses2 = new LinkedList<CourseNode>(courses);

				for (CourseNode courseNode : newSemesterNode.courses) {
					courses2.remove(courseNode);
					// if not existing add course (with subtree)
					if (!this.courses.contains(courseNode)) {
						courseNode.setDownloadActive(this.isDownloadActive());
						this.courses.add(courseNode);
					} else {
						// is save, because of contains
						CourseNode modify = this.courses.get(this.courses
								.indexOf(courseNode));
						modify.update(courseNode);
					}
				}

				// Delete Courses that are not on the Server
				for (CourseNode courseNode : courses2) {
					courseNode.deleteDataStructure();
					courses.remove(courseNode);
				}
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((semester_id == null) ? 0 : semester_id.hashCode());
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
		SemesterNode other = (SemesterNode) obj;
		if (semester_id == null) {
			if (other.semester_id != null)
				return false;
		} else if (!semester_id.equals(other.semester_id))
			return false;
		return true;
	}

	@Override
	public String getName() {
		return super.getName() == null ? replaceChars(this.title) : super
				.getName();
	}

	@Override
	protected List<Node> getChildren() {
		LinkedList<Node> list = new LinkedList<Node>(courses);
		return list;
	}

}
