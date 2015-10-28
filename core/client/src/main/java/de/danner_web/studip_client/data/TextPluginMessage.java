package de.danner_web.studip_client.data;

public class TextPluginMessage extends PluginMessage {

	private String header;
	private String text;

	/**
	 * This Simple Message Type gets a Header String and a text to display.
	 * 
	 * If the user clicks on the Popup message the methode onClick() from the
	 * listener is invoked.
	 * 
	 * @param header
	 *            String to display
	 * @param text
	 *            String to display
	 * @param listener
	 *            gets invoked when message is clicked
	 */
	public TextPluginMessage(String header, String text, ClickListener listener) {
		super(listener);
		this.header = header;
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public String getHeader() {
		return header;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		TextPluginMessage other = (TextPluginMessage) obj;
		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
}
