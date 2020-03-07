final String CI_NAME = "PretronicCI"
final String CI_EMAIL = "ci@pretronic.net"
final String COMMIT_MESSAGE = "Version change %version%"
final String RESOURCE_ID = "e5b65750-4dcc-4631-b275-06113b31a416"

final String BRANCH_DEVELOPMENT = "origin/development"
final String BRANCH_MASTER = "origin/master"
final String PROJECT_SSH = "git@github.com:DevKrieger/McNative.git"

String PROJECT_NAME = "McNative"
String VERSION = "UNDEFINED"
String BRANCH = "UNDEFINED"
boolean SKIP = false



pipeline {
    agent any
    tools {
        maven 'Maven3'
        jdk 'Java8'
    }
    options {
        buildDiscarder logRotator(numToKeepStr: '10')
    }
    stages {
        stage('CI Check') {
            steps {
                script {
                    String name = sh script: 'git log -1 --pretty=format:\"%an\"', returnStdout: true
                    String email = sh script: 'git log -1 --pretty=format:\"%ae\"', returnStdout: true
                    if (name == CI_NAME && email == CI_EMAIL) {
                        SKIP = true;
                    }
                }
            }
        }
        stage('Read information') {
            when { equals expected: false, actual: SKIP }
            steps {
                script {
                    VERSION = readMavenPom().getVersion()
                    BRANCH = env.GIT_BRANCH
                }
            }
        }
        stage('Version change') {
            when { equals expected: false, actual: SKIP }
            steps {
                script {
                    String[] versionSplit = VERSION.split("[-.]")

                    String major = versionSplit[0]
                    int minorVersion = versionSplit[1].toInteger()
                    int patchVersion = versionSplit[2].toInteger()

                    if(BRANCH.equalsIgnoreCase(BRANCH_MASTER)) {
                        VERSION = major + "." + minorVersion + "." + patchVersion
                    } else if (BRANCH.equalsIgnoreCase(BRANCH_DEVELOPMENT)) {
                        if (!VERSION.endsWith("-SNAPSHOT")) {
                            VERSION = VERSION + '-SNAPSHOT'
                        }
                    }
                    sh "mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$VERSION"
                    echo "mcnative-bungeecord/target/mcnative-bungeecord-${VERSION}.jar"
                }
            }
        }
        stage('Build') {
            when { equals expected: false, actual: SKIP }
            steps {
                sh 'mvn -B clean install'
            }
        }
        stage('Deploy') {
            when { equals expected: false, actual: SKIP }
            steps {
                configFileProvider([configFile(fileId: 'afe25550-309e-40c1-80ad-59da7989fb4e', variable: 'MAVEN_GLOBAL_SETTINGS')]) {
                    sh 'mvn -gs $MAVEN_GLOBAL_SETTINGS deploy'
                }
            }
        }
        stage('Archive') {
            when { equals expected: false, actual: SKIP }
            steps {
                archiveArtifacts artifacts: '**/target/*.jar'
            }
        }
    }
    post {
        success {
            script {
                if(!SKIP) {
                    sh """
                    git config --global user.name '$CI_NAME' -v
                    git config --global user.email '$CI_EMAIL' -v
                    """

                    String[] versionSplit = VERSION.split("[-.]")

                    String major = versionSplit[0]
                    int minorVersion = versionSplit[1].toInteger()
                    int patchVersion = versionSplit[2].toInteger()

                    if (BRANCH == BRANCH_DEVELOPMENT) {
                        patchVersion++

                        String version = major + "." + minorVersion + "." + patchVersion + "-SNAPSHOT"
                        String commitMessage = COMMIT_MESSAGE.replace("%version%", version)
                        sh """
                        mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$version
                        git add . -v
                        git commit -m '$commitMessage' -v
                        """

                        sshagent(['1c1bd183-26c9-48aa-94ab-3fe4f0bb39ae']) {
                            sh "git push origin HEAD:development -v"
                        }
                    } else if (BRANCH == BRANCH_MASTER) {
                        minorVersion++
                        patchVersion = 0
                        String version = major + "." + minorVersion + "." + patchVersion

                        String commitMessage = COMMIT_MESSAGE.replace("%version%", VERSION)
                        sshagent(['1c1bd183-26c9-48aa-94ab-3fe4f0bb39ae']) {

                            sh """
                            mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$VERSION
                            git add . -v
                            git commit -m 'Jenkins version change $VERSION' -v
                            git push origin HEAD:master -v
                            """
                            commitMessage = COMMIT_MESSAGE.replace("%version%", version)
                            sh """
                            if [ -d "tempDevelopment" ]; then rm -Rf tempDevelopment; fi
                            mkdir tempDevelopment
                            cd tempDevelopment/
                            git clone --single-branch --branch development $PROJECT_SSH

                            cd $PROJECT_NAME/
                            mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$version-SNAPSHOT

                            git add . -v
                            git commit -m '$commitMessage-SNAPSHOT' -v
                            git push origin HEAD:development -v
                            cd ..
                            cd ..
                            if [ -d "tempDevelopment" ]; then rm -Rf tempDevelopment; fi

                            """
                        }
                    }

                    int buildNumber = env.BUILD_NUMBER;
                    httpRequest(acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON',
                        httpMode: 'POST', ignoreSslErrors: true,timeout: 3000,
                        responseHandle: 'NONE',
                        url: "https://mirror.prematic.net/v1/$RESOURCE_ID/versions/create?name=$VERSION&qualifier=SNAPSHOT&buildNumber=$buildNumber");


                    httpRequest(acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_OCTETSTREAM',
                         httpMode: 'POST', ignoreSslErrors: true, timeout: 3000,
                         multipartName: 'file',
                         responseHandle: 'NONE',
                         uploadFile: "mcnative-bungeecord/target/mcnative-bungeecord-${VERSION}.jar",
                         url: "https://mirror.prematic.net/v1/$RESOURCE_ID/versions/$buildNumber/publish?edition=bungeecord")
                }
            }
        }
    }
}

