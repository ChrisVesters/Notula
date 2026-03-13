INSERT INTO topics(id, organisation_id, meeting_id, name)
OVERRIDING SYSTEM VALUE
VALUES 
	(1, 1, 1, 'Deliverables'),
	(2, 1, 1, 'Blockers'),
	(3, 1, 1, 'Timeline'),
	(4, 2, 3, 'Looking Back');

SELECT setval('topics_id_seq', (SELECT MAX(id) from "topics"));
