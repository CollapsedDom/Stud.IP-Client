package de.danner_web.studip_client.plugins.file_downloader.treeModel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This Class represents a GroupeNode.
 * 
 * @author Dominik Danner
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupNode extends InnerNode {

	private static final long serialVersionUID = -8828414828044403509L;

	/**
	 * Constructor for this Object.
	 */
	public GroupNode() {
	}

	@Override
	public long getLastChdate() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void update(GroupNode groupNode) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return super.getName() == null ? "" : super.getName();
	}

	@Override
	protected List<Node> getChildren() {
		return null;
	}

}
