CREATE TABLE `form1` (
	id INT PRIMARY KEY AUTO_INCREMENT,
	`field` INT NOT NULL DEFAULT 0
);


CREATE TABLE `form2` (
	id INT PRIMARY KEY AUTO_INCREMENT,
	`field` INT NOT NULL DEFAULT 0,
	`date` datetime
);

CREATE TABLE `flow` (
	id INT PRIMARY KEY AUTO_INCREMENT,
	`type` INT NOT NULL DEFAULT 0,
	form_id INT NOT NULL DEFAULT 0,
	create_by VARCHAR(20) NOT NULL DEFAULT ''
);
