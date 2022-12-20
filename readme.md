# starsector-fixes

This is a tiny javaagent thing I once wrote to make starsector work on JVM 17.
It handles the removal of this one call to `DirectBuffer.cleaner()` as well as adding a few libraries
that were previously part of JVM.

### JVM 8

To make starsector work with JVM 8 you need to add those JVM parameters to the launch script:

```
-XX:+UnlockDiagnosticVMOptions
-XX:-BytecodeVerificationRemote
```

Those are needed to disable bytecode verification - which is where the JVM complains about method and
field names in starsector code having dots in them - this is caused by their obfuscation method.

### JVM 11

To make starsector work with JVM 11 you need to do this:

1. Remove/replace a call to DirectBuffer.cleaner (in 1 place) - there I believe that the buffer can be just GCd,
   I replaced that method with a no-op and played the game for an hour and it seemed to work and not OOM
2. Add JAXB api and core libraries in place of the ones removed from Java,
   change import of IndentingXMLStreamWriter from internal to one present in those libs (in 1 place).
   Well changing one import can be done with a shim without having to edit more bytecode.

Both of those steps are handled by the javaagent that I wrote and you are reading the readme of.

First, compile it with `./gradlew build` (or grab the jar from the github releases page) and then
add `-javaagent:path-to-resulting-jar.jar` to the JVM parameters in the launch script.
The jar will be `build/libs/starsector-fixes.jar` when you build it.

### JVM 17

And lastly for JVM 17 you also need to add a few `--add-opens` parameters to the launch script, listed below:

```
--add-opens=java.base/java.util=ALL-UNNAMED
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED
--add-opens=java.base/java.text=ALL-UNNAMED
--add-opens=java.desktop/java.awt.font=ALL-UNNAMED
--add-opens=java.base/sun.nio.ch=ALL-UNNAMED
--add-opens java.base/java.lang.ref=ALL-UNNAMED
```

## Issues

- If you get an error similar to this:

```
[java] Inconsistency detected by ld.so: dl-lookup.c: 111: check_match: 
    Assertion `version->filename == NULL || 
    ! _dl_name_match_p (version->filename, map)' failed!
[java] Java Result: 127
```

It has nothing to do with starsector-fixes,
see [this](https://stackoverflow.com/questions/55847497/how-do-i-troubleshoot-inconsistency-detected-dl-lookup-c-111-java-result-12)
(tl;dr: try using AdoptOpenJDK instead of what you were using).

- When opening the intel view by pressing E the game crashes with `Comparison method violates its general contract`.

Well, you can try adding the `-Djava.util.Arrays.useLegacyMergeSort=true` parameter and for some people it somehow
fixes it, but the game already does literally that, but in code - so this shouldn't actually happen ¯\\\_(ツ)_/¯

## License

As with most of my things, the license is MIT, meaning that to use this in any way possible you include the
contents of the LICENSE file in any meaningful way - it has my name on top of it (aka 'attribution').
When using the jar you literally don't have to do anything as the LICENSE file is included in it.
