pipeline {
    agent any

    parameters {
        string(name: 'ENV_NAME', defaultValue: 'dev', description: 'Environment name')
        string(name: 'MYSQL_PORT', defaultValue: '3306', description: 'MySQL port')
    }

    stages {
        stage('Validate Parameters') {
            steps {
                script {
                    // Validate if the port is a valid number within the range
                    if (!params.MYSQL_PORT.isNumber() || params.MYSQL_PORT.toInteger() < 1 || params.MYSQL_PORT.toInteger() > 65535) {
                        error("Invalid port. Must be a number between 1 and 65535.")
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    // Build the Docker image
                    bat "docker build -t mysql-${params.ENV_NAME} ."
                }
            }
        }

        stage('Run Container') {
            steps {
                script {
                    // Remove the existing container if it exists
                    bat "docker rm -f mysql-${params.ENV_NAME} || true"

                    // Use the secure credential for the MySQL root password
                    withCredentials([string(credentialsId: 'mysql-root-password', variable: 'MYSQL_ROOT_PASSWORD')]) {
                        // Run a new container
                        bat """
                            docker run -d \
                            --name mysql-${params.ENV_NAME} \
                            -e MYSQL_ROOT_PASSWORD=%MYSQL_ROOT_PASSWORD% \
                            -p ${params.MYSQL_PORT}:3306 \
                            mysql-${params.ENV_NAME}
                        """
                    }
                }
            }
        }

        stage('Configure Database') {
            steps {
                script {
                    // Wait for MySQL to be ready (60 seconds)
                    bat "ping 127.0.0.1 -n 61 > nul"

                    // Use the secure credential for the MySQL root password
                    withCredentials([string(credentialsId: 'mysql-root-password', variable: 'MYSQL_ROOT_PASSWORD')]) {
                        // Create the database and table
                        bat """
                            docker exec mysql-${params.ENV_NAME} mysql -uroot -p%MYSQL_ROOT_PASSWORD% -e "
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
    }

    post {
        success {
            echo "Pipeline completed successfully!"
        }
        failure {
            echo "Pipeline failed. Check the logs for more details."
        }
    }
}
