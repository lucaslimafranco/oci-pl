# Jenkins Pipeline for MySQL Docker Environment

This project provides a Jenkins pipeline to automatically create a MySQL database instance running in a Docker container. It is designed to help developers quickly set up their own isolated MySQL environments for development and testing purposes.

---

## Overview

The pipeline performs the following tasks:

1. **Builds a Docker image** based on the official MySQL image.
2. **Runs a MySQL container** with the specified environment name, port, and root password.
3. **Configures the MySQL database** by creating a `DEVAPP` database and a `departments` table with sample data.

---

## Prerequisites

Before running the pipeline, ensure you have the following installed:

- **Jenkins**: The pipeline is designed to run on Jenkins. Make sure Jenkins is installed and configured.
- **Docker**: Docker must be installed on the machine where Jenkins is running.
- **Git**: The pipeline pulls the code from a Git repository.

---

## Files in the Project

- **`Dockerfile`**: Defines the custom Docker image based on the official MySQL image.
- **`build-dev-environment.groovy`**: The Jenkins pipeline script that automates the creation of the MySQL environment.
- **`include/create_developer.template`**: A SQL script to create a `developer` user and grant privileges.

---

## How It Works

### 1. Dockerfile
The `Dockerfile` uses the official MySQL image and copies the `create_developer.template` script to the `/docker-entrypoint-initdb.d/` directory. This ensures that the script runs automatically when the container starts.

### 2. Jenkins Pipeline
The pipeline is defined in `build-dev-environment.groovy` and consists of the following stages:

- **Validate Parameters**: Ensures the MySQL port is a valid number.
- **Build Docker Image**: Builds the Docker image using the `Dockerfile`.
- **Run Container**: Starts a MySQL container with the specified parameters.
- **Configure Database**: Creates the `DEVAPP` database and the `departments` table, and inserts sample data.

### 3. SQL Script
The `create_developer.template` script creates a `developer` user with the password `0slo1$$` and grants privileges to the `DEVAPP` database.

---

## How to Use

### 1. Set Up Jenkins
- Install Jenkins and the necessary plugins (e.g., Git, Docker Pipeline).
- Add the MySQL root password as a secret text credential in Jenkins with the ID `mysql-root-password`.

### 2. Clone the Repository
Clone this repository to your local machine or Jenkins workspace:
```bash
git clone https://github.com/lucaslimafranco/oci-pl.git
