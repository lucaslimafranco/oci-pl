pipeline {
    agent any

    parameters {
        string(name: 'ENV_NAME', defaultValue: 'dev', description: 'ENV_NAME')
        password(name: 'MYSQL_PASSWORD', defaultValue: '', description: 'MySQL Password')
        string(name: 'MYSQL_PORT', defaultValue: '3306', description: 'MySQL Port')
    }

    stages {
        stage('Validar Parâmetros') {
            steps {
                script {
                    // Valid if the port number it's correct
                    if (!params.MYSQL_PORT.isNumber() || params.MYSQL_PORT.toInteger() < 1 || params.MYSQL_PORT.toInteger() > 65535) {
                        error("Invalid port. should be a number 1 until 65535.")
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t mysql-${params.ENV_NAME} ."
                }
            }
        }

        stage('Execute Container') {
            steps {
                script {
                    sh """
                        docker run -d \
                        --name mysql-${params.ENV_NAME} \
                        -e MYSQL_ROOT_PASSWORD=${params.MYSQL_PASSWORD} \
                        -p ${params.MYSQL_PORT}:3306 \
                        mysql-${params.ENV_NAME}
                    """
                }
            }
        }

        stage('Config DB') {
            steps {
                script {
                    // Wait MySQL be ready
                    sh "sleep 30"

                    // Create DB and table
                    sh """
                        docker exec mysql-${params.ENV_NAME} mysql -uroot -p${params.MYSQL_PASSWORD} -e "
                            CREATE DATABASE DEVAPP;
                            USE DEVAPP;
                            CREATE TABLE departments (
                                DEPT INT(4) PRIMARY KEY,
                                DEPT_NAME VARCHAR(250)
                            );
                            INSERT INTO departments (DEPT, DEPT_NAME) VALUES (1001, 'Sales'), (1002, 'Engineering');
                        "
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline was concluded with success!"
        }
        failure {
            echo "Pipeline falied. Check the logs for more details."
        }
    }
}