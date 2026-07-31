-- will create all the tables using CREATE, but will not insert any data yet.
-- Enable PostGIS
CREATE EXTENSION IF NOT EXISTS postgis;

-- Users
CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- cities table
CREATE TABLE cities (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_city_country UNIQUE (name, country)
);

-- Categories
-- Examples: History, Art, Nature, Food, Architecture
CREATE TABLE categories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Trips
CREATE TABLE trips (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    city_id BIGINT NOT NULL,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_trip_dates CHECK (
        end_date IS NULL
        OR start_date IS NULL
        OR end_date >= start_date
    ),
    CONSTRAINT fk_trip_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_city FOREIGN KEY (city_id) REFERENCES cities (id) ON DELETE CASCADE
);

-- User/trip preferences
-- Score represents how much the user likes a category
-- 1 = dislike, 5 = love
CREATE TABLE trip_preferences (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    score SMALLINT NOT NULL,
    CONSTRAINT score_between_one_and_five CHECK (score BETWEEN 1 AND 5),
    CONSTRAINT fk_preference_trip FOREIGN KEY (trip_id) REFERENCES trips (id) ON DELETE CASCADE,
    CONSTRAINT fk_preference_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE,
    CONSTRAINT unique_trip_category_preference UNIQUE (trip_id, category_id)
);

-- Attractions
-- Populated later from RAG/LLM pipeline
CREATE TABLE attractions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    city_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address VARCHAR(500),
    location geometry (Point, 4326) NOT NULL,
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_attraction_city UNIQUE (name, city_id),
    CONSTRAINT fk_attraction_city FOREIGN KEY (city_id) REFERENCES cities (id) ON DELETE CASCADE
);

-- LLM generated attraction category scores
-- Every attraction receives a score for each category
CREATE TABLE attraction_category_scores (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    attraction_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    score SMALLINT NOT NULL,
    CONSTRAINT attraction_score_between_one_and_five CHECK (score BETWEEN 1 AND 5),
    CONSTRAINT fk_attraction_score_attraction FOREIGN KEY (attraction_id) REFERENCES attractions (id) ON DELETE CASCADE,
    CONSTRAINT fk_attraction_score_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE,
    CONSTRAINT unique_attraction_category_score UNIQUE (attraction_id, category_id)
);

-- Generated daily plans
CREATE TABLE trip_itinerary (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    attraction_id BIGINT NOT NULL,
    visit_date DATE NOT NULL,
    visit_order INTEGER NOT NULL,
    start_time TIME,
    end_time TIME,
    recommendation_score DECIMAL(4, 2),
    CONSTRAINT fk_itinerary_trip FOREIGN KEY (trip_id) REFERENCES trips (id) ON DELETE CASCADE,
    CONSTRAINT fk_itinerary_attraction FOREIGN KEY (attraction_id) REFERENCES attractions (id) ON DELETE CASCADE,
    CONSTRAINT unique_trip_visit_order UNIQUE (
        trip_id,
        visit_date,
        visit_order
    )
);