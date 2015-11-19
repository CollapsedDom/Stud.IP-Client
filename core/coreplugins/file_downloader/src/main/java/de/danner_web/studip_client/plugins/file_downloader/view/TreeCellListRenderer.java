package de.danner_web.studip_client.plugins.file_downloader.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import de.danner_web.studip_client.plugins.file_downloader.treeModel.DocumentLeaf;
import de.danner_web.studip_client.plugins.file_downloader.treeModel.InnerNode;
import de.danner_web.studip_client.plugins.file_downloader.treeModel.Node;
import de.danner_web.studip_client.utils.Template;

public class TreeCellListRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 4895840669941614205L;

	private CellPanel panel = new CellPanel();

	public TreeCellListRenderer() {
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		DefaultMutableTreeNode o = (DefaultMutableTreeNode) value;
		Node node = (Node) o.getUserObject();
		panel.updateData(node, selected);
		return panel;
	}
}

class TreeCellListEditor extends AbstractCellEditor implements TreeCellEditor {
	private static final long serialVersionUID = -4824862020572081975L;

	private CellPanel panel = new CellPanel();
	private JCheckBox cbDownload = new JCheckBox();
	private JTextField name = new JTextField();
	private JLabel icon = new JLabel();

	private boolean iconClicked = false;
	private boolean nameChanged = false;

	private Node node;

	/*
	 * This attribute represents the MouseListener for the icon to toggle
	 * hide/show folder.
	 */
	private MouseListener iconMouseListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			iconClicked = true;
			fireEditingStopped();
		}
	};

	/*
	 * This attribute represents the itemListener for the check box to toggle
	 * download active.
	 */
	private ItemListener listener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			fireEditingStopped();
		}
	};

	/*
	 * This attribute represents the MouseListener for the name field to enable
	 * name editing.
	 */
	private MouseListener nameClickListener = new MouseAdapter() {

		/*
		 * This attribute represents the focuslistener
		 */
		private FocusListener fl = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!node.getName().equals(panel.getNameComponent().getText())) {
					nameChanged = true;
				}
				updateName();
			}
		};

		/*
		 * This attribute represents the actionlistener
		 */
		private ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!node.getName().equals(panel.getNameComponent().getText())) {
					nameChanged = true;
				}
				updateName();
				fireEditingStopped();
			}
		};

		/*
		 * This attribute represents the keylistener
		 */
		private KeyListener kl = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					name.setText(node.getName());
				}
			}
		};

		@Override
		public void mousePressed(MouseEvent e) {

			if (e.getClickCount() == 2) {

				// Set design
				name.setBorder(new LineBorder(Template.COLOR_ACCENT));
				name.setEditable(true);
				name.setFocusable(true);
				name.setForeground(Template.COLOR_ACCENT);
				name.requestFocus();

				// Add focus listener
				for (FocusListener l : name.getFocusListeners()) {
					if (fl.equals(l)) {
						name.removeFocusListener(l);
					}
				}
				name.addFocusListener(fl);

				// Add action listener
				for (ActionListener l : name.getActionListeners()) {
					if (al.equals(l)) {
						name.removeActionListener(l);
					}
				}
				name.addActionListener(al);

				// Add KeyListener
				for (KeyListener l : name.getKeyListeners()) {
					if (kl.equals(l)) {
						name.removeKeyListener(l);
					}
				}
				name.addKeyListener(kl);
			}
		}
	};

	public TreeCellListEditor() {
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row) {
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
		node = (Node) treeNode.getUserObject();
		panel.updateData(node, true);

		// Prepare listeners
		prepareNameEditListener();
		prepareHideIconListener();
		prepareDownloadActiveListener();

		return panel;
	}

	private void prepareNameEditListener() {
		name = panel.getNameComponent();

		// Cleanup old Listener
		for (MouseListener l : name.getMouseListeners()) {
			if (nameClickListener.equals(l)) {
				name.removeMouseListener(l);
			}
		}

		if (node.isDownloadActive()) {
			name.addMouseListener(nameClickListener);
		}
	}

	private void prepareHideIconListener() {
		icon = panel.getIconComponent();

		// Cleanup old Listener
		for (MouseListener l : icon.getMouseListeners()) {
			if (iconMouseListener.equals(l)) {
				icon.removeMouseListener(l);
			}
		}
		if (node.isDownloadActive()) {
			icon.addMouseListener(iconMouseListener);
		}
	}

	private void prepareDownloadActiveListener() {
		cbDownload = panel.getCbDownload();

		// Cleanup old Listener
		for (ItemListener l : cbDownload.getItemListeners()) {
			if (listener.equals(l)) {
				cbDownload.removeItemListener(l);
			}
		}
		cbDownload.addItemListener(listener);
	}

	private void updateName() {
		// update name
		if (nameChanged) {
			if (!node.changeName(panel.getNameComponent().getText())) {
				JOptionPane
						.showMessageDialog(null,
								"Das umbenennen von \"" + node.getName() + "\" zu \""
										+ panel.getNameComponent().getText() + "\" ist nicht erlaubt.",
								"Umbenennen nicht erlaubt", JOptionPane.OK_OPTION);
				name.setText(node.getName());
			}
			nameChanged = false;
		}
	}

	@Override
	public Object getCellEditorValue() {

		// update hide/show folder
		if (iconClicked) {
			if (node instanceof InnerNode) {
				// if icon is clicked -> change the state of isNodeHide()
				InnerNode innerNode = (InnerNode) node;
				innerNode.setNodeHide(!innerNode.isNodeHide());
			}
			iconClicked = false;
		}

		// update active download
		if (node.isDownloadActive() != cbDownload.isSelected()) {
			node.setDownloadActive(cbDownload.isSelected());
		}
		return node;
	}

	@Override
	public boolean isCellEditable(EventObject e) {
		if (e instanceof MouseEvent && e.getSource() instanceof JTree) {
			MouseEvent me = (MouseEvent) e;
			if (me.isPopupTrigger()) {
				return false;
			}
			return true;

			// JTree tree = (JTree) me.getSource();
			// int row = tree.getClosestRowForLocation(me.getX(), me.getY());
			// Rectangle rect = tree.getRowBounds(row);
			//
			// // dirty hack of me.getPont().getX() to decide if only
			// select
			// // or edit
			// if (me.getClickCount() > 1 || me.getPoint().getX() >
			// rect.getWidth() - 20
			// || me.getPoint().getX() < 30 + rect.x) {
			// return true;
			// }
		}
		return false;
	}
}

class CellPanel extends JPanel {

	private static final long serialVersionUID = 7300295515789141514L;

	private JLabel icon = new JLabel();
	private JCheckBox cbDownload = new JCheckBox();
	public JTextField name = new JTextField(20);

	protected CellPanel() {
		JPanel panelMain = new JPanel();
		panelMain.setOpaque(false);
		panelMain.setLayout(new BorderLayout());
		name.setFont(UIManager.getFont("Tree.font"));
		name.setBorder(null);
		name.setOpaque(false);
		name.setEditable(false);
		icon.setBorder(new EmptyBorder(0, 0, 0, 5));
		panelMain.add(icon, BorderLayout.LINE_START);
		panelMain.add(name, BorderLayout.CENTER);

		cbDownload.setOpaque(false);

		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(5, 5, 5, 8));

		this.add(panelMain, BorderLayout.CENTER);
		this.add(cbDownload, BorderLayout.LINE_END);
		setBackground(Color.WHITE);
	}

	public JLabel getIconComponent() {
		return icon;
	}

	public JTextField getNameComponent() {
		return name;
	}

	public JCheckBox getCbDownload() {
		return cbDownload;
	}

	public void updateData(Node node, boolean selected) {
		name.setText(node.getName());
		name.setFocusable(false);
		name.setEditable(false);
		name.setBorder(null);

		cbDownload.setSelected(node.isDownloadActive());

		// Icon Document
		if (node instanceof DocumentLeaf) {
			if (node.isDownloadActive()) {
				icon.setIcon((Icon) UIManager.get("Tree.leafIcon"));
			} else {
				icon.setIcon((Icon) UIManager.get("Tree.leafIconInactive"));
			}
		}

		// Icon folder hidden
		if (node instanceof InnerNode) {
			if (((InnerNode) node).isNodeHide()) {
				icon.setIcon((Icon) UIManager.get("Tree.openIcon"));
			} else {
				icon.setIcon((Icon) UIManager.get("Tree.closedIcon"));
			}
		}

		// Icon and text color download active
		if (node.isDownloadActive()) {
			name.setForeground(Template.COLOR_DARK_GRAY);
		} else {
			name.setForeground(Template.COLOR_GRAY);
			if (node instanceof InnerNode) {
				if (((InnerNode) node).isNodeHide()) {
					icon.setIcon((Icon) UIManager.get("Tree.openIconInactive"));
				} else {
					icon.setIcon((Icon) UIManager.get("Tree.closedIconInactive"));
				}
			}
		}

		// Selection color
		if (selected) {
			name.setForeground(Template.COLOR_ACCENT);
			setBackground(Template.COLOR_LIGHTER_GRAY);
		} else {
			setBackground(Color.WHITE);
		}
	}
}

/**
 * Class to override the width of the JTree components
 * 
 * @author Philipp
 *
 */
class CustomTreeUI extends BasicTreeUI {

	private JComponent parent;

	public CustomTreeUI(JComponent parent) {
		super();
		this.parent = parent;
	}

	@Override
	protected AbstractLayoutCache.NodeDimensions createNodeDimensions() {
		return new NodeDimensionsHandler() {
			@Override
			public Rectangle getNodeDimensions(Object value, int row, int depth, boolean expanded, Rectangle size) {
				Rectangle dimensions = super.getNodeDimensions(value, row, depth, expanded, size);
				dimensions.width = parent.getWidth() - getRowX(row, depth);
				return dimensions;
			}
		};
	}

	@Override
	protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right) {
		// do nothing.
	}

	@Override
	protected void paintVerticalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets, TreePath path) {
		// do nothing.
	}
}