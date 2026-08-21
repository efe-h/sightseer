-- Spring Boot will only control the user profile and preferences, not the attractions data.
-- The attractions data is static and will be loaded from a JSON file in the FastAPI service
-- that will run the recommendation engine.

CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_preferences (
    user_id BIGINT PRIMARY KEY,
    history SMALLINT NOT NULL,
    art SMALLINT NOT NULL,
    architecture SMALLINT NOT NULL,
    nature SMALLINT NOT NULL,
    science SMALLINT NOT NULL,
    food SMALLINT NOT NULL,
    entertainment SMALLINT NOT NULL,
    shopping SMALLINT NOT NULL,
    views SMALLINT NOT NULL,
    family SMALLINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_preferences_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT history_between_one_and_five CHECK (history BETWEEN 1 AND 5),
    CONSTRAINT art_between_one_and_five CHECK (art BETWEEN 1 AND 5),
    CONSTRAINT architecture_between_one_and_five CHECK (architecture BETWEEN 1 AND 5),
    CONSTRAINT nature_between_one_and_five CHECK (nature BETWEEN 1 AND 5),
    CONSTRAINT science_between_one_and_five CHECK (science BETWEEN 1 AND 5),
    CONSTRAINT food_between_one_and_five CHECK (food BETWEEN 1 AND 5),
    CONSTRAINT entertainment_between_one_and_five CHECK (entertainment BETWEEN 1 AND 5),
    CONSTRAINT shopping_between_one_and_five CHECK (shopping BETWEEN 1 AND 5),
    CONSTRAINT views_between_one_and_five CHECK (views BETWEEN 1 AND 5),
    CONSTRAINT family_between_one_and_five CHECK (family BETWEEN 1 AND 5)
);