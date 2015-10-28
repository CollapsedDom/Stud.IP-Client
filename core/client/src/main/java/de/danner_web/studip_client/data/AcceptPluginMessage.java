package de.danner_web.studip_client.data;

public class AcceptPluginMessage extends TextPluginMessage {

	private ClickListener acceptListener;

	/**
	 * 
	 * @param header
	 * @param text
	 * @param listener
	 *            for the click on the body of the message
	 * @param listener
	 *            for the click on the accept button
	 */
	public AcceptPluginMessage(String header, String text, ClickListener listener, ClickListener acceptListener) {
		super(header, text, listener);
		this.acceptListener = acceptListener;
	}

	public ClickListener getAcceptListener() {
		return acceptListener;
	}
}
