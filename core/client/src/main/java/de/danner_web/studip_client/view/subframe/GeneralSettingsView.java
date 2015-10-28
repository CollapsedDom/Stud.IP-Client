package de.danner_web.studip_client.view.subframe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.model.SettingsModel;
import de.danner_web.studip_client.model.SettingsModel.NotificationOrientation;
import de.danner_web.studip_client.model.Model;
import de.danner_web.studip_client.view.DetachableView;
import de.danner_web.studip_client.view.components.DropDownBox;
import de.danner_web.studip_client.view.components.ModernSlider;
import de.danner_web.studip_client.view.components.buttons.ModernToggleButton;
import de.danner_web.studip_client.view.components.buttons.ModernButton;

public class GeneralSettingsView extends JPanel implements Observer, DetachableView {

    private static final long serialVersionUID = 4827015066453761389L;
    private static Logger logger = LogManager.getLogger(GeneralSettingsView.class);

    private ResourceBundle resourceBundle;
    private static final String GENERAL_SETTINGS = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.generalSettings";
    private static final String AUTO_START = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.autoStart";
    private static final String AUTO_UPDATE = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.autoUpdate";
    private static final String HIDDEN_START = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.hiddenStart";
    private static final String LANGUAGE = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.language";
    private static final String NOTIFICATION_SETTINGS = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.notificationSettings";
    private static final String NOTIFICAION_ENABLE = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.notificationEnable";
    private static final String NOTIFICAION_DELETE_TIME = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.notificationDeleteTime";
    private static final String NOTIFICAION_MAX_NUMBER = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.notificationMaxNumber";
    private static final String NOTIFICAION_ORIENTATION = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.notificationOrientation";
    private static final String NOTIFICAION_ORIENTATION_OPTION_TOP = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.notificationOrientationOptionTop";
    private static final String NOTIFICAION_ORIENTATION_OPTION_BOTTOM = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.notificationOrientationOptionBottom";
    private static final String ACCOUNT_SETTINGS = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.accountSettings";
    private static final String ACCOUNT_DELETE = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.accountDelete";
    private static final String ACCOUNT_DELETE_LABEL = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.accountDeleteLabel";
    private static final String ACCOUNT_DELETE_DIALOG = "de.danner_web.studip_client.view.subframe.GeneralSettingsView.accountDeleteDialog";

    private boolean requestRunning;
    
    private Model model;
    private SettingsModel settings;

    private ModernToggleButton autoStartButton, autoUpdateButton, hiddenStartButton, notificationEnableButton;
    private JLabel lblAutostart, lblAutoupdate, lblHiddenstart, lblGeneralSettings, lblLanguage,
            lblNotificationSettings, lblNotifications, lblDeleteTime, lblMaxNumber, lblOrientation, lblAccountSettings,
            lblAccountDelete;
    private ModernButton deleteaccount;
    private Map<String, NotificationOrientation> orientation = null;
    private DropDownBox<String> orientationSelection = null;
    
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 25;
    private static final int SLIDER_WIDTH = 200;

    public GeneralSettingsView(Model model) {
        logger.entry(model);
        this.model = model;
        this.resourceBundle = model.getResourceBundle(model.getCurrentLocale());
        this.settings = model.getSettingsModel();
        model.addObserver(this);
        settings.addObserver(this);
        createView();
        updateLocale();
        logger.exit();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void createView() {
        JPanel pane = new JPanel();
        pane.setBackground(Color.WHITE);
        pane.setLayout(new GridBagLayout());

        // GridBag constraints
        GridBagConstraints gbHeadline = new GridBagConstraints();
        gbHeadline.fill = GridBagConstraints.HORIZONTAL;
        gbHeadline.insets = new Insets(2, 0, 2, 0);
        gbHeadline.gridx = 0;

        GridBagConstraints gbLeft = new GridBagConstraints();
        gbLeft.anchor = GridBagConstraints.WEST;
        gbLeft.insets = new Insets(3, 20, 3, 0);
        gbLeft.weightx = 1.0;
        gbLeft.gridx = 0;

        GridBagConstraints gbRightValue = new GridBagConstraints();
        gbRightValue.anchor = GridBagConstraints.EAST;
        gbRightValue.insets = new Insets(3, 0, 3, 0);
        gbRightValue.weightx = 0;
        gbRightValue.gridx = 1;

        GridBagConstraints gbRight = new GridBagConstraints();
        gbRight.anchor = GridBagConstraints.EAST;
        gbRight.insets = new Insets(3, 0, 3, 20);
        gbRight.weightx = 0.1;
        gbRight.gridx = 2;

        // General Settings
        lblGeneralSettings = new JLabel(getLocalized(GENERAL_SETTINGS));
        lblGeneralSettings
                .setFont(lblGeneralSettings.getFont().deriveFont(lblGeneralSettings.getFont().getStyle() | Font.BOLD));
        gbHeadline.gridy = 0;
        pane.add(lblGeneralSettings, gbHeadline);

        gbHeadline.insets = new Insets(20, 0, 2, 0);

        // AutoStart
        lblAutostart = new JLabel(getLocalized(AUTO_START));
        autoStartButton = new ModernToggleButton(settings.isAutoStart());
//        autoStartButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        autoStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                toogleAutoStart();
            }
        });
        gbLeft.gridy = 1;
        pane.add(lblAutostart, gbLeft);
        gbRight.gridy = 1;
        pane.add(autoStartButton, gbRight);

        // AutoUpdate
        lblAutoupdate = new JLabel(getLocalized(AUTO_UPDATE));
        autoUpdateButton = new ModernToggleButton(settings.isAutoUpdate());
        autoUpdateButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        autoUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                toogleAutoUpdate();
            }
        });
        gbLeft.gridy = 2;
        pane.add(lblAutoupdate, gbLeft);
        gbRight.gridy = 2;
        pane.add(autoUpdateButton, gbRight);

        // HiddenStart
        lblHiddenstart = new JLabel(getLocalized(HIDDEN_START));
        hiddenStartButton = new ModernToggleButton(settings.isHidden());
        hiddenStartButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        hiddenStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                toogleHiddenStart();
            }
        });
        gbLeft.gridy = 3;
        pane.add(lblHiddenstart, gbLeft);
        gbRight.gridy = 3;
        pane.add(hiddenStartButton, gbRight);

        // Language
        lblLanguage = new JLabel(getLocalized(LANGUAGE));
        Locale[] languages = new Locale[3];
        languages[0] = Locale.ENGLISH;
        languages[1] = Locale.GERMAN;
        languages[2] = (new Locale.Builder()).setLanguage("de").setRegion("BY").build();
        final DropDownBox<Locale> languageSelection = new DropDownBox<Locale>(languages);
        languageSelection.setSelectedItem(model.getCurrentLocale());
        languageSelection.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        languageSelection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Locale petName = (Locale) languageSelection.getSelectedItem();
                model.setCurrentLocale(petName);
            }
        });
        gbLeft.gridy = 4;
        pane.add(lblLanguage, gbLeft);
        gbRight.gridy = 4;
        pane.add(languageSelection, gbRight);

        // Notification Settings
        lblNotificationSettings = new JLabel(getLocalized(NOTIFICATION_SETTINGS));
        lblNotificationSettings.setFont(
                lblNotificationSettings.getFont().deriveFont(lblNotificationSettings.getFont().getStyle() | Font.BOLD));
        gbHeadline.gridy = 5;
        pane.add(lblNotificationSettings, gbHeadline);

        // Notification enabled
        lblNotifications = new JLabel(getLocalized(NOTIFICAION_ENABLE));
        notificationEnableButton = new ModernToggleButton(settings.isNotificationEnabled());
        notificationEnableButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        notificationEnableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                toogleNotification();
            }
        });
        gbLeft.gridy = 6;
        pane.add(lblNotifications, gbLeft);
        gbRight.gridy = 6;
        pane.add(notificationEnableButton, gbRight);

        // Notification delete time
        lblDeleteTime = new JLabel(getLocalized(NOTIFICAION_DELETE_TIME));
        final JLabel currentDeleteTime = new JLabel(settings.getNotificationDeleteTime() + "");
        final ModernSlider nDTSlider = new ModernSlider();
        nDTSlider.setBackground(Color.WHITE);
        nDTSlider.setMaximum(30);
        nDTSlider.setMinimum(1);
        nDTSlider.setValue(settings.getNotificationDeleteTime());
        nDTSlider.setPreferredSize(new Dimension(SLIDER_WIDTH, BUTTON_HEIGHT));
        nDTSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                currentDeleteTime.setText(nDTSlider.getValue() + "");
            }
        });
        nDTSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                settings.setNotificationDeleteTime(nDTSlider.getValue());
            }
        });

        gbLeft.gridy = 7;
        pane.add(lblDeleteTime, gbLeft);
        gbRightValue.gridy = 7;
        pane.add(currentDeleteTime, gbRightValue);
        gbRight.gridy = 7;
        pane.add(nDTSlider, gbRight);

        // Notification max number
        lblMaxNumber = new JLabel(getLocalized(NOTIFICAION_MAX_NUMBER));
        final JLabel currentMaxNumber = new JLabel(settings.getNotificationMaxCount() + "");
        final ModernSlider nMNSlider = new ModernSlider();
        nMNSlider.setBackground(Color.WHITE);
        nMNSlider.setMaximum(5);
        nMNSlider.setMinimum(1);
        nMNSlider.setValue(settings.getNotificationMaxCount());
        nMNSlider.setPreferredSize(new Dimension(SLIDER_WIDTH, BUTTON_HEIGHT));
        nMNSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                currentMaxNumber.setText(nMNSlider.getValue() + "");
            }
        });
        nMNSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                settings.setNotificationMaxCount(nMNSlider.getValue());
            }
        });

        gbLeft.gridy = 8;
        pane.add(lblMaxNumber, gbLeft);
        gbRightValue.gridy = 8;
        pane.add(currentMaxNumber, gbRightValue);
        gbRight.gridy = 8;
        pane.add(nMNSlider, gbRight);

        // Notification orientation
        lblOrientation = new JLabel(getLocalized(NOTIFICAION_ORIENTATION));
        orientationSelection = new DropDownBox<String>();
        orientation = new HashMap<String, NotificationOrientation>();
        orientation.put(getLocalized(NOTIFICAION_ORIENTATION_OPTION_TOP), NotificationOrientation.TOP);
        orientation.put(getLocalized(NOTIFICAION_ORIENTATION_OPTION_BOTTOM), NotificationOrientation.BOTTOM);
        ComboBoxModel orientationModel = new DefaultComboBoxModel(orientation.keySet().toArray());
        orientationSelection.setModel(orientationModel);
        orientationSelection.setSelectedItem(getKeyByValue(orientation, settings.getNotificationOrientation()));
        orientationSelection.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        orientationSelection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                settings.setNotificationOrientation(orientation.get(orientationSelection.getSelectedItem()));
            }
        });

        gbLeft.gridy = 9;
        pane.add(lblOrientation, gbLeft);
        gbRight.gridy = 9;
        pane.add(orientationSelection, gbRight);

        // Account Settings
        lblAccountSettings = new JLabel(getLocalized(ACCOUNT_SETTINGS));
        lblAccountSettings
                .setFont(lblAccountSettings.getFont().deriveFont(lblAccountSettings.getFont().getStyle() | Font.BOLD));
        gbHeadline.gridy = 10;
        pane.add(lblAccountSettings, gbHeadline);

        // Account delete
        lblAccountDelete = new JLabel(getLocalized(ACCOUNT_DELETE_LABEL));
        deleteaccount = new ModernButton(getLocalized(ACCOUNT_DELETE));
        deleteaccount.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        deleteaccount.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int n = JOptionPane.showConfirmDialog(null, getLocalized(ACCOUNT_DELETE_DIALOG), "StudIP Client",
                        JOptionPane.YES_NO_OPTION);

                if (n == 0)
                    model.getPluginModel().deleteDefaultServer();
            }
        });

        gbLeft.gridy = 11;
        pane.add(lblAccountDelete, gbLeft);
        gbRight.gridy = 11;
        pane.add(deleteaccount, gbRight);

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
        add(pane, BorderLayout.NORTH);
    }

    private static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void toogleAutoStart() {
        logger.entry();
        SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

            @Override
            protected String doInBackground() throws Exception {
                if (!requestRunning) {
                    requestRunning = true;
                    settings.setAutoStart(autoStartButton.isSelected());
                }
                return "done";
            }
        };
        worker.execute();
        logger.exit();
    }

    private void toogleAutoUpdate() {
        logger.entry();
        SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

            @Override
            protected String doInBackground() throws Exception {
                if (!requestRunning) {
                    requestRunning = true;
                    settings.setAutoUpdate(autoUpdateButton.isSelected());
                }
                return "done";
            }
        };
        worker.execute();
        logger.exit();
    }

    private void toogleHiddenStart() {
        logger.entry();
        SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

            @Override
            protected String doInBackground() throws Exception {
                if (!requestRunning) {
                    requestRunning = true;
                    settings.setHidden(hiddenStartButton.isSelected());
                }
                return "done";
            }
        };
        worker.execute();
        logger.exit();
    }

    private void toogleNotification() {
        logger.entry();
        SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

            @Override
            protected String doInBackground() throws Exception {
                if (!requestRunning) {
                    requestRunning = true;
                    settings.setNotificationEnabled(notificationEnableButton.isSelected());
                }
                return "done";
            }
        };
        worker.execute();
        logger.exit();
    }

    public void detacheView() {
        model.deleteObserver(this);
        settings.deleteObserver(this);
    }

    private String getLocalized(String key) {
        return resourceBundle.getString(key);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void updateLocale() {
        resourceBundle = model.getResourceBundle(model.getCurrentLocale());

        lblAutostart.setText(getLocalized(AUTO_START));
        lblAutoupdate.setText(getLocalized(AUTO_UPDATE));
        lblHiddenstart.setText(getLocalized(HIDDEN_START));
        lblGeneralSettings.setText(getLocalized(GENERAL_SETTINGS));
        lblLanguage.setText(getLocalized(LANGUAGE));
        lblNotificationSettings.setText(getLocalized(NOTIFICATION_SETTINGS));

        lblNotifications.setText(getLocalized(NOTIFICAION_ENABLE));
        lblDeleteTime.setText(getLocalized(NOTIFICAION_DELETE_TIME));
        lblMaxNumber.setText(getLocalized(NOTIFICAION_MAX_NUMBER));
        lblOrientation.setText(getLocalized(NOTIFICAION_ORIENTATION));

        orientation = new HashMap<String, NotificationOrientation>();
        orientation.put(getLocalized(NOTIFICAION_ORIENTATION_OPTION_TOP), NotificationOrientation.TOP);
        orientation.put(getLocalized(NOTIFICAION_ORIENTATION_OPTION_BOTTOM), NotificationOrientation.BOTTOM);
        ComboBoxModel orientationModel = new DefaultComboBoxModel(orientation.keySet().toArray());
        orientationSelection.setModel(orientationModel);
        orientationSelection.setSelectedItem(getKeyByValue(orientation, settings.getNotificationOrientation()));

        deleteaccount.setText(getLocalized(ACCOUNT_DELETE));

    }

    @Override
    public void update(Observable o, Object arg) {
        logger.entry(o, arg);
        if (o instanceof Model && arg == null) {
            updateLocale();
            // Settings neu laden
        }
        if (o instanceof SettingsModel) {
            requestRunning = false;
            autoStartButton.setSelected(settings.isAutoStart());
            autoUpdateButton.setSelected(settings.isAutoUpdate());
            hiddenStartButton.setSelected(settings.isHidden());
            notificationEnableButton.setSelected(settings.isNotificationEnabled());
        }
        logger.exit();
    }
}
