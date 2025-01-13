# 4.4.1
Fix a missing function in Spiggot API.

# 4.4.0 Resilient data update

Flag data are now saved on Entity.
This makes flags removable even after server crashes or after the plugin data file have been deleted.
Flags which data can't be saved because of a crash won't be removable by `/fh remove [id]` nor show in the flag list. But they still can be destroyed by player hand.
Most servers won't be affected by this update, but the update will benefit to the server that crashes some time.

Explicitly support 1.21.4.

# 4.3.1
Explicitly support 1.21.3

# 4.3.0
Fix mangrove fences offset.

# 4.2.1
Support 1.21.1

# 4.2.0
- Add debug logs for events.
- Fix banner disabled not placing vanilla banner. (same for flag disabled)
- Cancel event if player is forbiden to interact with a flag.

# 4.1.0
Banner placed from off hand can no longer remove an other item (#10).
Banner placed with off hand can have alternate mods. Alternates modes allow to place vanilla banner or, banner in switchable block instead of flags (#7).
Make flags removable after a map change or a /kill command.

# 4.0.3
Explicit support for 1.21
Work with [1.20 - 1.21]
Needs Java 21

# 4.0.2
Explicit support for 1.20.5 & 1.20.6.
Work with [1.20 - 1.20.6]

# 4.0.1

Explicit support for 1.20.3 & 1.20.4.
Still work with 1.20, 1.20.1, 1.20.2.

# 4.0.0

Compatible with towny.

# 3.2.1

Remove banner from player inventory when placing it.

# 3.1.2

Add admin command.
Prevent player from interacting with flag in ADVENTURE.