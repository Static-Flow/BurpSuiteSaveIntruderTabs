package main.java.com.staticflow;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class which contains the various functionality for this extension
 */
public final class Utilities {

    private Utilities() {}

    /**
     * Called when the extension is loaded, this method first checks if the folder structure constructed by {@link #getIntruderTabFilePath} exists
     * and creates it if it does not. It then calls {@link #importIntruderTabs} to walk the directory for serialized Intruder tabs to import.
     */
    public static void loadExistingIntruderTabs() {
        //Get the path to the directory containing the serialized Intruder tabs for this project
        File serializedIntruderTabsDirectory = new File(getIntruderTabFilePath());
        //if it doesn't exist
        if (!serializedIntruderTabsDirectory.exists()) {
            //if creating the directory structure failed
            if( !serializedIntruderTabsDirectory.mkdirs() ) {
                ExtensionState.getInstance().getCallbacks().printError("Could not create export directory");
                return;
            }
        }
        //import all serialized Intruder tabs in the directory
        importIntruderTabs(serializedIntruderTabsDirectory);
    }

    /**
     * This method loops through every serialized {@link IntruderAttack}  in the supplied directory and converts in back into a {@link IntruderAttack} POJO
     * and imports it into Intruder.
     * @param directory Path to serialized {@link IntruderAttack IntruderAttacks}
     */
    private static void importIntruderTabs(File directory) {
        //loop over every file in the supplied directory
        for(String file : Stream.of(Objects.requireNonNull(directory.listFiles())).filter(file -> !file.isDirectory()).map(File::getAbsolutePath).collect(Collectors.toSet())) {
            ExtensionState.getInstance().getCallbacks().printOutput("Importing Intruder Tab");
            //attempt to load the file contents
            try (FileInputStream fileIn = new FileInputStream(file)) {
                ObjectInputStream in = new ObjectInputStream(fileIn);
                //unserialize the file into a IntruderAttack POJO
                IntruderAttack intruderAttack = (IntruderAttack) in.readObject();
                //import the IntruderAttack data into Intruder creating a new tab
                ExtensionState.getInstance().getCallbacks().sendToIntruder(
                        intruderAttack.getHttpService().getHost(),
                        intruderAttack.getHttpService().getPort(),
                        intruderAttack.getHttpService().getProtocol().equals("https"),
                        intruderAttack.getRequestTemplate());
                in.close();
            } catch (IOException | ClassNotFoundException i) {
                ExtensionState.getInstance().getCallbacks().printError(i.toString());
            }
        }
    }

    /**
     * This method calls {@link #clearExportFolder()} to remove the previously exported Intruder tabs,
     * then calls {@link #exportIntruderTab(Component) exportIntruderTab} to convert each Intruder tab to a {@link IntruderAttack},
     * serialize it, and write it to the directory specified by {@link #getIntruderTabFilePath()}.
     */
    public static void exportIntruderTabs() {
        try {
            //attempt to clear the previously exported Intruder tabs from the last time this Burp Suite project was closed
            clearExportFolder();
        } catch (IOException e) {
            //if we fail to clear the export folder the method aborts
            ExtensionState.getInstance().getCallbacks().printOutput("Couldn't clean export directory: "+e);
            return;
        }
        //for each Intruder tab
        for(int index = 0; index < ExtensionState.getInstance().getIntruderTabsComponent().getTabCount(); index++) {
            try {
                //give the tab to exportIntruderTab for exporting
                exportIntruderTab(ExtensionState.getInstance().getIntruderTabsComponent().getComponentAt(index));
            } catch (MalformedURLException e) {
                ExtensionState.getInstance().getCallbacks().printError(e.toString());
            }
        }
    }

    /**
     * Get the name of the Burp Suite project by extracting it from the main title
     * @return the name of the Burp Suite Project
     */
    private static String getBurpProjectName() {
        //For all the Swing frames
        for(Frame frame : Frame.getFrames()) {
            //If its title starts with Burp Suite
            if(frame.isVisible() && frame.getTitle().startsWith(("Burp Suite")))
            {
                //return the project name
                return frame.getTitle().split("-")[1].trim();
            }
        }
        return "";
    }

    /**
     * This method walks the Swing component tree of an Intruder tab using
     * {@link main.java.com.staticflow.BurpGuiControl#getComponentAtPath(Container, int[]) getComponentAtPath}
     * to find the JTextField containing the Target string
     * @param intruderTabComponent the Intruder tab Swing Component
     * @return the target string of the Intruder tab
     */
    private static String getIntruderTabTarget(Component intruderTabComponent) {
        return ((JTextField) BurpGuiControl.getComponentAtPath(
                (Container) intruderTabComponent,
                new int[]{0,1, 2, 0, 1})).getText();
    }

    /**
     * This method walks the Swing component tree of an Intruder tab using
     * {@link main.java.com.staticflow.BurpGuiControl#getComponentAtPath(Container, int[]) getComponentAtPath}
     * to find the JTextArea containing the Intruder request with its markers
     * @param intruderTabComponent the Intruder tab Swing Component
     * @return the JTextArea of the Intruder tab containing the request
     */
    private static JTextArea getIntruderTabTextArea(Component intruderTabComponent) {
        return (JTextArea) BurpGuiControl.getComponentAtPath(
                (Container) intruderTabComponent,
                new int[]{0, 1, 2, 8, 1, 0, 0});

    }

    /**
     * This method returns the directory where the serialized {@link IntruderAttack IntruderAttacks} for the current Burp Suite project context are stored
     * @return the directory where the serialized {@link IntruderAttack IntruderAttacks} for the current Burp Suite project context are stored
     */
    private static String getIntruderTabFilePath() {
        return System.getProperty("user.home")+File.separator+".BurpSuiteSaveIntruderTabs"+File.separator+getBurpProjectName()+ File.separator;
    }

    /**
     * This method performs the conversion of an Intruder tab into a {@link IntruderAttack} POJO, serialization, and storage into the directory specified by
     * {@link #getIntruderTabFilePath()}
     * @param intruderAttackComponent The Intruder Tab Swing component
     * @throws MalformedURLException if the Target string for the Intruder Tab extracted by {@link #getIntruderTabTarget(Component) getIntruderTabTarget} is invalid
     */
    private static void exportIntruderTab(Component intruderAttackComponent) throws MalformedURLException {
        //build a new IntruderAttack from the Intruder tab's Target and Request
        IntruderAttack intruderAttack = new IntruderAttack(
                new HttpService(Utilities.getIntruderTabTarget(intruderAttackComponent)),
                Utilities.getIntruderTabTextArea(intruderAttackComponent).getText().getBytes());
        //don't save the default one
        if(!(new String(intruderAttack.getRequestTemplate())).startsWith("POST /example")) {
            //attempt to create a file for this serialized IntruderAttack
            try (FileOutputStream fileOut = new FileOutputStream(Utilities.getIntruderTabFilePath() + UUID.randomUUID())) {
                ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
                objectOut.writeObject(intruderAttack);
                objectOut.close();
                ExtensionState.getInstance().getCallbacks().printOutput("Exported Intruder Tab successfully");
            } catch (Exception ex) {
                ExtensionState.getInstance().getCallbacks().printError(ex.toString());
            }
        }
    }

    /**
     * This method clears the export directory specified by {@link #getIntruderTabFilePath()} to prepare it for adding the new exported Intruder tabs
     * @throws IOException if FileUtils fails to clear the directory
     */
    private static void clearExportFolder() throws IOException {
        FileUtils.cleanDirectory(new File(getIntruderTabFilePath()));
    }

}
