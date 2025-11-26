INSERT INTO organisations(id, name)
OVERRIDING SYSTEM VALUE
VALUES 
	(1, 'Sporer LLC'),
	(2, 'Glover Group'),
	(3, 'Heul');

SELECT setval('organisations_id_seq', (SELECT MAX(id) from "organisations"));
