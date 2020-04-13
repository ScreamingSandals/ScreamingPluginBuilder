# screaming-plugin-builder
Gradle plugin for making your build.gradle smaller and prepared for minecraft plugins

## You build.gradle with this plugin (with subprojects)
```groovy
defaultTasks 'screamCompile' // use our task as default

allprojects {
    group = 'com.example.plugin'
    version = '1.0.0'
}

buildscript {
    repositories { // apply these repositories
        jcenter()
        maven {
          url = 'https://repo.screamingsandals.org'
        }
    }
    dependencies { // apply this dependency
        classpath 'org.screamingsandals.gradle.builder:screaming-plugin-builder:1.0.0'
    }
}

subprojects {

    apply plugin: 'java' // apply plugin with your favourite language 'java', 'groovy', 'kotlin' etc.
    apply plugin: 'org.screamingsandals.gradle.builder' // then apply our plugin

    repositories { // add some custom dependencies
        maven {
            url = 'https://repo.screamingsandals.org'
        }
        maven {
            url = 'https://repo.extendedclip.com/content/repositories/placeholderapi/'
        }
    }
    
    dependencies {
      // add some provided dependencies
      compileOnly paper()
      compileOnly lombok()
      
      // add some compiled dependencies
      implementation 'org.screamingsandals.simpleinventories:SimpleInventories-Core:1.0.0'
    }

    shadowJar {
       relocate 'org.screamingsandals.simpleinventories', 'org.screamingsandals.simpleinventories2' // add some relocation if you shade something inside
    }
    
    sourceCompatibility = '1.8' // now add here your favourite java version (at least 1.8, recommended 11)
}


```
