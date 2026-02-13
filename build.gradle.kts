// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.13.2" apply false
    id("com.android.library") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.dagger.hilt.android") version "2.55" apply false
}

import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.GradleException
import java.io.File

val moduleDependencyRules = mapOf(
    ":core" to emptySet(),
    ":domain" to setOf(":core"),
    ":data" to setOf(":core", ":domain"),
    ":feature-clock" to setOf(":core", ":data", ":domain"),
    ":feature-chime" to setOf(":core", ":data", ":domain", ":feature-clock"),
    ":feature-settings" to setOf(":core", ":data", ":domain"),
    ":app" to setOf(":core", ":data", ":domain", ":feature-clock", ":feature-chime", ":feature-settings")
)

fun collectRelativeResourcePaths(baseDir: File): Set<String> {
    if (!baseDir.exists()) return emptySet()
    return baseDir.walkTopDown()
        .filter { it.isFile }
        .map { it.relativeTo(baseDir).invariantSeparatorsPath }
        .filterNot { it.endsWith("/.DS_Store") || it == ".DS_Store" }
        .toSet()
}

fun collectResourceSymbols(baseDir: File): Set<String> {
    if (!baseDir.exists()) return emptySet()

    val valueResourcePattern = Regex(
        """<(string|plurals|string-array|array|color|dimen|integer|bool|style|attr|id|declare-styleable)\b[^>]*\bname="([^"]+)""""
    )
    val symbols = mutableSetOf<String>()

    baseDir.walkTopDown()
        .filter { it.isFile && it.extension == "xml" }
        .forEach { file ->
            val relativePath = file.relativeTo(baseDir).invariantSeparatorsPath
            if (relativePath.endsWith("/.DS_Store") || relativePath == ".DS_Store") return@forEach

            val typeDir = relativePath.substringBefore('/')
            if (!typeDir.startsWith("values")) {
                symbols.add("$typeDir/${file.nameWithoutExtension}")
                return@forEach
            }

            val content = file.readText()
            valueResourcePattern.findAll(content).forEach { match ->
                val type = match.groupValues[1]
                val name = match.groupValues[2]
                symbols.add("$type/$name")
            }
        }

    return symbols
}

tasks.register("checkModuleBoundaries") {
    group = "verification"
    description = "Fail build if module dependencies violate the allowed module graph."

    doLast {
        val violations = mutableListOf<String>()

        rootProject.subprojects
            .filter { moduleDependencyRules.containsKey(it.path) }
            .forEach { project ->
                val allowed = moduleDependencyRules.getValue(project.path)
                val deps = mutableSetOf<String>()
                project.configurations
                    .filter { it.isCanBeResolved }
                    .forEach { config ->
                        config.dependencies.forEach { dep ->
                            if (dep is ProjectDependency) {
                                val depClass = dep.javaClass
                                val depPath = when {
                                    depClass.methods.any { it.name == "getDependencyProjectPath" } ->
                                        depClass.getMethod("getDependencyProjectPath").invoke(dep) as String
                                    depClass.methods.any { it.name == "getDependencyProject" } ->
                                        (depClass.getMethod("getDependencyProject").invoke(dep) as org.gradle.api.Project).path
                                    depClass.methods.any { it.name == "getPath" } ->
                                        depClass.getMethod("getPath").invoke(dep) as String
                                    else -> dep.toString()
                                }
                                deps.add(depPath)
                            }
                        }
                    }

                deps.forEach { dep ->
                    if (dep == project.path) return@forEach
                    if (dep !in allowed) {
                        violations.add("${project.path} -> $dep (not allowed)")
                    }
                }
            }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "Module boundary violations detected:\n" + violations.joinToString("\n")
            )
        }
    }
}

tasks.register("checkSharedPreferencesIsolation") {
    group = "verification"
    description = "Fail build if SharedPreferences is referenced outside :data."

    doLast {
        val forbiddenRefs = listOf(
            "SharedPreferences",
            "getSharedPreferences",
            "PreferenceManager"
        )
        val violations = mutableListOf<String>()

        rootProject.subprojects
            .filter { it.path != ":data" }
            .forEach { project ->
                val sources = project.fileTree(project.projectDir) {
                    include("src/**/*.kt")
                    include("src/**/*.kts")
                    include("src/**/*.java")
                    include("src/**/*.xml")
                    exclude("**/build/**")
                }
                sources.forEach { file ->
                    if (!file.isFile) return@forEach
                    val content = file.readText()
                    if (forbiddenRefs.any { content.contains(it) }) {
                        val matches = forbiddenRefs.filter { content.contains(it) }
                        violations.add("${project.path}: ${file.relativeTo(project.projectDir)} -> ${matches.joinToString(", ")}")
                    }
                }
            }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "SharedPreferences usage must be confined to :data. Violations:\n" +
                    violations.joinToString("\n")
            )
        }
    }
}

tasks.register("checkResourceOwnershipBoundaries") {
    group = "verification"
    description = "Fail build if app and feature-settings declare duplicate resource file paths."

    doLast {
        val appResDir = project(":app").file("src/main/res")
        val settingsResDir = project(":feature-settings").file("src/main/res")

        val duplicates = collectRelativeResourcePaths(appResDir)
            .intersect(collectRelativeResourcePaths(settingsResDir))
            .sorted()

        if (duplicates.isNotEmpty()) {
            throw GradleException(
                "Duplicate resource paths detected between :app and :feature-settings:\n" +
                    duplicates.joinToString("\n") { "- $it" }
            )
        }
    }
}

tasks.register("checkResourceSymbolBoundaries") {
    group = "verification"
    description = "Fail build if app and feature-settings declare duplicate resource symbols."

    doLast {
        val appResDir = project(":app").file("src/main/res")
        val settingsResDir = project(":feature-settings").file("src/main/res")

        val duplicates = collectResourceSymbols(appResDir)
            .intersect(collectResourceSymbols(settingsResDir))
            .sorted()

        if (duplicates.isNotEmpty()) {
            throw GradleException(
                "Duplicate resource symbols detected between :app and :feature-settings:\n" +
                    duplicates.joinToString("\n") { "- $it" }
            )
        }
    }
}

tasks.register("checkAppResourceReferenceBoundaries") {
    group = "verification"
    description = "Fail build if :app uses :feature-settings resources via unqualified R.* references."

    doLast {
        val appResDir = project(":app").file("src/main/res")
        val settingsResDir = project(":feature-settings").file("src/main/res")

        val settingsOnlySymbols = collectResourceSymbols(settingsResDir) - collectResourceSymbols(appResDir)
        if (settingsOnlySymbols.isEmpty()) return@doLast

        val sourceRoots = listOf(
            project(":app").file("src/main/java"),
            project(":app").file("src/main/kotlin")
        ).filter { it.exists() }

        val refPattern = Regex(
            """\bR\.(anim|array|attr|bool|color|dimen|drawable|font|id|integer|layout|menu|mipmap|plurals|raw|string|style)\.([A-Za-z0-9_]+)"""
        )
        val violations = mutableListOf<String>()

        sourceRoots.forEach { root ->
            root.walkTopDown()
                .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
                .forEach { file ->
                    file.useLines { lines ->
                        lines.forEachIndexed { index, line ->
                            refPattern.findAll(line).forEach { match ->
                                val symbol = "${match.groupValues[1]}/${match.groupValues[2]}"
                                if (symbol in settingsOnlySymbols) {
                                    val rel = file.relativeTo(project(":app").projectDir).invariantSeparatorsPath
                                    violations.add(
                                        ":app/$rel:${index + 1} -> R.${match.groupValues[1]}.${match.groupValues[2]} " +
                                            "is owned by :feature-settings; use feature-settings R alias."
                                    )
                                }
                            }
                        }
                    }
                }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "Transitive resource references from :app to :feature-settings detected:\n" +
                    violations.joinToString("\n")
            )
        }
    }
}

tasks.register("checkNoFeatureSettingsRUsageInApp") {
    group = "verification"
    description = "Fail build if :app directly imports or references :feature-settings R."

    doLast {
        val sourceRoots = listOf(
            project(":app").file("src/main/java"),
            project(":app").file("src/main/kotlin")
        ).filter { it.exists() }

        val violations = mutableListOf<String>()
        val forbiddenImport = Regex("""import\s+com\.bokehforu\.openflip\.feature\.settings\.R(\s+as\s+\w+)?""")
        val forbiddenQualifiedRef = Regex("""com\.bokehforu\.openflip\.feature\.settings\.R\.""")

        sourceRoots.forEach { root ->
            root.walkTopDown()
                .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
                .forEach { file ->
                    file.useLines { lines ->
                        lines.forEachIndexed { index, line ->
                            if (forbiddenImport.containsMatchIn(line) || forbiddenQualifiedRef.containsMatchIn(line)) {
                                val rel = file.relativeTo(project(":app").projectDir).invariantSeparatorsPath
                                violations.add(":app/$rel:${index + 1} -> $line")
                            }
                        }
                    }
                }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "Direct :feature-settings R usage detected in :app:\n" +
                    violations.joinToString("\n")
            )
        }
    }
}

subprojects {
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(rootProject.tasks.named("checkModuleBoundaries"))
        dependsOn(rootProject.tasks.named("checkSharedPreferencesIsolation"))
        dependsOn(rootProject.tasks.named("checkResourceOwnershipBoundaries"))
        dependsOn(rootProject.tasks.named("checkResourceSymbolBoundaries"))
        dependsOn(rootProject.tasks.named("checkAppResourceReferenceBoundaries"))
        dependsOn(rootProject.tasks.named("checkNoFeatureSettingsRUsageInApp"))
    }
}
