pipeline {
    agent any

    triggers {
        // Poll Git for changes every 5 minutes
        pollSCM('H/5 * * * *')
    }

    options {
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Deploy via Ansible') {
            steps {
                sshagent(credentials: ['ansible-deploy-key']) {
                    sh '''
                        cd ansible
                        ansible-playbook -i inventory.ci.ini playbook.yml
                    '''
                }
            }
        }
    }

    post {
        failure {
            emailext(
                to: 'srengty@gmail.com',
                recipientProviders: [culprits(), requestor()],
                subject: "Build FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """Build failed.

Job: ${env.JOB_NAME}
Build: #${env.BUILD_NUMBER}
URL: ${env.BUILD_URL}console

Check the console output for details.
"""
            )
        }
    }
}
