import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.projectFeatures.buildReportTab
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2025.11"

project {
    description = "Contains all other projects"

    features {
        buildReportTab {
            id = "PROJECT_EXT_1"
            title = "Code Coverage"
            startPage = "coverage.zip!index.html"
        }
    }

    cleanup {
        baseRule {
            preventDependencyCleanup = false
        }
    }

    subProject(UnityBsp)
    subProject(UnityBsp_2)
}


object UnityBsp : Project({
    name = "Unity BSP"

    vcsRoot(UnityBsp_HttpsGithubComMadScientist11UnityBspGitBranch)

    buildType(UnityBsp_BuildCounter)
    buildType(UnityBsp_BuildIOS)

    template(UnityBsp_BuildTemplate)
})

object UnityBsp_BuildCounter : BuildType({
    name = "Build Counter"

    features {
        perfmon {
        }
    }
})

object UnityBsp_BuildIOS : BuildType({
    templates(UnityBsp_BuildTemplate)
    name = "Build iOS"

    features {
        perfmon {
            id = "perfmon"
        }
    }
})

object UnityBsp_BuildTemplate : Template({
    name = "Build Template"

    params {
        param("args", "")
        param("git.branch", "master")
        param("ios.app.specific.password", "243242")
        param("target", "iOS")
    }

    vcs {
        root(UnityBsp_HttpsGithubComMadScientist11UnityBspGitBranch)
    }

    steps {
        python {
            name = "Clear artifacts"
            id = "Clear_artifacts"
            command = script {
                content = """
                    import shutil
                    import os
                    import glob
                    
                    if os.path.exists("./Builds"):
                    	shutil.rmtree("./Builds")
                    os.makedirs("./Builds")
                    
                    for f in glob.glob("./CI/*.log"):
                      os.remove(f)
                """.trimIndent()
            }
        }
        script {
            name = "Get Unity Version"
            id = "Get_Unity_Version"
            scriptContent = """
                UNITY_VERSION=${'$'}(awk '/m_EditorVersion:/ {print ${'$'}2}' \
                  ProjectSettings/ProjectVersion.txt)
                
                if [ -z "${'$'}UNITY_VERSION" ]; then
                  echo "ERROR: Failed to determine Unity version from ProjectSettings/ProjectVersion.txt" >&2
                  exit 1
                fi
                
                UNITY_PATH=${'$'}{UNITY_LOCATION/VERSION/${'$'}UNITY_VERSION}
                
                if [ ! -f "${'$'}UNITY_PATH" ]; then
                  echo "ERROR: Unity executable not found: ${'$'}UNITY_PATH" >&2
                  exit 1
                fi
                
                echo "##teamcity[setParameter name='env.UNITY_VERSION' value='${'$'}UNITY_VERSION']"
                echo "##teamcity[setParameter name='env.UNITY_PATH' value='${'$'}UNITY_PATH']"
            """.trimIndent()
        }
        python {
            name = "Build"
            id = "Build"
            workingDir = "CI"
            command = file {
                filename = "build.py"
                scriptArguments = """--editor "${'$'}UNITY_PATH" --project "%teamcity.build.checkoutDir%" --target %target% --unity-args "--bundleversion=%build.number%" %args%"""
            }
        }
        python {
            name = "Deploy"
            id = "Deploy"
            workingDir = "CI/deploy"
            command = script {
                content = "sh %target%.sh %system.teamcity.build.checkoutDir% %ios.app.specific.password%"
            }
        }
    }

    dependencies {
        snapshot(UnityBsp_BuildCounter) {
            reuseBuilds = ReuseBuilds.NO
        }
    }
})

object UnityBsp_HttpsGithubComMadScientist11UnityBspGitBranch : GitVcsRoot({
    name = "https://github.com/MadScientist11/Unity-BSP#%git.branch%"
    url = "git@github.com:MadScientist11/Unity-BSP.git"
    branch = "%git.branch%"
    branchSpec = "refs/heads/*"
    authMethod = uploadedKey {
        uploadedKey = "alexmadgit"
    }
})


object UnityBsp_2 : Project({
    name = "Unity BSP (1)"

    vcsRoot(UnityBsp_2_HttpsGithubComMadScientist11UnityBspGitBranch)

    buildType(UnityBsp_2_BuildCounter)
    buildType(UnityBsp_2_BuildIOS)

    template(UnityBsp_2_BuildTemplate)
})

object UnityBsp_2_BuildCounter : BuildType({
    name = "Build Counter"

    features {
        perfmon {
        }
    }
})

object UnityBsp_2_BuildIOS : BuildType({
    templates(UnityBsp_2_BuildTemplate)
    name = "Build iOS"

    features {
        perfmon {
            id = "perfmon"
        }
    }
})

object UnityBsp_2_BuildTemplate : Template({
    name = "Build Template"

    params {
        param("args", "")
        param("git.branch", "master")
        param("ios.app.specific.password", "243242")
        param("target", "iOS")
    }

    vcs {
        root(UnityBsp_2_HttpsGithubComMadScientist11UnityBspGitBranch)
    }

    steps {
        python {
            name = "Clear artifacts"
            id = "Clear_artifacts"
            command = script {
                content = """
                    import shutil
                    import os
                    import glob
                    
                    if os.path.exists("./Builds"):
                    	shutil.rmtree("./Builds")
                    os.makedirs("./Builds")
                    
                    for f in glob.glob("./CI/*.log"):
                      os.remove(f)
                """.trimIndent()
            }
        }
        script {
            name = "Get Unity Version"
            id = "Get_Unity_Version"
            scriptContent = """
                UNITY_VERSION=${'$'}(awk '/m_EditorVersion:/ {print ${'$'}2}' \
                  ProjectSettings/ProjectVersion.txt)
                
                if [ -z "${'$'}UNITY_VERSION" ]; then
                  echo "ERROR: Failed to determine Unity version from ProjectSettings/ProjectVersion.txt" >&2
                  exit 1
                fi
                
                UNITY_PATH=${'$'}{UNITY_LOCATION/VERSION/${'$'}UNITY_VERSION}
                
                if [ ! -f "${'$'}UNITY_PATH" ]; then
                  echo "ERROR: Unity executable not found: ${'$'}UNITY_PATH" >&2
                  exit 1
                fi
                
                echo "##teamcity[setParameter name='env.UNITY_VERSION' value='${'$'}UNITY_VERSION']"
                echo "##teamcity[setParameter name='env.UNITY_PATH' value='${'$'}UNITY_PATH']"
            """.trimIndent()
        }
        python {
            name = "Build"
            id = "Build"
            workingDir = "CI"
            command = file {
                filename = "build.py"
                scriptArguments = """--editor "${'$'}UNITY_PATH" --project "%teamcity.build.checkoutDir%" --target %target% --unity-args "--bundleversion=%build.number%" %args%"""
            }
        }
        python {
            name = "Deploy"
            id = "Deploy"
            workingDir = "CI/deploy"
            command = script {
                content = "sh %target%.sh %system.teamcity.build.checkoutDir% %ios.app.specific.password%"
            }
        }
    }

    dependencies {
        snapshot(UnityBsp_2_BuildCounter) {
            reuseBuilds = ReuseBuilds.NO
        }
    }
})

object UnityBsp_2_HttpsGithubComMadScientist11UnityBspGitBranch : GitVcsRoot({
    name = "https://github.com/MadScientist11/Unity-BSP#%git.branch%"
    url = "git@github.com:MadScientist11/Unity-BSP.git"
    branch = "%git.branch%"
    branchSpec = "refs/heads/*"
    authMethod = uploadedKey {
        uploadedKey = "alexmadgit"
    }
})
