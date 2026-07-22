INSERT INTO organisation_users(id, organisation_id, user_id, role)
OVERRIDING SYSTEM VALUE
VALUES 
	(1, 1, 1, 0),
	(2, 1, 2, 1),
	(3, 2, 4, 0),
	(4, 3, 4, 1);

SELECT setval('organisation_users_id_seq', (SELECT MAX(id) from "organisation_users"));
