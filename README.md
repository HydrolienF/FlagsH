[download]: https://img.shields.io/github/downloads/HydrolienF/FlagsH/total
[downloadLink]: https://hangar.papermc.io/Hydrolien/FlagsH

# FlagsH
Minecraft plugins to display banners as flags.

[ ![download][] ][downloadLink]

[![Click to view video](https://img.youtube.com/vi/94QfPndYXYM/maxresdefault.jpg)](https://youtu.be/94QfPndYXYM)
[See video here](https://youtu.be/94QfPndYXYM)


## Use

this plugin tries to be as intuitive as possible. No command is needed to create, extend and remove a flag.

 - Place a banner item on a block to get a banner.
 - Place a banner item on a block while sneaking to get a flag.
 - Extends flag or banner by placing new banner item onto the flag or banner.
 - Break flag or banner by hitting it.

## Compatibility

FlagsH work with Spigot, Paper, Folia (and should work with any fork).

FlagsH is compatible with [Towny](https://github.com/TownyAdvanced/Towny) claim permission.


## Admin commands

 - `/flagsh` or `fh` Print the version.
 - `/fh reload` Reload config & flags data file.
 - `/fh list` Print the list of all flags.
 - `/fh remove <id>|all` Remove a flag on the list based on its possition or all flags.


## Install
Download last version.
Place it in `plugins/` in your server files.
After 1st launch you can edit config in `plugins/FlagH/config.yml`


## Test
To test plugin you need Java 17+ to compile & package: `./gradlew assemble`
Then you need an 1.20 Minecraft server with PaperMc or fork to run it.


## Statistics
[![bStats Graph Data](https://bstats.org/signatures/bukkit/flagsh.svg)](https://bstats.org/plugin/bukkit/FlagsH/19981)
