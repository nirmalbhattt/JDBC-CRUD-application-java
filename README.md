# JDBC-CRUD-application-java

Employee Database App (Java + MySQL)

This is a console-based Java application that performs CRUD (Create, Read, Update, Delete) operations on an employees table stored in a MySQL database. JDBC (Java Database Connectivity) is used to connect Java with MySQL.


---

Tools and Technologies Used

Java (Core + JDBC)

MySQL Database

MySQL Connector/J (JDBC Driver)

---

Database Setup

Run the following SQL commands before running the program:

CREATE DATABASE IF NOT EXISTS employee_db;
USE employee_db;

CREATE TABLE IF NOT EXISTS employees (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100),
  salary DOUBLE,
  department VARCHAR(100)
);

---

How to Run

1. Download the MySQL JDBC driver (mysql-connector-java-x.x.x.jar).


2. Compile the Java file with the connector in the classpath.


3. Run the program and follow the menu options to add, view, update, or delete employees.

---

Features

Add Employee

View All Employees

Update Employee by ID

Delete Employee by ID
