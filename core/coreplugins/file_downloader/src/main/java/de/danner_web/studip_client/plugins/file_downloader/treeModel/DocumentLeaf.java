package de.danner_web.studip_client.plugins.file_downloader.treeModel;

import javax.xml.bind.annotation.XmlAttribute;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This Class represents a DocumentLeaf.
 * 
 * A document has a ID, name, make date, change date, file name, file size,
 * document type and a protection status.
 * 
 * @author Dominik Danner
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentLeaf extends Leaf {

	private static final long serialVersionUID = 4524808032290187009L;

	public String document_id;
	public String name;
	public Long mkdate;
	public Long chdate;
	public String filename;
	public Long filesize;
	public String mime_type;
	public String protection;

	/**
	 * This flag is set to true, if the download is allowed, only when
	 * protection is enabled.
	 */
	@XmlAttribute
	private boolean downloadAllowed = false;

	/**
	 * Constructor for this Object.
	 */
	public DocumentLeaf() {
	}

	/**
	 * Set permission to download.
	 */
	public void setDownloadAllowed() {
		this.downloadAllowed = true;
	}

	/**
	 * Getter for permission to download.
	 * 
	 * @return permission to download.
	 */
	public boolean isDownloadAllowed() {
		return this.downloadAllowed;
	}

	/**
	 * Returns the Protection status.
	 * 
	 * @return true if this document is protected, otherwise false
	 */
	public boolean isProtected() {
		return (this.protection.equals("1"));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((document_id == null) ? 0 : document_id.hashCode());
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
		DocumentLeaf other = (DocumentLeaf) obj;
		if (document_id == null) {
			if (other.document_id != null)
				return false;
		} else if (!document_id.equals(other.document_id))
			return false;
		return true;
	}

	@Override
	public String getName() {
		return super.getName() == null ? replaceChars(filename) : super
				.getName();
	}

	/**
	 * Updates the information of this documentLeaf if the given one has the
	 * same id.
	 * 
	 * @param documentLeaf
	 *            new documentLeaf.
	 */
	public void update(DocumentLeaf documentLeaf) {
		// only update, if the given document_id is the same
		if (this.equals(documentLeaf)) {
			this.name = documentLeaf.name;
			this.mkdate = documentLeaf.mkdate;
			this.chdate = documentLeaf.chdate;
			this.filename = documentLeaf.filename;
			this.filesize = documentLeaf.filesize;
			this.mime_type = documentLeaf.mime_type;
		}
	}

	@Override
	public long getLastChdate() {
		return this.chdate;
	}

}
