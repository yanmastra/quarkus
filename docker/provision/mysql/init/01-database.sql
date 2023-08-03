# create databases
CREATE DATABASE IF NOT EXISTS `db_ql_crud_reactive`;
CREATE DATABASE IF NOT EXISTS `db_ql_kafka_reactive`;

# create root user and grant rights
CREATE USER 'developer'@'localhost' IDENTIFIED BY 'local';
GRANT ALL PRIVILEGES ON db_ql_crud_reactive.* TO 'developer'@'%';
GRANT ALL PRIVILEGES ON db_ql_kafka_reactive.* TO 'developer'@'%';