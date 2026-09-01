UPDATE sighs
SET memo = NULL
WHERE char_length(memo) > 50;

ALTER TABLE sighs
    ALTER COLUMN memo TYPE VARCHAR(50);
