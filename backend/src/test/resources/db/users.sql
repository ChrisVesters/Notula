INSERT INTO users(id, email)
OVERRIDING SYSTEM VALUE
VALUES
	(1, 'eduardo.christiansen@sporer.com'),
	(2, 'kristina.thiel@sporer.com'),
	(3, 'daphnee.lesch@sporer.com'),
	(4, 'alison_dach@glover-group.co.uk'),
	(5, 'carlotta_moen@glover-group.co.uk'),
	(6, 'damaris_bins@glover-group.co.uk'),
	(7, 'micheal.hagenes@huel.eu'),
	(8, 'harry.powlowski@heul.eu'),
	(9, 'casper.walter@heul.eu');

SELECT setval('users_id_seq', (SELECT MAX(id) from "users"));
