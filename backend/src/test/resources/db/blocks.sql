INSERT INTO blocks(id, organisation_id, topic_id, type, rank)
OVERRIDING SYSTEM VALUE
VALUES 
	(1, 1, 1, 0, '1'),
	(2, 1, 2, 0, '1'),
	(3, 1, 2, 0, '2'),
	(4, 1, 2, 0, '3');

SELECT setval('blocks_id_seq', (SELECT MAX(id) from "blocks"));
