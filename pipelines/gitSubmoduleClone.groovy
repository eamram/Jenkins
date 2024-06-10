pipeline {
    agent { label 'node_label' }
    environment {
		branch = 'master'
        gitUrl = 'gitlab.com/your/gitlab_path.git'
        gitUrlHttp = 'http://gitlab.com/your/gitlab_path.git'
        gitCredentialsId = '12345-12345-12345-12345-12345' // GitLab access token
        gitSubCredentialsId = '09876-09876-09876-09876-09876' // Gitlab guest user
    }
    stages {
        stage ('Cleanup') {
            steps {
                echo 'Running a Clean build'
                cleanWs()
            }
        }
        stage('Clone Repository with git clone') {
            steps {
                script {
                    // Clean workspace
                    deleteDir()

                    // Clone repository using bat command with access token
                    withCredentials([usernamePassword(credentialsId: env.gitCredentialsId, usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
                        bat """
                            git clone --quiet --branch ${branch} --single-branch http://${GIT_USERNAME}:${GIT_PASSWORD}@${gitUrl} . > nul
                        """
                    }
                }
            }
        }
        stage('Clone Repository using plugin') {
            steps {
                script {
                    // Clean workspace
                    deleteDir()

                    // Clone repository using Jenkins Git plugin
                    git credentialsId: env.gitCredentialsId, url: env.gitUrlHttp, branch: env.branch
                }
            }
        }
        stage('Clone Repository with Submodules') {
            steps {
                script {
                    // Clone the main repository and update submodules using Jenkins Git plugin
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: env.branch]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [
                            [$class: 'SubmoduleOption', recursiveSubmodules: true, trackingSubmodules: true]
                        ],
                        submoduleCfg: [],
                        userRemoteConfigs: [[
                            url: env.gitUrlHttp,
                            credentialsId: env.gitSubCredentialsId
                        ]]
                    ])
                }
                bat'''
                dir your/git/submodule/path
                '''
            }
        }
    }
}
