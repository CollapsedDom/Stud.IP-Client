package de.danner_web.studip_client.view.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

import de.danner_web.studip_client.utils.Template;
import de.danner_web.studip_client.view.components.buttons.ModernButton;

public class DropDownBox<E> extends JComboBox<E> {

    private static final long serialVersionUID = -8505181082753207765L;

    public DropDownBox() {
        super();
        init();
    }

    public DropDownBox(ComboBoxModel<E> aModel) {
        super(aModel);
        init();
    }

    public DropDownBox(E[] items) {
        super(items);
        init();
    }

    public DropDownBox(Vector<E> items) {
        super(items);
        init();
    }

    private void init() {
        this.setFocusable(false);
        this.setUI(new MyComboBoxUI());
        this.setBorder(new LineBorder(Template.COLOR_LIGHT_GRAY));
    }

    private class MyComboBoxUI extends BasicComboBoxUI {

        @Override
        protected JButton createArrowButton() {
            class ArrowButton extends ModernButton {
                
                private static final long serialVersionUID = -2915937151436642636L;

                public ArrowButton(String string) {
                    super(string);
                    setBorder(new EmptyBorder(5, 5, 5, 5));
                }
                
                @Override
                public void paint(Graphics g) {
                    super.paint(g);

                    if (this.model.isPressed() | this.model.isSelected()
                            | this.model.isArmed()) {
                        g.setColor(Color.WHITE);
                    } else {
                        g.setColor(Template.COLOR_DARK_GRAY);
                    }
                    
                    // print triangle
                    int tHeight = getHeight()/4;
                    int i, j = 0;
                    
                    g.translate(getWidth()/2, getHeight()/2 - (tHeight/2) + 1);
                    for (i = tHeight - 1; i >= 0; i--) {
                        g.drawLine(-i, j, i, j);
                        j++;
                    }
                }
            }

            return new ArrowButton("");
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected ListCellRenderer createRenderer() {
            return new ListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                        boolean cellHasFocus) {
                    JLabel renderer = (JLabel) new DefaultListCellRenderer().getListCellRendererComponent(list, value,
                            index, isSelected, cellHasFocus);
                    if (cellHasFocus || isSelected) {
                        renderer.setBackground(Template.COLOR_ACCENT);
                        renderer.setForeground(Color.WHITE);
                    } else {
                        renderer.setBackground(Color.WHITE);
                        renderer.setForeground(Color.BLACK);
                    }

                    renderer.setBorder(new EmptyBorder(5, 5, 5, 5));
                    return renderer;
                }
            };
        }

        @Override
        protected ComboPopup createPopup() {
            return new BasicComboPopup(comboBox) {
                private static final long serialVersionUID = -1460253465809092623L;

                @Override
                protected void configurePopup() {
                    setBorderPainted(true);
                    setBorder(BorderFactory.createLineBorder(Template.COLOR_LIGHT_GRAY));
                    setOpaque(false);
                    add(scroller);
                    setFocusable(false);
                }

            };

        }
    }
}
