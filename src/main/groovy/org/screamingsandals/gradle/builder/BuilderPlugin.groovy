package org.screamingsandals.gradle.builder;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.plugins.ObjectConfigurationAction
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList

class BuilderPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.repositories.jcenter()
		project.repositories.mavenCentral()
		project.repositories.mavenLocal()
		
		project.apply([
			plugin: 'com.github.johnrengelman.shadow'
		])
		
		project.apply([
			plugin: 'kr.entree.spigradle'
		])
		
		project.apply([
			plugin: MavenPublishPlugin.class
		])
	
	
		project.tasks.getByName("spigotPluginYaml").enabled = false
		
		PublishingExtension publishing = project.extensions.getByName("publishing")
		publishing.publications.create("maven", MavenPublication) {
			it.from(project.components.getByName("java"))
			
			it.pom.withXml {
				List<String> usedArtifacts = new ArrayList<>();
				
				NodeList list = it.asElement().getElementsByTagName("dependency")
				for (int i = 0; i < list.getLength(); i++) {
					Element element = list.item(i)
					String str = element.getElementsByTagName('groupId').item(0).getTextContent()
					str += ":" + element.getElementsByTagName('artifactId').item(0).getTextContent()
					if (!usedArtifacts.contains(str)) {
					   usedArtifacts.add(str)
					}
					
					element.getParentNode().removeChild(element)
				}
				
				Element dependencyList = it.asElement().getElementsByTagName("dependencies").item(0)
				Document document = dependencyList.getOwnerDocument()
				
				Iterator iterator = project.configurations.collectMany { it.allDependencies }.findAll { it instanceof ExternalModuleDependency }.iterator()
				while (iterator.hasNext()) {
					ExternalModuleDependency dependency = iterator.next()
					String str = dependency.getGroup() + ":" + dependency.getName()
					if (!usedArtifacts.contains(str) && !dependency.getName().equals("spigradle")) {
						usedArtifacts.add(str)
						Element node = document.createElement("dependency")
						Element groupId = document.createElement("groupId")
						groupId.appendChild(document.createTextNode(dependency.getGroup()))
						node.appendChild(groupId);
						Element artifactId = document.createElement("artifactId")
						artifactId.appendChild(document.createTextNode(dependency.getName()))
						node.appendChild(artifactId);
						Element version = document.createElement("version")
						version.appendChild(document.createTextNode(dependency.getVersion()))
						node.appendChild(version);
						Element scope = document.createElement("scope")
						scope.appendChild(document.createTextNode("provided"))
						node.appendChild(scope);
						dependencyList.appendChild(node)
					}
				}

			}
		}
		
		if (project.hasProperty("screamingRepository")) {
			publishing.repositories {
				it.maven({MavenArtifactRepository repository ->
					repository.url = project.property("screamingRepository")
				})
			}
		}
		
		List tasks = ["clean", "shadowJar", "publishToMavenLocal"]
		
		if (project.hasProperty("screamingRepository")) {
			tasks.add("publish")
		}
		
		project.tasks.create("screamCompile").dependsOn = tasks
	}
	
}