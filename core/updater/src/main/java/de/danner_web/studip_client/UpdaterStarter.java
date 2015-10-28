package de.danner_web.studip_client;

import javax.swing.JOptionPane;

import de.danner_web.studip_client.model.UpdateModel;
import de.danner_web.studip_client.view.UpdaterUI;

/**
 * This class only parses the input and manages Errorprinting
 * 
 * @author Dominik Danner
 *
 */
public class UpdaterStarter {

    /**
     * This Method updates the StudIP Client with the given File from the
     * Parameters.
     * 
     * If the File is an older Version or not a StudIP Client jar, the update
     * won't proceed.
     * 
     */
    public static void main(String args[]) {

        UpdateModel model = new UpdateModel();

        // Check if Update is enabled
        if (model.isAutoUpdate() || model.isFirstInstall()) {

            // Check if newer version is available
            if (model.isNewerVersionAvailable()) {

                if (!model.isFirstInstall()) {
                    int n = 0;
                    // Only show OptionPane if no Client Application is
                    // installed.
                    n = JOptionPane.showConfirmDialog(null, "Do you want to update your StudIP Client?",
                            "StudIP Client - New Update Available", JOptionPane.YES_NO_OPTION);
                    if (n != 0) {
                        // Launch StudIP Client
                        model.launchAndExit();
                    }
                }

                // Build Gui and update the client
                UpdaterUI ui = new UpdaterUI(model);
                model.updateClient();
                ui.close();
            }
        }

        // Launch StudIP Client
        model.launchAndExit();
    }
}
