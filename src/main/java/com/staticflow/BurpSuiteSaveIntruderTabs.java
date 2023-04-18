package main.java.com.staticflow;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.ExtensionUnloadingHandler;

/**
 * This extension provides a means of saving Intruder tabs when closing Burp Suite.
 * To accomplish this, the extension serializes all Intruder tabs and stores them in a
 * folder called `.BurpSuiteSaveIntruderTabs` located in the user home directory.
 * When Burp Suite is reopened again the serialized Intruder tabs that match the project
 * are unserialized and injected back into the Intruder tab.
 */
public class BurpSuiteSaveIntruderTabs implements BurpExtension, ExtensionUnloadingHandler {

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

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().registerUnloadingHandler(this);
        ExtensionState.getInstance().setCallbacks(api);
        Utilities.loadExistingIntruderTabs();
    }
}
