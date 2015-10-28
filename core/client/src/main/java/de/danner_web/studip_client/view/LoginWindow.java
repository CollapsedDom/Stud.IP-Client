package de.danner_web.studip_client.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.data.OAuthServer;
import de.danner_web.studip_client.model.LoginModel;
import de.danner_web.studip_client.model.Model;
import de.danner_web.studip_client.utils.ResourceLoader;
import de.danner_web.studip_client.utils.Template;
import de.danner_web.studip_client.utils.oauth.ResponseCode;
import de.danner_web.studip_client.view.components.buttons.BackButton;
import de.danner_web.studip_client.view.components.buttons.ModernButton;
import de.danner_web.studip_client.view.components.listrenderer.UniCellListRenderer;

public class LoginWindow extends JFrame implements Observer, DetachableView {

    /**
     * Logger dieser Klasse.
     */
    private static Logger logger = LogManager.getLogger(LoginWindow.class);

    private static final long serialVersionUID = 1L;

    private static final String SUFIX = "de.danner_web.studip_client.view.LoginWindow.";

    private static final String AUTHORIZE_BUTTON_NAME = SUFIX + "authorizeButtonlabel";
    private static final String NEXT_BUTTON_NAME = SUFIX + "nextLabel";

    private static final String LOGIN_WINDOW_TITLE = SUFIX + "LoginWindowTitle";
    private static final String UNI_NAME_LABEL = SUFIX + "uniNameLabel";
    private static final String AUTHORIZE = SUFIX + "authorize";
    private static final String ERROR_SERVER_LABEL = SUFIX + "errorServer";
    private static final String APP_NOT_AUTHORIZED_IN_STUDIP_LABEL = SUFIX + "errorStudipNotAuthorized";

    private static int WINDOW_HIGHT = 350;
    private static int WINDOW_WIDTH = 400;
    private static int PANEL_WIDTH = 300;
    private static int LIST_HEIGHT = 165;

    private ModernButton authorizeButton, nextButton;
    private BackButton backButton;
    private JList<OAuthServer> uniList;
    private JLabel errorLabel, uniNameLabel;

    private JPanel mainPanel, navigationPanel;

    private LoginModel loginModel;
    private Model model;

    private boolean requestRunning;
    private EventListenerList listeners = new EventListenerList();
    private Locale currentLocale;
    private ResourceBundle resourceBundle;

    LoginWindow(Model model) {
        super();

        this.model = model;
        this.loginModel = model.getPluginModel().getLoginModel();
        this.currentLocale = model.getCurrentLocale();
        this.resourceBundle = model.getResourceBundle(currentLocale);

        createView();
        model.addObserver(this);
        loginModel.addObserver(this);
    }

    private JPanel createWaitingPanel() {
        logger.entry();
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(0, 50, 10, 50));
        panel.setOpaque(false);

        panel.add(new JLabel(getLocalized(AUTHORIZE)));

        try {
            BufferedImage myPicture = ImageIO.read(ResourceLoader.getURL(Template.AUTHORIZE));
            JLabel picLabel = new JLabel();
            ImageIcon icon = new ImageIcon(myPicture);
            picLabel.setIcon(icon);
            picLabel.setPreferredSize(new Dimension(PANEL_WIDTH, LIST_HEIGHT));
            panel.add(picLabel);
        } catch (IOException e) {
        }

        nextButton = new ModernButton(getLocalized(NEXT_BUTTON_NAME));
        nextButton.setPreferredSize(new Dimension(PANEL_WIDTH, 30));
        nextButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                requestAccessToken();
            }
        });
        panel.add(nextButton);

        return logger.exit(panel);
    }

    private void requestAccessToken() {
        if (loginModel.isServerSelected()) {
            SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

                @Override
                protected String doInBackground() throws Exception {
                    if (!requestRunning) {
                        requestRunning = true;
                        boolean success = (loginModel.getAccessToken() == ResponseCode.SUCCESS);
                        if (!success) {
                            errorLabel.setText(getLocalized(APP_NOT_AUTHORIZED_IN_STUDIP_LABEL));
                        } else {
                            errorLabel.setText("");
                            model.getPluginModel().activateNewPlugin(loginModel.getPluginInformation(),
                                    loginModel.getOAuthConnector());
                        }
                    }
                    return "done";
                }
            };
            errorLabel.setText("");
            setBussyMouse(true);
            worker.execute();
        }
    }

    private JLabel createLabel(String name) {
        JLabel label = new JLabel(name);
        label.setFocusable(false);
        label.setFont(new Font(label.getFont().getFamily(), Font.PLAIN, label.getFont().getSize()));
        return label;
    }

    private JPanel createNavigationPanel() {
        logger.entry();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Template.COLOR_LIGHTER_GRAY);

        panel.setPreferredSize(new Dimension(PANEL_WIDTH, 45));

        backButton = new BackButton();

        backButton.setVisible(false);
        backButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (loginModel.isServerSelected()) {
                    getContentPane().remove(mainPanel);
                    mainPanel = createUniSelectPanel();
                    getContentPane().add(mainPanel, BorderLayout.CENTER);

                    loginModel.resetSelectedServer();
                    backButton.setVisible(false);
                    errorLabel.setText("");

                    pack();
                    repaint();
                }
            }
        });

        panel.add(backButton, BorderLayout.WEST);

        errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED);
        panel.add(errorLabel, BorderLayout.CENTER);

        return logger.exit(panel);
    }

    private JPanel createUniSelectPanel() {
        logger.entry();
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(0, 50, 10, 50));
        panel.setOpaque(false);

        uniNameLabel = createLabel(getLocalized(UNI_NAME_LABEL));
        panel.add(uniNameLabel);

        uniList = new JList<OAuthServer>(new Vector<>(loginModel.getServers()));
        uniList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                authorizeButton.setEnabled(true);
            }
        });
        uniList.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                if (arg0.getClickCount() == 2 && uniList.getSelectedValue() != null) {
                    requestServerSelection();
                }
            }
        });
        uniList.setCellRenderer(new UniCellListRenderer());
        uniList.setForeground(Template.COLOR_ACCENT);

        JScrollPane scrollPane = new JScrollPane(uniList);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(PANEL_WIDTH, LIST_HEIGHT));

        panel.add(scrollPane);

        authorizeButton = new ModernButton(getLocalized(AUTHORIZE_BUTTON_NAME));
        authorizeButton.setPreferredSize(new Dimension(PANEL_WIDTH, 30));
        authorizeButton.setEnabled(false);
        authorizeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                requestServerSelection();
            }
        });

        panel.add(authorizeButton);

        return logger.exit(panel);
    }

    private void requestServerSelection() {
        SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

            @Override
            protected String doInBackground() throws Exception {
                if (!requestRunning) {
                    requestRunning = true;

                    // set the selected server as selected in model
                    boolean success = loginModel.selectServer(uniList.getSelectedValue());
                    if (!success) {
                        errorLabel.setText(getLocalized(ERROR_SERVER_LABEL));
                    } else {
                        errorLabel.setText("");
                    }

                    // request RequestToken
                    ResponseCode code = loginModel.getRequestToken();
                    if (code != ResponseCode.SUCCESS) {
                        errorLabel.setText(getLocalized(ERROR_SERVER_LABEL));
                    } else {
                        errorLabel.setText("");
                    }

                    loginModel.openBrowser();

                }
                return "done";
            }
        };

        errorLabel.setText("");
        setBussyMouse(true);

        worker.execute();
    }

    private void createView() {
        logger.entry();
        this.setTitle(getLocalized(LOGIN_WINDOW_TITLE));

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                fireNavigationEvent();
            }
        });

        // Set Icon
        try {
            BufferedImage image = ImageIO.read(ResourceLoader.getURL(Template.FAVICON));
            this.setIconImage(image);
        } catch (IOException e) {
            // Nichts tun
        }

        getContentPane().setLayout(new BorderLayout(5, 5));

        navigationPanel = createNavigationPanel();
        navigationPanel.setOpaque(false);
        getContentPane().add(navigationPanel, BorderLayout.NORTH);

        mainPanel = createUniSelectPanel();
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HIGHT));
        getContentPane().setBackground(Color.WHITE);
        setSize(getPreferredSize());
        setResizable(false);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        logger.exit();
    }

    public void detacheView() {
        this.model.deleteObserver(this);
    }

    private String getLocalized(String key) {
        return resourceBundle.getString(key);
    }

    void addNavigationListener(NavigationListener listener) {
        if (listener != null) {
            this.listeners.add(NavigationListener.class, listener);
        }
    }

    private void setBussyMouse(boolean wait) {
        if (wait) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            this.setCursor(Cursor.getDefaultCursor());
        }

    }

    private void fireNavigationEvent() {

        for (NavigationListener listener : listeners.getListeners(NavigationListener.class)) {
            listener.actionPerformed(NavigationAction.LOGIN_TO_PLUGINS);
        }
    }

    @Override
    public void update(Observable o, final Object arg) {
        logger.entry(o, arg);
        if (o == loginModel) {
            requestRunning = false;
            setBussyMouse(false);
            if (loginModel.isAutherized()) {
                requestRunning = false;
                logger.debug("switch to Mainwindow");
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        fireNavigationEvent();
                    }
                });

            } else if (loginModel.isServerSelected()) {

                logger.debug("update to waiting page");
                requestRunning = false;
                setBussyMouse(false);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // Change View to loginInput
                        getContentPane().remove(mainPanel);
                        mainPanel = createWaitingPanel();
                        getContentPane().add(mainPanel, BorderLayout.CENTER);

                        backButton.setVisible(true);

                        pack();
                        repaint();
                    }
                });
            }
        }
        logger.exit();
    }

}
