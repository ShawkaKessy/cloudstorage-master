TRUNCATE TABLE files RESTART IDENTITY CASCADE;
TRUNCATE TABLE auth_tokens RESTART IDENTITY CASCADE;
TRUNCATE TABLE users RESTART IDENTITY CASCADE;

INSERT INTO users (login, password)
VALUES ('testuser@example.com', '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW');

INSERT INTO auth_tokens (user_id, token)
SELECT id, 'sample-token-1' FROM users WHERE login='testuser@example.com'
UNION ALL
SELECT id, 'sample-token-2' FROM users WHERE login='testuser@example.com';

INSERT INTO files (user_id, filename, size, created_at)
SELECT id, 'document.pdf', 2048, CURRENT_TIMESTAMP FROM users WHERE login='testuser@example.com'
UNION ALL
SELECT id, 'photo.png', 102400, CURRENT_TIMESTAMP FROM users WHERE login='testuser@example.com'
UNION ALL
SELECT id, 'notes.txt', 512, CURRENT_TIMESTAMP FROM users WHERE login='testuser@example.com';
