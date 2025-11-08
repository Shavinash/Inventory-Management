CREATE DATABASE IF NOT EXISTS ims_db;
USE ims_db;

CREATE TABLE IF NOT EXISTS inventory (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100),
    sku VARCHAR(50) UNIQUE,
    category VARCHAR(50),
    quantity INT,
    supplier VARCHAR(100),
    price DOUBLE,
    location VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS audit (
    id INT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(255),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);