// apply { plugin("maven-publish") }
apply(plugin = "maven-publish")

val jitpack = (systemEnv("JITPACK") ?: "false").toBoolean()
fun systemEnv(key: String): String? = System.getenv(key)
fun property(key: String, project: Project? = null): String? =
    project?.findProperty(key)?.toString() ?: System.getProperty(key)

afterEvaluate {
    if (jitpack) {
        group = arrayOf(systemEnv("GROUP"), systemEnv("ARTIFACT")).joinToString(".")
        version = systemEnv("VERSION")
    } else {
        val artifactGroup = property("ARTIFACT_GROUP", project)
        if (artifactGroup.isNullOrEmpty().not()) group = artifactGroup
        version = property("ARTIFACT_VERSION", project)
            ?: version.takeUnless { it == "unspecified" } ?: "1.0.0-SNAPSHOT"
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("product") {
                println("Publication $project ===> components ${components.joinToString { it.name }}")
                components.firstOrNull {
                    it.name == "release" || it.name == "java"
                }?.let {
                    println("Publication $project ===> component from ${it.name}")
                    from(it)
                }
                // artifact(sourcesJar)
                pom {
                    withXml {
                        val root = asNode()
                        val depsNode = root.children().find find@{
                            val node = it as? groovy.util.Node ?: return@find false
                            val name = it.name() as? groovy.namespace.QName ?: return@find false

                            println("Publication pom root $project ===> ${node}, ${name.localPart}")
                            name.localPart == "dependencies"
                        } as? groovy.util.Node

                        println("Publication pom dependencies $project ===> $depsNode")

                        depsNode?.children()?.forEach each@{ dependence ->
                            if (dependence !is groovy.util.Node) return@each
                            // println("Publication pom dependence $project ===> dependence")
                            val groupId = dependence.children().firstNotNullOfOrNull find@{ group ->
                                if (group !is groovy.util.Node) return@find false
                                val name = group.name() as? groovy.namespace.QName
                                    ?: return@find false
                                val value = group.value() as? groovy.util.NodeList
                                    ?: return@find false
                                println("Publication pom dependence groupId ${name} ${value}")
                                if (name.localPart == "groupId") value.text() else null
                            }

                            // 排除指定组ID的依赖
                            if (groupId == "org.jetbrains.kotlin") {
                                depsNode.remove(dependence)
                            }
                        }
                    }
                }

                println("Publication $project ===> $jitpack $groupId:$artifactId:$version")
            }

        }

        repositories {
            maven {
                if (!jitpack) url = uri("${rootProject.layout.buildDirectory.file("repo").get()}")
            }
        }
    }
}
