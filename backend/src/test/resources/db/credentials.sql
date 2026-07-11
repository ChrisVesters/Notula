INSERT INTO credentials(id, user_id, password)
OVERRIDING SYSTEM VALUE
VALUES
	(1, 1, 'bbkpHh_hKk6KMwv'),
	(2, 2, 'wLITAlWOYY5J8ms'),
	(3, 3, 'VIz3jmembRtsuoo'),
	(4, 4, 'YIHS3bbkpHh_hKk');

SELECT setval('credentials_id_seq', (SELECT MAX(id) from "credentials"));
