package main.java.com.staticflow;

import burp.api.montoya.core.Marker;
import burp.api.montoya.http.HttpService;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.persistence.PersistedList;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class which contains the various functionality for this extension
 */
public final class Utilities {

    private static final char INTRUDER_OFFSET = '§';
    private static final String EXTENSION_PERSISTENCE_KEY = "SAVED_INTRUDER_TAB_NAMES";

    private Utilities() {}

    /**
     * This method is called on Extension load and checks for any saved intruder tabs. It first checks for the {@link Utilities#EXTENSION_PERSISTENCE_KEY} key
     * ,which is only present if there are saved Intruder tabs, and loads the names of all saved tabs via {@link Utilities#importIntruderTab(String)}
     */
    public static void loadExistingIntruderTabs() {
        if( ExtensionState.getInstance().getCallbacks().persistence().extensionData().stringListKeys().contains(EXTENSION_PERSISTENCE_KEY)) {
            ExtensionState.getInstance().getCallbacks().persistence().extensionData().getStringList(EXTENSION_PERSISTENCE_KEY).forEach(Utilities::importIntruderTab);
        }
    }

    /**
     * This method imports each saved Intruder tab identified by {@link Utilities#loadExistingIntruderTabs()}. It takes the intruder tab name, uses it as a
     * key to retrieve the stored {@link HttpRequest} from the persistence API, sends the {@link HttpRequest} to the Intruder, and updates the new Intruder
     * tab with its old name.
     * @param intruderTabName The name of the Intruder tab to load
     */
    private static void importIntruderTab(String intruderTabName) {
        ExtensionState.getInstance().getCallbacks().logging().logToOutput("importing: "+intruderTabName);
        HttpRequest httpRequest = ExtensionState.getInstance().getCallbacks().persistence().extensionData().getHttpRequest(intruderTabName);
        ExtensionState.getInstance().getCallbacks().intruder().sendToIntruder(httpRequest);
        ExtensionState.getInstance().getIntruderTabsComponent().setTitleAt(
            ExtensionState.getInstance().getIntruderTabsComponent().getSelectedIndex(),
            intruderTabName
        );
    }

    /**
     * This method exports all non-default Intruder tabs using the Persistence API. The intruder tab names are used as keys and the values for each tab
     * are created by {@link Utilities#exportIntruderTab(int)}. The following steps are taken:<br>
     *      1. A stream of ints the size of the number of Intruder tabs is created <Br>
     *      2. Each int, corresponding to an Intruder tab index is passed to {@link Utilities#exportIntruderTab(int)} <br>
     *      3. If {@link Utilities#exportIntruderTab(int)} returns True, retrieve the title of the Intruder tab and store it into `intruderTabTitles` <br>
     *      4. If {@link Utilities#exportIntruderTab(int)} returns False, ignore the corresponding Intruder tab <br>
     *      5. Persist the list of Intruder tab keys via the Montoya Persistence APIs <Br>
     */
    public static void exportIntruderTabs() {
        PersistedList<String> intruderTabTitles = PersistedList.persistedStringList();
        intruderTabTitles.addAll(IntStream.range(0, ExtensionState.getInstance().getIntruderTabsComponent().getTabCount())
                .filter(Utilities::exportIntruderTab)
                .mapToObj(i -> ExtensionState.getInstance().getIntruderTabsComponent().getTitleAt(i))
                .collect(Collectors.toList()));
        ExtensionState.getInstance().getCallbacks().logging().logToOutput("Exporting tabs: "+ new ArrayList<>(intruderTabTitles));
        ExtensionState.getInstance().getCallbacks().persistence().extensionData().setStringList(EXTENSION_PERSISTENCE_KEY, intruderTabTitles);
    }

    /**
     * This method walks the Swing component tree of an Intruder tab using
     * {@link main.java.com.staticflow.BurpGuiControl#findFirstComponentOfType}
     * to find the JTextField containing the Target string
     * @param currentIntruderTabComponent The Intruder tab component to retrieve the target URL from
     * @return the target text of the Intruder tab
     */
    private static String getIntruderTabTarget(Component currentIntruderTabComponent) {
        return ((JTextField) BurpGuiControl.findFirstComponentOfType((Container) currentIntruderTabComponent, JTextField.class)).getText();
    }

    /**
     * This method walks the Swing component tree of an Intruder tab using
     * {@link main.java.com.staticflow.BurpGuiControl#findFirstComponentOfType}
     * to find the JTextArea containing the Intruder request with its markers
     * @param currentIntruderTabComponent The Intruder tab component to retrieve the request text from
     * @return the JTextArea of the Intruder tab containing the request
     */
    private static JTextArea getIntruderTabTextArea(Component currentIntruderTabComponent) {

        List<Component> textAreas = BurpGuiControl.findAllComponentsOfType((Container) currentIntruderTabComponent, JTextArea.class);
        return (JTextArea) textAreas.get(1);

    }

    /**
     * This method, called by {@link Utilities#exportIntruderTabs()}, saves the Intruder data at the corresponding tab index using the following steps:<br>
     *      1. The title for the tab is retrieved<br>
     *      2. The text contents of the Intruder tab are retrieved <br>
     *      3. If the Intruder tab is the default on (a POST to /example on the host http://localhost:80), ignore it and return False <br>
     *      4. If it isn't the default one, get a list of all offsets by searching the Intruder text for the {@link Utilities#INTRUDER_OFFSET} character<br>
     *      5. Convert the list of offsets into {@link Marker} pairs
     *      6. Build a new {@link HttpRequest} from the Intruder request text, a new {@link HttpService} built from the Intruder target url, and computed {@link Marker Markers}<br>
     *      7. Store it within the Project using the Persistence APIs with the Intruder tab title as the key
     * @param index The Intruder Tab index
     * @return True if it successfully exported, false otherwise
     */
    private static boolean exportIntruderTab(int index) {
        Component currentIntruderComponent = ExtensionState.getInstance().getIntruderTabsComponent().getComponentAt(index);
        String intruderTabTitle =  ExtensionState.getInstance().getIntruderTabsComponent().getTitleAt(index);
        String intruderTarget = getIntruderTabTarget(currentIntruderComponent);
        String intruderText = Utilities.getIntruderTabTextArea(currentIntruderComponent).getText();
        //don't save the default one, sorry if you're actually trying to run an intruder attack against this endpoint while using this extension
        if(!intruderText.startsWith("POST /example") && !intruderTarget.equals("http://localhost:80")) {
            List<Integer> intruderOffsetList = IntStream.range(0, intruderText.length())
                    .filter(i -> intruderText.charAt(i) == INTRUDER_OFFSET).boxed()
                    .collect(Collectors.toList());

            ArrayList<Marker> markers = new ArrayList<>();
            for (int i = 1; i < intruderOffsetList.size(); i += 2) {
                Marker marker = Marker.marker(intruderOffsetList.get(i - 1), intruderOffsetList.get(i));
                markers.add(marker);
            }

            HttpRequest httpRequest = HttpRequest.httpRequest(intruderText);
            httpRequest = httpRequest.withService(HttpService.httpService(intruderTarget));
            httpRequest = httpRequest.withMarkers(markers);
            ExtensionState.getInstance().getCallbacks().logging().logToOutput("Exporting intruder request: "+httpRequest);
            ExtensionState.getInstance().getCallbacks().persistence().extensionData().setHttpRequest(intruderTabTitle,httpRequest);
            return true;
        }
        return false;
    }
}