INSERT INTO meetings(id, organisation_id, name)
OVERRIDING SYSTEM VALUE
VALUES 
	(1, 1, 'Project Meeting'),
	(2, 1, 'Retrospective Meeting'),
	(3, 2, '2026 Kickoff Meeting'),
	(4, 1, 'Q2 Planning Session');

SELECT setval('meetings_id_seq', (SELECT MAX(id) from "meetings"));
