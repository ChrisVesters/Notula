INSERT INTO organisation_users(id, organisation_id, user_id)
OVERRIDING SYSTEM VALUE
VALUES 
	(1, 1, 1);

SELECT setval('organisation_users_id_seq', (SELECT MAX(id) from "organisation_users"));
