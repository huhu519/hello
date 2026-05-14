CREATE DATABASE IF NOT EXISTS private_chat DEFAULT CHARACTER SET utf8mb4;
USE private_chat;

CREATE TABLE IF NOT EXISTS user (
    user_id VARCHAR(8) PRIMARY KEY,
    phone VARCHAR(20) UNIQUE NOT NULL,
    password VARCHAR(32) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    create_time DATETIME,
    disabled INT DEFAULT 0,
    storage_used BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS invite_code (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    status INT DEFAULT 1,
    use_count INT DEFAULT 0,
    max_use_count INT DEFAULT 0,
    remark VARCHAR(255),
    create_time DATETIME
);

CREATE TABLE IF NOT EXISTS friend (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(8) NOT NULL,
    friend_id VARCHAR(8) NOT NULL,
    remark VARCHAR(50),
    status INT DEFAULT 0,
    create_time DATETIME
);

CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(50) UNIQUE NOT NULL,
    session_type INT DEFAULT 0,
    storage_used BIGINT DEFAULT 0,
    create_time DATETIME
);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id VARCHAR(8) NOT NULL,
    session_id VARCHAR(50) NOT NULL,
    message_type INT DEFAULT 0,
    content TEXT,
    file_url VARCHAR(255),
    file_size BIGINT DEFAULT 0,
    create_time DATETIME,
    INDEX idx_session (session_id)
);

CREATE TABLE IF NOT EXISTS group_chat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id VARCHAR(50) UNIQUE NOT NULL,
    group_name VARCHAR(100),
    creator_id VARCHAR(8),
    create_time DATETIME
);

CREATE TABLE IF NOT EXISTS group_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(8) NOT NULL,
    join_time DATETIME
);
