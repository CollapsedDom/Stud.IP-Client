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
        if (model.isClientAppMissing()) {
            updateClient(model);
        } else if (model.isAutoUpdate()) {
            
            // Check if newer version is available
            if (model.isNewerVersionAvailable()) {
                
                int n = 0;
                // Only show OptionPane if no Client Application is
                // installed.
                n = JOptionPane.showConfirmDialog(null, "Do you want to update your StudIP Client?",
                        "StudIP Client - New Update Available", JOptionPane.YES_NO_OPTION);
                if (n != 0) {
                    // Launch StudIP Client
                    model.launchAndExit();
                }
                updateClient(model);
            }
        }
        
        // Launch StudIP Client
        if (!model.isClientAppMissing()) {
            //model.launchAndExit();
        //} else {
            String err_text = "ERROR: Could not start Stud.IP Client because of missing Client Application.";
            System.err.println(err_text);
            JOptionPane.showMessageDialog(null, err_text, "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static void updateClient(UpdateModel model) {
        
        // Build Gui and update the client
        UpdaterUI ui = new UpdaterUI(model);
        boolean success = model.updateClient();
        
        // If update fails, wait 5 seconds and then close the UI.
        if (!success) {
            try {
                // This is the main thread.
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
        }
        ui.close();
    }
}
