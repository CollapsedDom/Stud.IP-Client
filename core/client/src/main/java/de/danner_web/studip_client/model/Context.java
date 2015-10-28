package de.danner_web.studip_client.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.data.PluginMessage;

/**
 * Diese Singleton Klasse bietet allgemeine Funtkionen, die ein Plugin aufrufen
 * kann.
 * 
 * 
 * @author dominik
 * 
 */
public class Context {

	private Model model;
	
	private String name;

	/**
	 * Logger dieser Klasse.
	 */
	private static Logger logger = LogManager.getLogger();

	/**
	 * Konstuktor des Context
	 * 
	 * @param model
	 *            für den Zugriff
	 */
	Context(Model model) {
		this.model = model;
	}
	
	private void setName(String name){
		this.name = name;
	}
	
	Context createContextForPlugin(String name) {
		Context newContext = new Context(model);
		newContext.setName(name);
		return newContext;
	}

	/**
	 * Fügt einen Historyeintrag in der Verlaufsanzeigen hinzu.
	 * 
	 * Dieser Eintrag wird gleichzeitig mit Log4j persistent weggespeichert.
	 * 
	 * @param name
	 *            Name des Plugins
	 * @param text
	 *            Nachricht
	 */
	public void appendHistory(String text) {
		model.appendHistory(name, text);
		logger.info("History: " + name + ": " + text);
	}

	/**
	 * Fügt eine Popup Nachricht hinzu.
	 * 
	 * Diese Nachricht wird gleichzeitig im Verlauf aufgenommen und mit Log4j
	 * persistent weggespeichert.
	 * 
	 * 
	 * @param name
	 *            Name des Plugins
	 * @param header
	 *            Überschrift der Nachricht
	 * @param text
	 *            Nachricht
	 */
	public void appendPopup(PluginMessage message) {
		if (message == null) {
			throw logger.throwing(new IllegalArgumentException(
					"Message should not be null"));
		}
		model.appendPopup(message);
		model.appendHistory(name, message.getHeader() + " - " + message.getText());
		logger.info("Popup: " + message.getHeader() + " - " + message.getText());
	}

}
