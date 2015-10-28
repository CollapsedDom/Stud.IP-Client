package de.danner_web.studip_client.plugins.file_downloader.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import de.danner_web.studip_client.plugins.file_downloader.DefaultFileHandler;
import de.danner_web.studip_client.plugins.file_downloader.UpdateFailureException;
import de.danner_web.studip_client.utils.Template;
import de.danner_web.studip_client.view.components.ModernScrollPane;
import de.danner_web.studip_client.view.components.buttons.ModernButton;

/**
 * This Class is a JPanel that is shown by the Framework.
 * 
 * You can define all settings for the plugin here.
 * 
 * @author Dominik Danner
 *
 */
public class SettingsView extends JPanel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7358648860849224066L;

	private DefaultFileHandler model;
	private JTree tree;
	private JTextField dir;
	private JButton saveDir;

	/**
	 * Constructor of the SettingsView
	 */
	public SettingsView(DefaultFileHandler model) {
		this.model = model;
		model.addObserver(this);
		createAndShowGui();
		getTree();
	}

	private void getTree() {
		SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

			@Override
			protected String doInBackground() throws Exception {
				try {
					model.updateDatabaseTree();
				} catch (UpdateFailureException e) {
					// Igonore Update Failure and use old MetaTree
				}
				return "done";
			}
		};
		setBussyMouse(true);
		worker.execute();
	}

	private void createAndShowGui() {
		// Main panel
		this.setBackground(Color.WHITE);
		this.setLayout(new BorderLayout());
		Dimension size = new Dimension(500, 500);
		this.setMinimumSize(size);
		this.setMaximumSize(size);
		this.setPreferredSize(size);

		// Directory selections panel
		JPanel panelDir = new JPanel();
		panelDir.setBackground(Color.WHITE);
		panelDir.setLayout(new BorderLayout());
		panelDir.setBorder(new EmptyBorder(10, 10, 10, 10));

		panelDir.add(new JLabel("Sync Ordner"), BorderLayout.LINE_START);
		saveDir = new ModernButton("Setzen");
		saveDir.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.changeDestination(dir.getText());
			}
		});
		dir = new JTextField(model.getDir());
		dir.setBorder(new LineBorder(Template.COLOR_LIGHT_GRAY));
		dir.setPreferredSize(new Dimension(250, saveDir.getPreferredSize().height));

		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.add(dir);
		panel.add(saveDir);
		panelDir.add(panel, BorderLayout.LINE_END);

		this.add(panelDir, BorderLayout.NORTH);

		// Tree panel
		JPanel panelTree = new JPanel();
		panelTree.setBackground(Color.WHITE);
		panelTree.setLayout(new BorderLayout());
		panelTree.setBorder(new EmptyBorder(10, 10, 10, 10));

		tree = new JTree();
		tree.setModel(new DefaultTreeModel(model.getTree().toTreeNode()));
		tree.setCellRenderer(new TreeCellListRenderer());
		tree.setCellEditor(new TreeCellListEditor());
		tree.setUI(new CustomTreeUI(tree)); // full width
		tree.setEnabled(false);
		tree.setEditable(true);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		panelTree.add(tree, BorderLayout.CENTER);

		JScrollPane scrollPane = new ModernScrollPane(panelTree);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		this.add(scrollPane, BorderLayout.CENTER);

		setVisible(true);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof DefaultFileHandler) {
			tree.setModel(new DefaultTreeModel(model.getTree().toTreeNode()));
			tree.setEnabled(true);
			dir.setText(model.getDir());
			setBussyMouse(false);
			repaint();
		}
	}

	private void setBussyMouse(boolean wait) {
		if (wait) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		} else {
			this.setCursor(Cursor.getDefaultCursor());
		}
	}
}
