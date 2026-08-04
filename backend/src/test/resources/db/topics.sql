INSERT INTO topics(id, organisation_id, meeting_id, sequence_id, name, description, duration)
OVERRIDING SYSTEM VALUE
VALUES
	(1, 1, 1, 0, 'Deliverables', 'What needs to be done for the project', 30),
	(2, 1, 1, 1, 'Blockers', 'What is blocking us right now', 15),
	(3, 1, 1, 2, 'Timeline', 'How can we get this organised', NULL),
	(4, 2, 3, 0, 'Looking Back', 'What went well and what can be improved from last year', 45);

SELECT setval('topics_id_seq', (SELECT MAX(id) from "topics"));
