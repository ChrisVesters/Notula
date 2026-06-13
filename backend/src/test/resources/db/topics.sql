INSERT INTO topics(id, organisation_id, meeting_id, name, description)
OVERRIDING SYSTEM VALUE
VALUES 
	(1, 1, 1, 'Deliverables', 'What needs to be done for the project'),
	(2, 1, 1, 'Blockers', 'What is blocking us right now'),
	(3, 1, 1, 'Timeline', 'How can we get this organised'),
	(4, 2, 3, 'Looking Back', 'What went well and what can be improved from last year');

SELECT setval('topics_id_seq', (SELECT MAX(id) from "topics"));
