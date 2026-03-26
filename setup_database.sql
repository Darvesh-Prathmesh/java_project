CREATE DATABASE IF NOT EXISTS event_management_db;
USE event_management_db;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS organizations (
    user_id INT PRIMARY KEY,
    organization_name VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS participants (
    user_id INT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS volunteers (
    user_id INT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    hours_logged INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS events (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    org_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    event_date TIMESTAMP NOT NULL,
    location VARCHAR(255) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    max_capacity INT DEFAULT 100,
    status VARCHAR(50) DEFAULT 'PUBLISHED',
    FOREIGN KEY (org_id) REFERENCES organizations(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS concerts (
    event_id INT PRIMARY KEY,
    band_name VARCHAR(255),
    genre VARCHAR(100),
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS conferences (
    event_id INT PRIMARY KEY,
    keynote_speaker VARCHAR(255),
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS weddings (
    event_id INT PRIMARY KEY,
    couple_names VARCHAR(255),
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS registrations (
    registration_id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    registration_role VARCHAR(50) NOT NULL,
    application_text TEXT,
    status VARCHAR(50) DEFAULT 'ACCEPTED',
    ticket_number VARCHAR(50),
    UNIQUE KEY (event_id, user_id),
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
