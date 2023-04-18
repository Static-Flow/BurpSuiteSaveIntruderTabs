package main.java.com.staticflow;

import burp.api.montoya.MontoyaApi;

import javax.swing.*;
import java.awt.*;

/**
 * This Singleton class holds all custom state for the extension and provides a central means of accessing it.
 */
public class ExtensionState {

    // Reference to this Singleton
    private static ExtensionState state = null;

    // Burp Suite callback and helper APIs
    private MontoyaApi callbacks;

    // Reference to the Intruder Tab JTabbedPane
    private final JTabbedPane intruderTabsComponent;

    // Constructor which initializes any custom extension state
    private ExtensionState() {
        // Walk the Intruder tab component to find the JTabbedPane
        this.intruderTabsComponent = (JTabbedPane) BurpGuiControl.findFirstComponentOfType((Container) BurpGuiControl.getBaseBurpComponent("Intruder"), JTabbedPane.class);
    }

    /**
     * Getter for the Singleton State object
     * @return reference to the Singleton State object
     */
    static ExtensionState getInstance() {
        if (state == null) {
            state = new ExtensionState();
        }
        return state;
    }

    /**
     * Getter for the Intruder JTabbedPane
     * @return the Intruder JTabbedPane
     */
    JTabbedPane getIntruderTabsComponent() {
        return intruderTabsComponent;
    }

    /**
     * Getter for the Burp Suite callback and helper APIs
     * @return the Burp Suite callback and helper APIs
     */
    MontoyaApi getCallbacks() {
        return callbacks;
    }

    /**
     * Setter for the Burp Suite callback and helper APIs
     * @param callbacks the callback and helper API reference given to us by Burp Suite
     */
    void setCallbacks(MontoyaApi callbacks) {
        this.callbacks = callbacks;
    }
}
