CREATE INDEX idx_sighs_location_gist
    ON sighs USING GIST (location);
