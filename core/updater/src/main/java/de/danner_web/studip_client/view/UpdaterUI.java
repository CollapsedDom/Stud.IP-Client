package de.danner_web.studip_client.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;

import de.danner_web.studip_client.model.UpdateModel;
import de.danner_web.studip_client.utils.ResourceLoader;
import de.danner_web.studip_client.utils.Template;

public class UpdaterUI extends JFrame implements Observer {

    private static final long serialVersionUID = -1116149953702147921L;

    private UpdateModel model;

    private JProgressBar progressBar;
    private JLabel statusLabel;

    public UpdaterUI(UpdateModel model) {
        super();
        this.model = model;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Set JLabel font default non bold
        Font oldLabelFont = UIManager.getFont("Label.font");
        UIManager.put("Label.font", oldLabelFont.deriveFont(Font.PLAIN));
        model.addObserver(this);
        
        createView();
    }

    private void createView() {

        // Set Icon
        try {
            BufferedImage image = ImageIO.read(ResourceLoader.getURL(Template.FAVICON));
            this.setIconImage(image);
        } catch (IOException e) {
            // Nichts tun
        }
        this.setTitle("StudIP Client Updater");
        JPanel pane = (JPanel) this.getContentPane();
        pane.setBackground(Color.WHITE);
        pane.setLayout(new BorderLayout());
        pane.setBorder(new EmptyBorder(20, 20, 20, 20));

        statusLabel = new JLabel("Initialize Updater");
        pane.add(statusLabel, BorderLayout.NORTH);

        progressBar = new JProgressBar();
        progressBar.setValue(1);
        progressBar.setStringPainted(false);
        progressBar.setUI(new ModernProgressUI());
        progressBar.setBackground(Color.WHITE);
        progressBar.setBorder(new EmptyBorder(0, 0, 0, 0));

        pane.add(progressBar, BorderLayout.CENTER);

        setPreferredSize(new Dimension(400, 100));
        setSize(getPreferredSize());
        setResizable(false);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            progressBar.setValue(model.getProgress());
            statusLabel.setText(model.getStatusText());
        }
    }

    public void close() {
        model.deleteObserver(this);
        this.dispose();
    }

    private static class ModernProgressUI extends BasicProgressBarUI {

        @Override
        protected void paintDeterminate(Graphics g, JComponent c) {

            if (!(g instanceof Graphics2D)) {
                return;
            }

            Insets b = new Insets(5, 5, 5, 5); // area for border
            int barRectWidth = progressBar.getWidth() - (b.right + b.left);
            int barRectHeight = 16 - (b.top + b.bottom);
            if (barRectWidth <= 0 || barRectHeight <= 0) {
                return;
            }

            int cellLength = getCellLength();
            int cellSpacing = getCellSpacing();

            // amount of progress to draw
            int amountFull = getAmountFull(b, barRectWidth, barRectHeight);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // draw the cells
            if (cellSpacing == 0 && amountFull > 0) {
                // draw one big Rect because there is no space between cells
                g2.setStroke(new BasicStroke((float) barRectHeight, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            } else {
                // draw each individual cell
                g2.setStroke(new BasicStroke((float) barRectHeight, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0.f,
                        new float[] { cellLength, cellSpacing }, 0.f));
            }

            // Background
            g2.setColor(Template.COLOR_LIGHT_GRAY);
            g2.drawLine(b.left, (barRectHeight / 2) + b.top, barRectWidth + b.left, (barRectHeight / 2) + b.top);

            // Foreground
            g2.setColor(Template.COLOR_ACCENT);
            g2.drawLine(b.left, (barRectHeight / 2) + b.top, amountFull + b.left - barRectHeight / 2,
                    (barRectHeight / 2) + b.top);

        }
    }
}
