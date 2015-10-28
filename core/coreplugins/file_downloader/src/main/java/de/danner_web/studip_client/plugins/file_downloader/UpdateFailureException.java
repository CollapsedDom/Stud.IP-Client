package de.danner_web.studip_client.plugins.file_downloader;

/**
 * This Exception can be thrown, if an update failes.
 * 
 * @author Danner Dominik
 *         
 */
public class UpdateFailureException extends Exception {
    
    /**
     * Default ID for the Exception
     */
    private static final long serialVersionUID = -4675552031484015390L;
    
    /**
     * Constructor
     */
    public UpdateFailureException() {
        super();
    }
    
    /**
     * Constructor with text to be written.
     * 
     * @param text
     *            to be written.
     */
    public UpdateFailureException(String text) {
        super(text);
    }
    
}
