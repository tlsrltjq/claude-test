ALTER TABLE users DROP CONSTRAINT IF EXISTS users_position_check;
ALTER TABLE users ADD CONSTRAINT users_position_check CHECK (
    position IN (
        'REPRESENTATIVE',
        'EXECUTIVE_DIRECTOR',
        'MANAGING_DIRECTOR',
        'DIRECTOR',
        'GENERAL_MANAGER',
        'DEPUTY_GENERAL_MANAGER',
        'MANAGER',
        'ASSISTANT_MANAGER',
        'STAFF'
    )
);
