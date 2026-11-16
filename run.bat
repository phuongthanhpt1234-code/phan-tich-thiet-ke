@echo off
cd /d "%~dp0BookStore-Management"
java -cp "target/classes;%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar" com.bookstore.Main
