# screaming-plugin-builder
Gradle plugin for making your build.gradle smaller and prepared for minecraft plugins!
This plugin extends Spigradle gradle plugin and combine it with shadow plugin. 

**This gradle plugin requires Gradle >= 6.0**

## Table of contents
* [Apply Plugin](#apply-plugin)
* [Use our task](#use-our-task)
* [Example with subprojects](#example-with-subprojects)

## Apply plugin

There is one way for apply the plugin.

```groovy
buildscript {
    repositories {
        /* these repos are needed because there are saved dependencies for our plugin */
        jcenter() 
        maven { 
          url = "https://plugins.gradle.org/m2/"
        }
        /* in this repository is our plugin */
        maven {
          url = 'https://repo.screamingsandals.org'
        }
    }
    dependencies {
        classpath 'org.screamingsandals.gradle.builder:screaming-plugin-builder:1.0.0'
    }
}

// Spigradle and Shadow will be imported automatically
apply plugin: 'org.screamingsandals.gradle.builder'
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

allprojects {
    group = 'com.example.plugin'
    version = '1.0.0'
}

buildscript {
    repositories { // apply these repositories
        jcenter()
        maven { 
          url = "https://plugins.gradle.org/m2/"
        }
        maven {
          url = 'https://repo.screamingsandals.org'
        }
    }
    dependencies { // apply this dependency
        classpath 'org.screamingsandals.gradle.builder:screaming-plugin-builder:LATEST_VERSION_HERE'
    }
}

subprojects {

    apply plugin: 'java' // apply plugin with your favourite language 'java', 'groovy', 'kotlin' etc.
    apply plugin: 'org.screamingsandals.gradle.builder' // then apply our plugin

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

    shadowJar {
       relocate 'org.screamingsandals.simpleinventories', 'org.screamingsandals.simpleinventories2' // add some relocation if you shade something inside
    }
    
    sourceCompatibility = '1.8' // now add here your favourite java version (at least 1.8, recommended 11)
}


```
