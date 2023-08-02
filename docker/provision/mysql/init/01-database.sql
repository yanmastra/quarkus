# create databases
CREATE DATABASE IF NOT EXISTS `db_crud_reactive`;
CREATE DATABASE IF NOT EXISTS `db_kafka_example`;

# create root user and grant rights
CREATE USER 'developer'@'localhost' IDENTIFIED BY 'crud_reactive_hibernate';
GRANT ALL PRIVILEGES ON *.* TO 'developer'@'%';