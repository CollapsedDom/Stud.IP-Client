package de.danner_web.studip_client.data;

import java.util.ArrayList;
import java.util.List;

import de.danner_web.studip_client.view.components.PluginMessageDetailsView;

public abstract class PluginMessage {

	private List<ClickListener> listeners = new ArrayList<ClickListener>();
	
	private ClickListener showDetailView = new ClickListener() {

		@Override
		public void onClick(PluginMessage message) {
			new PluginMessageDetailsView(message);
		}
	};
	
	public PluginMessage(ClickListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	public void setShowDetailView(boolean b) {
		if (b) {
			if(!this.listeners.contains(showDetailView)){
				this.listeners.add(showDetailView);
			}
		} else {
			if(this.listeners.contains(showDetailView)){
				this.listeners.remove(showDetailView);
			}
		}
	}

	public abstract String getText();

	public abstract String getHeader();

	public void addMessageListener(ClickListener listener) {
		if (listener != null) {
			this.listeners.add(listener);
		}
	}

	public List<ClickListener> getMessageListener() {
		return listeners;
	}

	public abstract int hashCode();

	public abstract boolean equals(Object obj);

}
