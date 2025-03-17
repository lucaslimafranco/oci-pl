pipeline {
    agent any

    parameters {
        string(name: 'ENV_NAME', defaultValue: 'dev', description: 'Nome do ambiente')
        password(name: 'MYSQL_PASSWORD', defaultValue: '', description: 'Senha do MySQL')
        string(name: 'MYSQL_PORT', defaultValue: '3306', description: 'Porta do MySQL')
    }

    stages {
        stage('Validar Parâmetros') {
            steps {
                script {
                    // Valida se a porta é um número válido
                    if (!params.MYSQL_PORT.isNumber() || params.MYSQL_PORT.toInteger() < 1 || params.MYSQL_PORT.toInteger() > 65535) {
                        error("Porta inválida. Deve ser um número entre 1 e 65535.")
                    }
                }
            }
        }

        stage('Construir Imagem Docker') {
            steps {
                script {
                    bat "docker build -t mysql-${params.ENV_NAME} ."
                }
            }
        }

        stage('Executar Container') {
            steps {
                script {
                    // Remove o container existente, se houver
                    bat "docker rm -f mysql-${params.ENV_NAME} || true"

                    // Cria um novo container
                    bat """
                        docker run -d \
                        --name mysql-${params.ENV_NAME} \
                        -e MYSQL_ROOT_PASSWORD=${params.MYSQL_PASSWORD} \
                        -p ${params.MYSQL_PORT}:3306 \
                        mysql-${params.ENV_NAME}
                    """
                }
            }
        }

        stage('Configurar Banco de Dados') {
            steps {
                script {
                    // Aguarda o MySQL estar pronto (60 segundos)
                    bat "ping 127.0.0.1 -n 61 > nul"

                    // Cria o banco de dados e a tabela
                    bat """
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
            echo "Pipeline concluída com sucesso!"
        }
        failure {
            echo "Pipeline falhou. Verifique os logs para mais detalhes."
        }
    }
}
