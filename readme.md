# starsector-fixes
This is a tiny javaagent thing I once wrote to make starsector work on JVM 17.
It handles the removal of this one call to `DirectBuffer.cleaner()` as well as adding a few libraries
that were previously part of JVM.

### JVM 8
To make starsecotr work with JVM 8 you need to add those JVM parameters to the launch script:
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

First, compile it with `./gradlew build` and then add `-javaagent:path-to-resulting-jar.jar` to the
JVM parameters in the launch script. The jar is `build/libs/starsector-fixes.jar` after running the build.

### JVM 17
And lastly for JVM 17 you also need to add a few `--add-opens` parameters to the launch script, listed below:
```
--add-opens=java.base/java.util=ALL-UNNAMED
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED
--add-opens=java.base/java.text=ALL-UNNAMED
--add-opens=java.desktop/java.awt.font=ALL-UNNAMED
--add-opens=java.base/sun.nio.ch=ALL-UNNAMED
```

# License
As with most of my things, the license is MIT, meaning that to use this in any way possible you include the
contents of the LICENSE file in any meaningful way - it has my name on top of it (aka 'attribution').
When using the jar you literally don't have to do anything as the LICENSE file is included in it.
