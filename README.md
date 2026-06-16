# Cerulean

Cerulean is an advancement optimization mod.

This branch targets Minecraft `26.2` and builds both `fabric` and `neoforge` from one codebase using direct Loom no-remap configuration.

Build commands:

```powershell
.\gradlew.bat buildAll
.\gradlew.bat build "-Ploom.platform=fabric"
.\gradlew.bat build "-Ploom.platform=neoforge"
```
