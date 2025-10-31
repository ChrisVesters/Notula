INSERT INTO sessions(id, user_id, refresh_token, active_until)
OVERRIDING SYSTEM VALUE
VALUES 
	(1, 1, 'abc7775', NOW() + INTERVAL '7 days'),
	(2, 2, 'eff74def', NOW() + INTERVAL '30 days'),
	(7, 1, 'ddef741', NOW() - INTERVAL '1 days');

SELECT setval('sessions_id_seq', (SELECT MAX(id) from "sessions"));
