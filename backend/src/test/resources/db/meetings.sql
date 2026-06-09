INSERT INTO meetings(id, organisation_id, name, description)
OVERRIDING SYSTEM VALUE
VALUES 
	(1, 1, 'Project Meeting', 'Discuss project progress and next steps'),
	(2, 1, 'Retrospective Meeting', 'Reflect on the past sprint and identify improvements'),
	(3, 2, '2026 Kickoff Meeting', 'What are our goals and expectations for 2026?'),
	(4, 1, 'Q2 Planning Session', '');

SELECT setval('meetings_id_seq', (SELECT MAX(id) from "meetings"));
