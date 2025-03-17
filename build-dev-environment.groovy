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
                    if (!params.MYSQL_PORT.isInteger() || params.MYSQL_PORT.toInteger() < 1 || params.MYSQL_PORT.toInteger() > 65535) {
                        error("Porta inválida. Deve ser um número entre 1 e 65535.")
                    }
                }
            }
        }

        stage('Remover Container Antigo (se existir)') {
            steps {
                script {
                    bat '''
                    docker rm -f mysql-%ENV_NAME% || exit /b 0
                    '''
                }
            }
        }

        stage('Construir Imagem Docker') {
            steps {
                script {
                    bat "docker build -t mysql-%ENV_NAME% ."
                }
            }
        }

        stage('Executar Container') {
            steps {
                script {
                    bat """
                        docker run -d ^
                        --name mysql-%ENV_NAME% ^
                        -e MYSQL_ROOT_PASSWORD=%MYSQL_PASSWORD% ^
                        -p %MYSQL_PORT%:3306 ^
                        mysql-%ENV_NAME%
                    """
                }
            }
        }

        stage('Configurar Banco de Dados') {
            steps {
                script {
                    // Aguarda o MySQL estar pronto (30 segundos)
                    bat "timeout /T 30 /NOBREAK >nul"

                    // Cria o banco de dados e a tabela
                    bat """
                        docker exec mysql-%ENV_NAME% mysql -uroot -p%MYSQL_PASSWORD% -e " ^
                            CREATE DATABASE IF NOT EXISTS DEVAPP; ^
                            USE DEVAPP; ^
                            CREATE TABLE IF NOT EXISTS departments ( ^
                                DEPT INT(4) PRIMARY KEY, ^
                                DEPT_NAME VARCHAR(250) ^
                            ); ^
                            INSERT INTO departments (DEPT, DEPT_NAME) VALUES (1001, 'Sales'), (1002, 'Engineering');"
                    """
                }
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline concluída com sucesso!"
        }
        failure {
            echo "❌ Pipeline falhou. Verifique os logs para mais
