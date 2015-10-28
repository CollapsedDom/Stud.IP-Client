package de.danner_web.studip_client.view;

import java.util.EventListener;

public interface NavigationListener extends EventListener {

	public void actionPerformed(NavigationAction a);
}
