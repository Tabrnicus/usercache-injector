# Usercache Injector
Command line application for injecting or otherwise modifying `usercache.json` on Minecraft servers

## Description
This program modifies the `usercache.json` file on Minecraft servers to include extra entries for fake players. The reason is that servers with `online-mode=true` have the restriction that it can only support players with real usernames (i.e. the username they are trying to connect with is associated with a real Minecraft account). This is a generally a good thing to have for the most part, except that it limits the usage of specific tools such as the [player command](https://youtu.be/Lt-ooRGpLz4?t=1703) from [Carpet Mod](https://github.com/gnembon/fabric-carpet). Essentially, the issue is that you cannot summon players with fake names into the server, as the server will ask Mojang for player information, but would receive none.

Since it's a bad idea to have `online-mods=false`, this program aims to solve this issue by tricking the server into thinking it's already asked Mojang for the information for the fake player. In brief, the program works by getting a list of fake player names from a file, and will check `usercache.json` if that player already exists: 
- In the case that the player exists, it refreshes the expiry date so that there is less of a chance that the username will disappear from the file.
- In the case that the player doesn't exist in `usercache.json`, it will add it as a new entry with a fake UUID.

Keep in mind that if you use this solution for use with the `/player` command from Carpet, I would suggest that you never give any important items to bots. If the usercache entry for that bot happens to expire, and you attempt to spawn it, the entry will disappear from the cache, and the UUID will be unrecoverable. When you run the program again, the UUID will be regenerated, and the player that logs in will likely have a different inventory. However, if you run the program at least once a month (see Usage section) this should never happen.

## Installation
To "install" this program, just download the built `.jar` from the [Releases Page](https://github.com/Tardnicus/usercache-injector/releases) or build from source.

All you need is Java 8. All other libraries are precompiled in the `.jar` file. Other Java versions may work, but this was tested mainly on Java 8.

## Usage
This is a command line application. To use it, use the JRE in a shell:

```shell script
java -jar usercache-injector.jar [-h|-c] <usercache.json> [fake_names_file]
```

In plain english, run `java -jar usercache-injector.jar`, with optional parameters `-h` and `-c`, a required path to the `usercache.json` file, and an optional path to a fake names file.

Omitting the fake names file path will use the default location, which is in the same directory as the `.jar`. Feel free to omit this and use the default, but it might be useful to define a fake names file per server you run -- if you happen to run multiple.

### Options
The following options are available:

| Option | Description |
|--------|-------------|
| `-h` or `--help`            | Displays the help screen
| `-c` or `--check-usernames` | Every fake name is checked with Mojang to make sure it's fake

### Note about `--check-usernames`
This program has the ability to ask Mojang if a username is real or not with the `-c` option. Every fake username in the fake user file is checked on every execution with this flag on. To avoid unnecessary API calls, I recommend you run the program with the `-c` flag on the first execution, and every time you add another username. You'll be surprised how many weird names are registered.

### Note about frequency
This program refreshes the `expiresOn` tag for each fake user to +2 years from the run time. Unfortunately, Minecraft servers force refresh this same tag to +1 month, whenever that fake user joins. That means that in order for fake user entries to never expire, you must run this program **at least** once a month. Technically, if you run this program once, you have 2 years to log in with each fake player, but once you join, you have 1 month to run the program again before the entry expires.

I recommend you add this to your server start script as a prerequisite, so that it runs **before** every server start. Then restart your server at least once a month, and you should be good. That way you don't have to think about it.

## Building from Source
The project uses Gradle, and there's a custom task for building a fat `.jar` (has all the dependencies included in the file):

```shell script
# UNIX based OSs
./gradlew prod

# Windows
gradlew.bat prod
```

The built `.jar` will be in `build/libs`.
