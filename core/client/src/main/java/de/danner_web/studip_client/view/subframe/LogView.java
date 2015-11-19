package de.danner_web.studip_client.view.subframe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.data.LogItem;
import de.danner_web.studip_client.model.Model;
import de.danner_web.studip_client.utils.Template;
import de.danner_web.studip_client.view.DetachableView;
import de.danner_web.studip_client.view.components.ModernScrollPane;
import de.danner_web.studip_client.view.components.listrenderer.LogListRenderer;

public class LogView extends JPanel implements Observer, DetachableView {

	private static final long serialVersionUID = -1430326569970992865L;
	private static Logger logger = LogManager.getLogger(LogView.class);

	private JScrollPane scrollPane;

	private Model model;

	private DefaultListModel<LogItem> listModel;
	private JList<LogItem> logList;

	public LogView(Model model) {
		logger.entry();
		this.model = model;

		model.addObserver(this);

		createView();

		logger.exit();
	}

	private void createView() {
		logger.entry();
		setBackground(Color.WHITE);
		setLayout(new BorderLayout());

		// Create JList
		listModel = new DefaultListModel<LogItem>();
		for (LogItem item : model.getLogList()) {
			listModel.addElement(item);
		}
		logList = new JList<LogItem>(listModel);

		// FIXME: get width from outer container
		logList.setCellRenderer(new LogListRenderer(396));
		logList.setForeground(Template.COLOR_ACCENT);

		// add JList in scroll able panel
		scrollPane = new ModernScrollPane(logList);

		// add scroll able panel
		this.add(scrollPane, BorderLayout.CENTER);
		logger.exit();
	}

	public void detacheView() {
		model.deleteObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.entry(o, arg);
		if (o == model) {

			// update LogList
			if (arg instanceof LogItem) {
				listModel = new DefaultListModel<LogItem>();
				for (LogItem item : model.getLogList()) {
					listModel.addElement(item);
				}
				logList.setModel(listModel);
				logList.invalidate();
				logList.ensureIndexIsVisible(model.getLogList().size() - 1);
			}
		}
		repaint();

		logger.exit();
	}

}
