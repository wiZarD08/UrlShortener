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
    user_id BIGINT REFERENCES app_user(id)
);

CREATE TABLE IF NOT EXISTS utm_tag (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(30) NOT NULL,
    medium VARCHAR(30) NOT NULL,
    campaign VARCHAR(30) NOT NULL,
    content VARCHAR(30),
    clicks BIGINT DEFAULT 0,
    url_id BIGINT REFERENCES url(id)
);

--CREATE TABLE IF NOT EXISTS utm_stat (
--    clicks BIGINT DEFAULT 0,
--    url_id BIGINT REFERENCES url(id),
--    utm_tag_id BIGINT REFERENCES utm_tag(id)
--);
