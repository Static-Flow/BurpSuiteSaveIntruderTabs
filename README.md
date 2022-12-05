# BurpSuiteSaveIntruderTabs
This Burp Suite Extension allows you to save Intruder tabs for a project so they persist across restarts!

## How it works

If the extension is loaded, anytime burp closes the extension will serialize all Intruder tabs and place them in a directory structure inside your home directory
called `.BurpSuiteSaveIntruderTabs\{Project Name}` where `{Project Name}` is the active project when you closed Burp Suite .

When you restart Burp Suite and the extension is loaded, it checks the `.BurpSuiteSaveIntruderTabs\{Project Name}` for any serilaized Intruder tabs and reinserts them
into the Intruder tab!

Note: _If you are a Burp Community User, all projects are the same `Temporary Project` thus this extension operates as a global store of all Intruder Tabs so is likely not
very helpful._

