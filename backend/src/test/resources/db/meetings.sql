INSERT INTO meetings(id, organisation_id, name, description, revision)
OVERRIDING SYSTEM VALUE
VALUES 
	(1, 1, 'Project Meeting', 'Discuss project progress and next steps', 17),
	(2, 1, 'Retrospective Meeting', 'Reflect on the past sprint and identify improvements', 0),
	(3, 2, '2026 Kickoff Meeting', 'What are our goals and expectations for 2026?', 5),
	(4, 1, 'Q2 Planning Session', '', 9);

SELECT setval('meetings_id_seq', (SELECT MAX(id) from "meetings"));
