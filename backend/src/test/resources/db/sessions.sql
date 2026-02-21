INSERT INTO sessions(id, user_id, organisation_id, refresh_token, active_until)
OVERRIDING SYSTEM VALUE
VALUES 
	(1, 1, 1, 'abc7775', NOW() + INTERVAL '7 days'),
	(2, 2, NULL, 'eff74def', NOW() + INTERVAL '30 days'),
	(3, 4, NULL, 'ghj89tyu', NOW() + INTERVAL '15 days'),
	(7, 1, NULL, 'ddef741', NOW() - INTERVAL '1 days'),
	(8, 4, 2, 'ad98gh3', NOW() + INTERVAL '3 days');

SELECT setval('sessions_id_seq', (SELECT MAX(id) from "sessions"));
