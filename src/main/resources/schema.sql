CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS url (
    id BIGSERIAL PRIMARY KEY,
    full_url TEXT NOT NULL UNIQUE,
    code CHAR(8) NOT NULL UNIQUE,
    string VARCHAR(100) UNIQUE,
    creation_date DATE NOT NULL,
    expiration_date DATE NOT NULL,
    user_id BIGINT REFERENCES app_user(id)
);
