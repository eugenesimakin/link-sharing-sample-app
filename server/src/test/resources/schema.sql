CREATE TABLE IF NOT EXISTS users (
    email VARCHAR(255) PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    image_url VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_email) REFERENCES users(email)
);
