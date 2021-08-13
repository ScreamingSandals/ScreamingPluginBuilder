# screaming-plugin-builder
Gradle plugin for making your build.gradle smaller and prepared for ScreamingLib plugins!

**This gradle plugin requires Gradle >= 6.0**

## Table of contents
* [Apply Plugin](#apply-plugin)
* [Use our task](#use-our-task)
* [Example with subprojects](#example-with-subprojects)

## Apply plugin

You need to modify 2 files in your project, it's simple!


First, go to your `settings.gradle` and add this ad the **TOP**:
```groovy
pluginManagement {
  repositories {
    maven {
      url = "https://repo.screamingsandals.org/public/"
    }
    gradlePluginPortal()
  }
}
```

Then go to your build.gradle:
```groovy
plugins {
  id 'org.screamingsandals.plugin-builder' version '1.0.49'
}

// Spigradle and Shadow will be imported automatically
apply plugin: 'org.screamingsandals.plugin-builder'
```

## Use our task
Just add this line to top of your build.gradle and than run default task of your plugin
```groovy
defaultTasks 'screamCompile'
```
This task will:
* compile plugins and shade all dependencies with scope `implementation` into new jar file
* prepare your pom.xml to right format and publish it to your local maven repository (folder `.m2`)
* If you run gradle with switch `-PscreamingRepository="file:///directory/with/repository"`
  * then it'll also publish your work here

## Example with subprojects
```groovy
defaultTasks 'clean', 'screamCompile' // use our task as default

plugins {
  id 'org.screamingsandals.plugin-builder' version '1.0.49'
}

allprojects {
    group = 'com.example.plugin'
    version = '1.0.0'
}

subprojects {
    apply plugin: 'java' // apply plugin with your favourite language 'java', 'groovy', 'kotlin' etc.
    apply plugin: 'org.screamingsandals.plugin-builder' // then apply our plugin

    repositories { // add some custom dependencies
        maven {
            url = 'https://repo.extendedclip.com/content/repositories/placeholderapi/'
        }
    }
    
    dependencies {
      // lombok and jetbrains annotations are added automatically
    
      // add some provided dependencies
      compileOnly paper()
      
      // add some compiled dependencies
      implementation 'org.screamingsandals.simpleinventories:SimpleInventories-Core:1.0.0'
    }
  
    enableShadowPlugin() // enable shading

    shadowJar {
       relocate 'org.screamingsandals.simpleinventories', 'org.screamingsandals.simpleinventories2' // add some relocation if you shade something inside
    }
    
    sourceCompatibility = 11
}


```

## Repository uploading
For uploading artifacts to repository, only few environment variables are needed.

### Gitlab Repository
```
GITLAB_REPO -> address of gitlab repository
GITLAB_TOKEN -> private token
```

### Nexus Repository
```
NEXUS_URL_SNAPSHOT -> nexus snapshot repository
NEXUS_URL_RELEASE -> nexus release repository
NEXUS_USERNAME -> nexus username
NEXUS_PASSWORD -> nexus password
```
