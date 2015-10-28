package de.danner_web.studip_client.view;

import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.data.ClickListener;
import de.danner_web.studip_client.data.PluginMessage;
import de.danner_web.studip_client.model.SettingsModel;
import de.danner_web.studip_client.model.SettingsModel.NotificationOrientation;
import de.danner_web.studip_client.model.Model;
import de.danner_web.studip_client.view.components.InfoPanel;

public class InfoController implements Observer, ClickListener {

	/**
	 * Logger dieser Klasse.
	 */
	private static Logger logger = LogManager.getLogger(InfoController.class);

	private Model model;
	private SettingsModel settings;
	private AutoDeletThread thread;

	private int maxNotificationCount;
	private int deleteTime;
	private boolean fromTop;
	private int desktopHeight, desktopWidth, padding, topPadding, bottonPadding;

	private List<PluginMessage> newPluginMessageList = new LinkedList<PluginMessage>();
	private List<InfoPanel> infoPanelList = new LinkedList<InfoPanel>();

	public InfoController(Model model) {
		this.model = model;

		settings = model.getSettingsModel();
		desktopHeight = (int) GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight();
		desktopWidth = (int) GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getWidth();
		padding = 10;

		model.addObserver(this);
		settings.addObserver(this);

		loadSettings();
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.entry(o, arg);

		if (o == model && arg instanceof PluginMessage) {
			PluginMessage message = (PluginMessage) arg;
			newPluginMessageList.add(message);
			updateNotification();
		} else if (o == settings) {
			loadSettings();
		}
		logger.exit();
	}

	private void loadSettings() {
		topPadding = settings.getNotificationPaddingTop();
		bottonPadding = settings.getNotificationPaddingBottom();
		fromTop = settings.getNotificationOrientation().equals(NotificationOrientation.TOP);
		maxNotificationCount = settings.getNotificationMaxCount();
		deleteTime = settings.getNotificationDeleteTime();
	}

	private boolean updateNotification() {

		if (!newPluginMessageList.isEmpty() && infoPanelList.size() < maxNotificationCount) {

			// newPluginMessageList is not empty and infoPanelList is not full
			PluginMessage message = newPluginMessageList.get(0);
			Point insertPosition;

			// Calculate position to insert
			if (fromTop) {
				if (infoPanelList.isEmpty()) {
					insertPosition = new Point(desktopWidth - InfoPanel.WIDTH - padding, topPadding);
				} else {
					InfoPanel lastElement = infoPanelList.get(infoPanelList.size() - 1);
					insertPosition = new Point(lastElement.getLocation().x,
							lastElement.getLocation().y + padding + lastElement.getPreferredSize().height);
				}
			} else {
				if (infoPanelList.isEmpty()) {
					insertPosition = new Point(desktopWidth - InfoPanel.WIDTH - padding, desktopHeight - bottonPadding);
				} else {
					InfoPanel lastElement = infoPanelList.get(infoPanelList.size() - 1);
					insertPosition = new Point(lastElement.getLocation().x, lastElement.getLocation().y - padding);
				}
			}

			// Convert PluginMessage to InfoPanel
			message.addMessageListener(this);
			infoPanelList.add(new InfoPanel(message, insertPosition, fromTop));
			newPluginMessageList.remove(0);
		}
		updatePositions();
		updateTimer();

		return true;
	}

	private void updatePositions() {

		// update the position of all panels
		for (int i = 0; i < infoPanelList.size(); i++) {
			Point p;
			InfoPanel infoPanel = infoPanelList.get(i);
			if (fromTop) {
				if (i == 0) {
					p = new Point(desktopWidth - InfoPanel.WIDTH - padding, topPadding);
				} else {
					InfoPanel lastElement = infoPanelList.get(i - 1);
					p = new Point(lastElement.target.x,
							lastElement.target.y + padding + lastElement.getPreferredSize().height);
				}
			} else {
				if (i == 0) {
					p = new Point(desktopWidth - InfoPanel.WIDTH - padding, desktopHeight - bottonPadding);
				} else {
					InfoPanel lastElement = infoPanelList.get(i - 1);
					p = new Point(lastElement.target.x, lastElement.target.y - padding);
				}
			}
			infoPanel.moveToLocation(p);
		}
	}

	/**
	 * Updates the Timer Thread that closes old Notifications
	 * 
	 * This thread deletes the running Thread if this Thread counts down for a
	 * now non existing infoPanel. After that a new Thread will be created for
	 * the new oldest InfoPanel. If no infoPanel is left, no new Thread will be
	 * created.
	 */
	private void updateTimer() {
		if (infoPanelList.isEmpty()) { // No element left -> kill thread if
										// existing
			if (thread != null) {
				thread.interrupt();

				// Be sure that Thread is dead
				try {
					thread.join();
				} catch (InterruptedException e) {
					thread.interrupt();
				}
			}
		} else {

			// infoPanelList is not empty
			InfoPanel oldestPanel = infoPanelList.get(0);
			if (thread != null) {
				if (!(thread.oldestPanel.equals(oldestPanel))) {

					// Kill thread if oldestPanel differs and start a new one
					thread.interrupt();

					// Be sure that Thread is dead
					try {
						thread.join();
					} catch (InterruptedException e) {
						thread.interrupt();
					}

					thread = new AutoDeletThread(deleteTime, oldestPanel);
					thread.start();
				}
			} else {
				thread = new AutoDeletThread(deleteTime, oldestPanel);
				thread.start();
			}
		}
	}

	private boolean closeNotification(InfoPanel panel) {
		if (infoPanelList.isEmpty()) {
			return false;
		} else {

			// remove panel from list and dispose
			infoPanelList.remove(panel);
			panel.dispose();

			// update the possition of all other panels

			// show a new notification if possible
			updateNotification();
			return true;
		}
	}

	public void detacheView() {
		model.deleteObserver(this);
		settings.deleteObserver(this);
	}

	@Override
	public void onClick(PluginMessage message) {
		InfoPanel removePanel = null;
		for (InfoPanel infoPanel : infoPanelList) {
			if (infoPanel.hasMessage(message)) {
				removePanel = infoPanel;
			}
		}
		closeNotification(removePanel);
	}

	class AutoDeletThread extends Thread {

		private InfoPanel oldestPanel;
		private int deleteTime;
		private boolean running = true;

		public AutoDeletThread(int deleteTime, InfoPanel oldestPanel) {
			super();
			this.deleteTime = deleteTime * 1000;
			this.oldestPanel = oldestPanel;
		}

		@Override
		public void run() {
			while (running) {
				try {
					Thread.sleep(deleteTime);
					closeNotification(oldestPanel);
					running = false;
				} catch (InterruptedException e) {
					running = false;
				}
			}
		}
	}

	// public static void main(String[] args) throws InterruptedException {
	// ClickListener listener = new ClickListener() {
	//
	// @Override
	// public void onClick(PluginMessage message) {
	// System.out.println("Clicked on Message:" + message.getHeader());
	// }
	// };
	//
	// Model model = new Model();
	// InfoController infoController = new InfoController(model);
	// infoController.update(model,
	// new TextPluginMessage("Big News!", "Deine Oma fährt im Hünerstall
	// Motorrad", null));
	//
	// // Thread.sleep(1000);
	//
	// infoController.update(model,
	// new TextPluginMessage("Big News2!", "Deine Oma fährt im Hünerstall
	// Motorrad", listener));
	//
	// // Thread.sleep(1000);
	//
	// infoController.update(model,
	// new TextPluginMessage("Big News3!",
	// "Deine Oma fährt im Hünerstall Motorrad. Deine Oma fährt im Hünerstall
	// Motorrad. Deine Oma fährt im Hünerstall Motorrad. Deine Oma fährt im
	// Hünerstall Motorrad. Deine Oma fährt im Hünerstall Motorrad. Deine Oma
	// fährt im Hünerstall Motorrad. Deine Oma fährt im Hünerstall Motorrad.
	// Deine Oma fährt im Hünerstall Motorrad",
	// listener));
	// // Thread.sleep(2000);
	//
	// ClickListener test = new ClickListener() {
	// @Override
	// public void onClick(PluginMessage message) {
	// System.out.println("OK Clicked");
	// }
	// };
	//
	// infoController.update(model,
	// new AcceptPluginMessage("Big News4!",
	// "Deine Oma fährt im Hünerstall Motorrad. Deine Oma fährt im Hünerstall
	// Motorrad. Deine Oma fährt im Hünerstall Motorrad. Deine Oma fährt im
	// Hünerstall Motorrad. Deine Oma fährt im Hünerstall Motorrad. Deine Oma
	// fährt im Hünerstall Motorrad. Deine Oma fährt im Hünerstall Motorrad.
	// Deine Oma fährt im Hünerstall Motorrad",
	// listener, test));
	//
	// infoController.update(model, new AcceptPluginMessage("Big News4!", "a",
	// listener, test));
	// // Thread.sleep(2000);
	//
	// infoController.update(model,
	// new TextPluginMessage("Big News5!", "Deine Oma fährt im Hünerstall
	// Motorrad", listener));
	//
	// }

}
