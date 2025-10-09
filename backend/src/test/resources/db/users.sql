INSERT INTO users(id, email, password)
OVERRIDING SYSTEM VALUE
VALUES 
	(1, 'eduardo.christiansen@sporer.com', 'bbkpHh_hKk6KMwv'),
	(2, 'kristina.thiel@sporer.com', 'wLITAlWOYY5J8ms'),
	(3, 'daphnee.lesch@sporer.com', 'VIz3jmembRtsuoo'),
	(4, 'alison_dach@glover-group.co.uk', 'YIHS3bbkpHh_hKk'),
	(5, 'carlotta_moen@glover-group.co.uk', 'uRlN54aT_Qh6Rvk'),
	(6, 'damaris_bins@glover-group.co.uk', 'vRax5wOXVWX9c6S'),
	(7, 'micheal.hagenes@huel.eu', 'qBTs4oreciZGlcX'),
	(8, 'harry.powlowski@heul.eu', 'iSQlzXtvTKumDFT'),
	(9, 'casper.walter@heul.eu', 'hetbhBBiBvhItPk');

SELECT setval('users_id_seq', (SELECT MAX(id) from "users"));
