package de.danner_web.studip_client.view.components.listrenderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import com.kitfox.svg.app.beans.SVGIcon;

import de.danner_web.studip_client.model.Model;
import de.danner_web.studip_client.model.PluginModel;
import de.danner_web.studip_client.plugin.PluginInformation;
import de.danner_web.studip_client.utils.ResourceLoader;
import de.danner_web.studip_client.utils.Template;

public class PluginListRenderer extends JPanel implements ListCellRenderer<PluginInformation> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JLabel name, author, version, website, verified;

    private PluginModel pluginModel;
    private ResourceBundle resourceBundle;
    
    private static final String PLUGIN_VERIFIIED = "de.danner_web.studip_client.view.subframe.PluginView.verified";
    private static final String PLUGIN_NOT_VERIFIIED = "de.danner_web.studip_client.view.subframe.PluginView.notVerified";
    

    public PluginListRenderer(Model model) {
        this.pluginModel = model.getPluginModel();
        this.resourceBundle = model.getResourceBundle(model.getCurrentLocale());
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        createComponents();

    }

    private void createComponents() {
        name = new JLabel();
        name.setFont(new Font(name.getFont().getFamily(), Font.BOLD, name.getFont().getSize() + 2));
        verified = new JLabel();
        verified.setFont(new Font(verified.getFont().getFamily(), Font.PLAIN, verified.getFont().getSize()));
        verified.setBorder(new EmptyBorder(0, 5, 0, 0));
        author = new JLabel();
        author.setFont(new Font(author.getFont().getFamily(), Font.PLAIN, author.getFont().getSize()));
        version = new JLabel();
        version.setFont(new Font(version.getFont().getFamily(), Font.PLAIN, version.getFont().getSize()));
        version.setAlignmentX(Component.RIGHT_ALIGNMENT);
        website = new JLabel();
        website.setFont(new Font(website.getFont().getFamily(), Font.PLAIN, website.getFont().getSize()));
        website.setForeground(Color.BLUE);
        website.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel verifiedName = new JPanel();
        verifiedName.setLayout(new BoxLayout(verifiedName, BoxLayout.X_AXIS));
        verifiedName.setOpaque(false);
        verifiedName.add(name);
        verifiedName.add(verified);
        verifiedName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        panel1.setOpaque(false);
        panel1.add(verifiedName);
        panel1.add(author);
        add(panel1);

        JPanel space = new JPanel();
        space.setOpaque(false);
        add(space);

        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
        panel2.setOpaque(false);
        panel2.add(version);
        panel2.add(website);
        add(panel2);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends PluginInformation> list, PluginInformation value,
            int index, boolean isSelected, boolean cellHasFocus) {
        name.setText(value.getName());
        if (pluginModel.isVerified(value)) {
            SVGIcon icon = ResourceLoader.getSVGIcon(Template.PLUGIN_VERIFIED);
            icon.setPreferredSize(new Dimension(16, 16));
            verified.setIcon(icon);
            verified.setForeground(Color.decode("#008000"));
            verified.setText(getLocalized(PLUGIN_VERIFIIED));
            setToolTipText(
                    "Verified Plugin. Default Server Login will be used.");
        } else {
            SVGIcon icon = ResourceLoader.getSVGIcon(Template.PLUGIN_NOT_VERIFIED);
            icon.setPreferredSize(new Dimension(16, 16));
            verified.setIcon(icon);
            verified.setForeground(Color.ORANGE);
            verified.setText(getLocalized(PLUGIN_NOT_VERIFIIED));
            setToolTipText(
                    "NOT verified Plugin. Seperate Server Login will be used due to security reasons.");
        }
        author.setText(value.getAuthor());
        version.setText("version: " + value.getVersion());
        website.setText(value.getWebsite().toString());

        if (pluginModel.isactivePlugin(value)) {
            name.setForeground(Color.BLACK);
            author.setForeground(Color.BLACK);
            version.setForeground(Color.BLACK);
            website.setForeground(Color.BLACK);
        } else {
            name.setForeground(Color.LIGHT_GRAY);
            author.setForeground(Color.LIGHT_GRAY);
            version.setForeground(Color.LIGHT_GRAY);
            website.setForeground(Color.LIGHT_GRAY);
        }

        Color background;
        Color foreground;

        // check if this cell represents the current DnD drop location
        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index) {

            background = Color.WHITE;
            foreground = Color.RED;

            // check if this cell is selected
        } else if (isSelected) {
            background = Template.COLOR_LIGHTER_GRAY;
            foreground = Color.WHITE;

            // unselected, and not the DnD drop location
        } else {
            background = Color.WHITE;
            foreground = Color.BLACK;
        }

        setBackground(background);
        setForeground(foreground);

        return this;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Template.COLOR_LIGHTER_GRAY);
        g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
    }
    
    private String getLocalized(String key) {
        return resourceBundle.getString(key);
    }

}
