INSERT INTO blocks(id, organisation_id, topic_id, type, sequence_id)
OVERRIDING SYSTEM VALUE
VALUES 
	(1, 1, 1, 0, 0),
	(2, 1, 2, 0, 0),
	(3, 1, 2, 0, 1),
	(4, 1, 2, 0, 2);

SELECT setval('blocks_id_seq', (SELECT MAX(id) from "blocks"));
