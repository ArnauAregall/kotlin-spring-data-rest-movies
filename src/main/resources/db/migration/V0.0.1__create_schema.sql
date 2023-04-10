SET SEARCH_PATH TO "kotlin-spring-data-rest-movies";

CREATE TABLE IF NOT EXISTS "actor" (
    id BIGSERIAL UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    death_date DATE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "character" (
    id BIGSERIAL UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "director" (
    id BIGSERIAL UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "movie" (
    id BIGSERIAL UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    release_date DATE NOT NULL,
    director_id BIGSERIAL NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (director_id) REFERENCES director(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "movie_character" (
    movie_id BIGSERIAL NOT NULL,
    character_id BIGSERIAL NOT NULL,
    PRIMARY KEY (movie_id, character_id),
    UNIQUE (movie_id, character_id),
    FOREIGN KEY (movie_id) REFERENCES movie(id) ON DELETE CASCADE,
    FOREIGN KEY (character_id) REFERENCES character(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "actor_character" (
    actor_id BIGSERIAL NOT NULL,
    character_id BIGSERIAL NOT NULL,
    PRIMARY KEY (actor_id, character_id),
    UNIQUE (actor_id, character_id),
    FOREIGN KEY (actor_id) REFERENCES actor(id) ON DELETE CASCADE,
    FOREIGN KEY (character_id) REFERENCES character(id) ON DELETE CASCADE
);