package main.java.com.staticflow;

import burp.IBurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionStateListener;

/**
 * This extension provides a means of saving Intruder tabs when closing Burp Suite.
 * To accomplish this, the extension serializes all Intruder tabs and stores them in a
 * folder called `.BurpSuiteSaveIntruderTabs` located in the user home directory.
 * When Burp Suite is reopened again the serialized Intruder tabs that match the project
 * are unserialized and injected back into the Intruder tab.
 */
public class BurpSuiteSaveIntruderTabs implements IBurpExtender, IExtensionStateListener {
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks iBurpExtenderCallbacks) {
        //Hand callback reference to the ExtensionState singleton for easy access throughout extension code
        ExtensionState.getInstance().setCallbacks(iBurpExtenderCallbacks);
        //Check for export folder existence and load any Intruder tabs that match this project
        Utilities.loadExistingIntruderTabs();
        //Watch for extension unloading
        iBurpExtenderCallbacks.registerExtensionStateListener(this);
    }

    /**
      When the extension unloads all current Intruder tabs are serialized and stored in the
     `.BurpSuiteSaveIntruderTabs/{Project Name}` folder.

     NOTE: We use `extensionUnloaded` here because it will get called when Burp Suite exits.
            It will also fire if the user unloads the extension manually.
            Trying to hook the application closing is too late in the process to use Burp Suite's
            API calls to facilitate exporting Intruder tabs.
     */
    @Override
    public void extensionUnloaded() {
        Utilities.exportIntruderTabs();
    }
}
