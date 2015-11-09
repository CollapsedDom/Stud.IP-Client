package de.danner_web.studip_client.view.subframe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.model.Model;
import de.danner_web.studip_client.model.PluginModel;
import de.danner_web.studip_client.plugin.PluginInformation;
import de.danner_web.studip_client.utils.Template;
import de.danner_web.studip_client.view.SettingsDialog;
import de.danner_web.studip_client.view.components.ModernScrollPane;
import de.danner_web.studip_client.view.components.buttons.ActionButton;
import de.danner_web.studip_client.view.components.buttons.ModernToggleButton;
import de.danner_web.studip_client.view.components.buttons.ActionButton.ActionType;
import de.danner_web.studip_client.view.components.listrenderer.PluginListRenderer;

public class PluginView extends JPanel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3240260501346883675L;

	/**
	 * Logger dieser Klasse.
	 */
	private static Logger logger = LogManager.getLogger(PluginView.class);

	private static final String DELETE_TITLE = "de.danner_web.studip_client.view.subframe.PluginView.deleteTitle";
	private static final String DELETE_QUESTION = "de.danner_web.studip_client.view.subframe.PluginView.deleteQuestion";

	private static final String DEACTIVATION_TITLE = "de.danner_web.studip_client.view.subframe.PluginView.deactivateTitle";
	private static final String DEACTIVATION_QUESTION = "de.danner_web.studip_client.view.subframe.PluginView.deactivateQuestion";

	private ModernToggleButton pauseButton;
	private ActionButton addButton, activateButton, settingsButton, deletButton;

	private JList<PluginInformation> pluginList;
	private DefaultListModel<PluginInformation> listModel;

	private Model model;
	private PluginModel pluginModel;
	private ResourceBundle resourceBundle;

	private boolean buildSettingsGui;
	private boolean requestRunning;

	public PluginView(Model model) {
		this.model = model;
		this.pluginModel = model.getPluginModel();
		this.resourceBundle = model.getResourceBundle(model.getCurrentLocale());
		pluginModel.addObserver(this);
		createView();
	}

	private void createView() {
		this.setBackground(Color.WHITE);
		this.setLayout(new BorderLayout());

		add(createOptionPanel(), BorderLayout.PAGE_START);
		add(createPluginList(), BorderLayout.CENTER);
	}

	private JPanel createOptionPanel() {
		JPanel mainOptionPanel = new JPanel() {
			private static final long serialVersionUID = -5117047180924520550L;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				g.setColor(Template.COLOR_GRAY);
				g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
			}
		};
		mainOptionPanel.setLayout(new BorderLayout());
		mainOptionPanel.setBackground(Color.WHITE);
		mainOptionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		pauseButton = new ModernToggleButton(pluginModel.isRunning());
		pauseButton.setLocale(model.getCurrentLocale());
		pauseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (pluginModel.isRunning()) {
					requestPause();
				} else {
					requestResume();
				}
			}
		});

		addButton = new ActionButton(ActionType.ADD);
		addButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				requestAddNewPlugin();
			}
		});

		activateButton = new ActionButton(ActionType.ACTIVATE);
		activateButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (pluginModel.isactivePlugin(pluginList.getSelectedValue())) {
					requestPluginDeactivation();
				} else {
					requestPluginActivation();
				}

			}
		});

		settingsButton = new ActionButton(ActionType.SETTINGS);
		settingsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PluginInformation info = pluginList.getSelectedValue();
				if (info.hasSettingsGui() && pluginModel.getPlugins().contains(info)
						&& pluginModel.isactivePlugin(info)) {
					requestPause();
					buildSettingsGui = true;
				}
			}
		});

		deletButton = new ActionButton(ActionType.DELETE);
		deletButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (deletButton.isEnabled()) {
					requestDeletePlugin();
				}
			}
		});

		JPanel leftPanel = new JPanel();
		leftPanel.setBackground(Color.WHITE);
		leftPanel.add(pauseButton);
		leftPanel.add(addButton);
		mainOptionPanel.add(leftPanel, BorderLayout.LINE_START);

		JPanel rightPanel = new JPanel();
		rightPanel.setBackground(Color.WHITE);
		rightPanel.add(activateButton);
		rightPanel.add(settingsButton);
		rightPanel.add(deletButton);
		mainOptionPanel.add(rightPanel, BorderLayout.LINE_END);

		// By Default no entry in the List is selected -> no settings, activate
		// or delete
		activateButton.setEnabled(false);
		settingsButton.setEnabled(false);
		deletButton.setEnabled(false);

		return mainOptionPanel;
	}

	private JScrollPane createPluginList() {
		listModel = new DefaultListModel<PluginInformation>();
		for (PluginInformation info : pluginModel.getPlugins()) {
			listModel.addElement(info);
		}
		pluginList = new JList<PluginInformation>(listModel);
		pluginList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				activateButton.setEnabled(true);
				if (pluginModel.isactivePlugin(pluginList.getSelectedValue())) {
					activateButton.setActive(true);
					settingsButton.setEnabled(pluginList.getSelectedValue().hasSettingsGui());
				} else {
					settingsButton.setEnabled(false);
					activateButton.setActive(false);
				}

				deletButton.setEnabled(true);
			}
		});
		pluginList.setCellRenderer(new PluginListRenderer(model));
		pluginList.setForeground(Template.COLOR_ACCENT);

		return new ModernScrollPane(pluginList);
	}

	private void showPluginSettings() {
		
		JPanel settingsView = pluginModel.getSettingsView(pluginList.getSelectedValue());
		if(settingsView != null){
			final SettingsDialog dialog = new SettingsDialog(settingsView);
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					requestResume();
					dialog.dispose();
				}
			});
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
		}
	}

	private void requestDeletePlugin() {
		SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

			@Override
			protected String doInBackground() throws Exception {
				if (!requestRunning) {
					requestRunning = true;
					boolean success = pluginModel.removePlugin(pluginList.getSelectedValue());
					if (!success)
						logger.debug(
								"Removing plugin: " + pluginList.getSelectedValue().getName() + " was not successful.");

				}
				return "done";
			}
		};
		int input = JOptionPane.showConfirmDialog(this,
				getLocalized(DELETE_QUESTION) + " " + pluginList.getSelectedValue().getName(),
				getLocalized(DELETE_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		// If set to delete
		if (input == 0) {
			setBussyMouse(true);
			worker.execute();
		}
	}

	private void requestPluginActivation() {
		logger.entry();
		SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

			@Override
			protected String doInBackground() throws Exception {
				if (!requestRunning) {
					requestRunning = true;
					boolean success = pluginModel.authorizePlugin(pluginList.getSelectedValue());
					if (!success)
						logger.debug("Authorizing plugin: " + pluginList.getSelectedValue().getName()
								+ " was not successful.");

				}
				return "done";
			}
		};
		setBussyMouse(true);
		worker.execute();
		logger.exit();
	}

	private void requestPluginDeactivation() {
		logger.entry();
		SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

			@Override
			protected String doInBackground() throws Exception {
				if (!requestRunning) {
					requestRunning = true;
					boolean success = pluginModel.deactivatePlugin(pluginList.getSelectedValue());
					if (!success)
						logger.debug("Deactivating plugin: " + pluginList.getSelectedValue().getName()
								+ " was not successful.");
				}
				return "done";
			}
		};

		int input = JOptionPane.showConfirmDialog(this,
				getLocalized(DEACTIVATION_QUESTION) + " " + pluginList.getSelectedValue().getName(),
				getLocalized(DEACTIVATION_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		// If set to delete
		if (input == 0) {
			setBussyMouse(true);
			worker.execute();
		}

		logger.exit();
	}

	private void requestAddNewPlugin() {
		FileDialog fileChooser = new FileDialog((java.awt.Frame) null);
		fileChooser.setFilenameFilter(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar") ? true : false;
			}
		});
		fileChooser.setDirectory(System.getProperty("user.home"));
		fileChooser.setMultipleMode(false);
		fileChooser.setVisible(true);
		String directory = fileChooser.getDirectory();
		String file = fileChooser.getFile();
		final File selected = new File(directory + "/" + file);
		SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

			@Override
			protected String doInBackground() throws Exception {
				if (!requestRunning) {
					requestRunning = true;
					pluginModel.installPlugin(selected);
				}
				return "done";
			}
		};
		setBussyMouse(true);
		worker.execute();
	}

	private void requestResume() {
		logger.entry();
		SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

			@Override
			protected String doInBackground() throws Exception {
				if (!requestRunning) {
					requestRunning = true;
					pluginModel.resume();
				}
				return "done";
			}
		};
		setBussyMouse(true);
		worker.execute();
		logger.exit();
	}

	private void requestPause() {
		logger.entry();
		SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

			@Override
			protected String doInBackground() throws Exception {
				if (!requestRunning) {
					requestRunning = true;
					pluginModel.pause();
				}
				return "done";
			}
		};
		setBussyMouse(true);
		worker.execute();
		logger.exit();
	}

	private void setBussyMouse(boolean wait) {
		if (wait) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		} else {
			this.setCursor(Cursor.getDefaultCursor());
		}
	}

	private String getLocalized(String key) {
		String val = resourceBundle.getString(key);
		try {
			return new String(val.getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return val;
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.entry();
		if (o == pluginModel) {
			requestRunning = false;
			pauseButton.setSelected(pluginModel.isRunning());
			activateButton.setActive(model.getPluginModel().isactivePlugin(pluginList.getSelectedValue()));
			settingsButton.setEnabled(model.getPluginModel().isactivePlugin(pluginList.getSelectedValue()));
			if (buildSettingsGui) {
				buildSettingsGui = false;
				showPluginSettings();
			}

			// update PluginList
			if (arg instanceof PluginInformation) {
				listModel = new DefaultListModel<PluginInformation>();
				for (PluginInformation info : pluginModel.getPlugins()) {
					listModel.addElement(info);
				}
				pluginList.setModel(listModel);
				pluginList.invalidate();
			}
		}
		repaint();

		logger.exit();

	}
}
