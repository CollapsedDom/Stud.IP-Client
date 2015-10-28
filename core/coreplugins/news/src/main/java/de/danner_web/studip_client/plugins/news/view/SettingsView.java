package de.danner_web.studip_client.plugins.news.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import de.danner_web.studip_client.data.PluginSettings;

/**
 * THis class implements the settings view for the news plugin
 * 
 * @author philipp
 *
 */
public class SettingsView extends JPanel {

	private static final long serialVersionUID = -7199119520911188760L;

	private PluginSettings settings;
	public static final String NEWS_STUDIP = "news_studip";
	public static final String NEWS_INSTITUTE = "news_institute";
	public static final String NEWS_COURSES = "news_courses";

	private JCheckBox studipCb, instituteCb, coursesCb;

	/**
	 * Constructor of the SettingsView
	 * 
	 * @param settings
	 */
	public SettingsView(PluginSettings settings) {
		this.settings = settings;
		createAndShowGui();
	}

	/**
	 * helper method to build gui
	 */
	private void createAndShowGui() {

		// Main panel
		this.setBackground(Color.WHITE);
		Dimension size = new Dimension(200, 170);
		this.setMinimumSize(size);
		this.setMaximumSize(size);
		this.setPreferredSize(size);

		JLabel h = new JLabel("Ankündigungen");
		this.add(h);

		// studip
		studipCb = new JCheckBox("Global");
		studipCb.setOpaque(false);
		studipCb.setSelected(new Boolean(settings.get(NEWS_STUDIP)));
		studipCb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

					@Override
					protected String doInBackground() throws Exception {
						settings.put(NEWS_STUDIP, String.valueOf(studipCb.isSelected()));
						return "done";
					}
				};
				worker.execute();
			}
		});

		// institute
		instituteCb = new JCheckBox("Institut");
		instituteCb.setOpaque(false);
		instituteCb.setSelected(new Boolean(settings.get(NEWS_INSTITUTE)));
		instituteCb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

					@Override
					protected String doInBackground() throws Exception {
						settings.put(NEWS_INSTITUTE, String.valueOf(instituteCb.isSelected()));
						return "done";
					}
				};
				worker.execute();
			}
		});

		// courses
		coursesCb = new JCheckBox("Veranstaltungen");
		coursesCb.setOpaque(false);
		coursesCb.setSelected(new Boolean(settings.get(NEWS_COURSES)));
		coursesCb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

					@Override
					protected String doInBackground() throws Exception {
						settings.put(NEWS_COURSES, String.valueOf(coursesCb.isSelected()));
						return "done";
					}
				};
				worker.execute();
			}
		});

		// Put the check boxes in a column in a panel
		JPanel checkPanel = new JPanel(new GridLayout(0, 1));
		checkPanel.setOpaque(false);
		checkPanel.add(studipCb);
		checkPanel.add(instituteCb);
		checkPanel.add(coursesCb);

		this.add(checkPanel);
		setVisible(true);
	}
}
