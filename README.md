# hub
Smart home hub that implements the same api as SmartThings

The project started out as a POC but it is now functional and has an extension system for adding additional functionality.
It could use some help with documentation and testing at this point as well as any missing functionality requests or implementations.

This project is under active development and will possibly see breaking changes as it matures, however every care will be taken to avoid this and perform safe updates.

Requirements:
- Java 11
- Ember based zigbee stick (ie Nortek HUSBZB-1) if you want to control zigbee devices
- Z/IP Gateway and Z-Wave Bridge Controller stick (ie UZB-7) or ZWave JS Server and any ZWave controller stick (ie the above mentioned HUSBZB-1) if you want to control z-wave devices

## Installing on a Raspberry Pi (or another compatible linux system)
https://github.com/parrot-ha/scripts/blob/main/rpi/README.md

## Compile (requires JDK 11 and npm to be installed):
1. Check out code to new directory.
2. run ```npm install``` in hub/ui directory
3. run ```./gradlew build``` in root directory
4. Distribution file will be in hub/build/distributions

## Running from sourcecode:

1. Compile code (see above)
2. Create a directory called deviceHandlers and place your device handler groovy code there.  See the devicehandlers repository for examples.
3. Create a directory called automationApps and place your automation app groovy code there.  See the automationApps repository for examples.
4. Extract tar or zip file from hub/build/distributions/ to the directory you want to run in.
5. run the hub or hub.bat script in bin
6. UI is available at localhost:7000


##  Intellij

If running in Intellij, be sure to set the config script to point to the config.groovy file:
https://www.jetbrains.com/help/idea/groovy-compiler.html
File -> Settings -> Build, Execution, Deployment -> Compiler -> Groovy Compiler
Path to configscript: \[code directory\]/device-handlers/config.groovy

Intellij Run Application configuration:

Click Run -> Edit Configurations
Click plus sign -> Application

Name: Main
Module: 1.11 java version
Classpath: parrotha-hub.hub.main
Main class: org.parrotha.service.Main

Click "Modify options" -> Add before launch task

Before launch:
1. Build
2. Run Gradle task 'parrotha-hub.hub.main: npmBuildFrontend'
3. Run Gradle task 'parrotha-hub.hub.main: copyIntellijResources'

## Publish Jar Files
To publish jars to local maven repository run the following command:  
```./gradlew publishToMavenLocal```

## Populate license information

```./gradlew licenseFormatMain licenseFormatTest```

