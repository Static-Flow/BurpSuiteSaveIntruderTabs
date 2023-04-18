# BurpSuiteSaveIntruderTabs
This Burp Suite Extension allows you to save Intruder tabs for a project so they persist across restarts!

## How it works

If the extension is loaded, anytime burp closes, the extension will store every Intruder tab within the Project using the Montoya Persistence APIs. 

When you restart Burp Suite and the extension is loaded, it recreates the stored Intruder tabs and reinserts them!

Note: _If you are a Burp Community User, this extension will not work as only Enterprise and Professional versions have project files. I am looking into replicating this functionality locally._

