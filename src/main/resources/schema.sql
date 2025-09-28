CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password CHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS url (
    id BIGSERIAL PRIMARY KEY,
    full_url TEXT NOT NULL,
    code CHAR(8) NOT NULL UNIQUE,
    string VARCHAR(100) UNIQUE,
    creation_date DATE NOT NULL,
    expiration_date DATE NOT NULL,
    utm_support BOOLEAN DEFAULT false,
    clicks BIGINT DEFAULT 0,
    unique_clicks BIGINT DEFAULT 0,
    user_id BIGINT REFERENCES app_user(id)
);

CREATE TABLE IF NOT EXISTS utm_tag (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(100) NOT NULL,
    medium VARCHAR(100) NOT NULL,
    campaign VARCHAR(100) NOT NULL,
    content VARCHAR(100),
    clicks BIGINT DEFAULT 0,
    url_id BIGINT REFERENCES url(id)
);

CREATE TABLE IF NOT EXISTS statistics (
    id BIGSERIAL PRIMARY KEY,
    ip_address VARCHAR(45) NOT NULL,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    device VARCHAR(30) NOT NULL,
    agent VARCHAR(30) NOT NULL,
    os VARCHAR(30) NOT NULL,
    url_id BIGINT REFERENCES url(id)
);

CREATE TABLE IF NOT EXISTS click_time (
    id BIGSERIAL PRIMARY KEY,
    date_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    url_id BIGINT REFERENCES url(id)
);
