FROM mysql:latest

# Copy the user creation script to container
COPY include/create_developer.template /docker-entrypoint-initdb.d/create_developer.sql

# MySQL port
EXPOSE 3306