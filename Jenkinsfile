pipeline {

    agent any

    tools {
        jdk 'jdk-25'
        maven 'maven-3.9'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 45, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '10'))
    }

    parameters {
        booleanParam(
            name: 'PUBLISH_TO_SVN',
            defaultValue: true,
            description: 'Calisir paketi SVN current dizinine gonder.'
        )
        booleanParam(
            name: 'TAG_IN_SVN',
            defaultValue: false,
            description: 'SVN tags altinda bu build icin kalici kopya olustur. Paket buyuk oldugu icin yalnizca surum cikislarinda isaretleyin.'
        )
        booleanParam(
            name: 'RUN_INTEGRATION_TESTS',
            defaultValue: false,
            description: 'Testcontainers entegrasyon testlerini calistir. Agent uzerinde Docker erisimi gerektirir.'
        )
    }

    environment {
        MAVEN_OPTS = '-Xmx1536m -Djava.awt.headless=true'
        MVN_FLAGS = '-B -ntp -Dstyle.color=never'
        SVN_BASE_URL = 'https://svn.sirket.local/svn/aft-api/releases'
        SVN_CREDENTIALS_ID = 'aft-svn'
        RELEASE_BRANCH = 'master'
    }

    stages {

        stage('Hazirlik') {
            steps {
                script {
                    env.APP_VERSION = sh(
                        script: "mvn ${MVN_FLAGS} -q help:evaluate -Dexpression=project.version -DforceStdout",
                        returnStdout: true
                    ).trim()
                    currentBuild.displayName = "#${env.BUILD_NUMBER} - ${env.APP_VERSION}"
                }
                sh 'java -version'
                sh "mvn ${MVN_FLAGS} -version"
            }
        }

        stage('Derle') {
            steps {
                sh "mvn ${MVN_FLAGS} clean compile"
            }
        }

        stage('Birim Testleri') {
            steps {
                sh "mvn ${MVN_FLAGS} test"
            }
            post {
                always {
                    junit allowEmptyResults: false, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Entegrasyon Testleri') {
            when {
                expression { return params.RUN_INTEGRATION_TESTS }
            }
            steps {
                sh "mvn ${MVN_FLAGS} test -Pintegration-tests"
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Paketle') {
            steps {
                sh "mvn ${MVN_FLAGS} package -DskipTests"
            }
        }

        stage('Yayin Paketi') {
            steps {
                sh "bash ci/package-release.sh '${env.APP_VERSION}' '${env.BUILD_NUMBER}'"
                sh 'cat target/release/*/BUILD-INFO.txt'
            }
            post {
                success {
                    archiveArtifacts(
                        artifacts: 'target/release/*/BUILD-INFO.txt, target/release/*/SHA256SUMS',
                        fingerprint: true
                    )
                }
            }
        }

        stage('SVN Yayini') {
            when {
                allOf {
                    expression { return params.PUBLISH_TO_SVN }
                    expression {
                        def current = (env.BRANCH_NAME ?: env.GIT_BRANCH ?: '').replaceFirst(/^origin\//, '')
                        return current.isEmpty() || current == env.RELEASE_BRANCH
                    }
                }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: env.SVN_CREDENTIALS_ID,
                    usernameVariable: 'SVN_USERNAME',
                    passwordVariable: 'SVN_PASSWORD'
                )]) {
                    withEnv([
                        "SVN_URL=${env.SVN_BASE_URL}",
                        "SVN_TAG=${params.TAG_IN_SVN ? '1' : '0'}"
                    ]) {
                        sh 'bash ci/publish-svn.sh'
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Basarili: ${currentBuild.displayName}"
        }
        unstable {
            echo "Testler kirmizi: ${currentBuild.displayName}"
        }
        failure {
            echo "Build basarisiz: ${currentBuild.displayName}"
        }
        cleanup {
            cleanWs(deleteDirs: true, notFailBuild: true)
        }
    }
}
