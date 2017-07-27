drop procedure if exists create_rec_table;
create procedure create_rec_table(in uid VARCHAR(20))
BEGIN
 DECLARE tbname VARCHAR(50);
SET @tbname=CONCAT('rec_',uid);
SET @sqls=CONCAT("CREATE TABLE if not exists ",@tbname," (
  `iid` int(11) NOT NULL,
  `mid` int(11) NOT NULL,
  `timestamp` bigint NOT NULL ,
  PRIMARY KEY (`iid`),
  KEY `timestamp` (`timestamp`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

	PREPARE pst FROM @sqls;   
  EXECUTE pst;
END;

drop procedure if exists create_vh_table;
create procedure create_vh_table(in uid VARCHAR(20))
BEGIN
 DECLARE tbname VARCHAR(50);
SET @tbname=CONCAT('view_history_',uid);
SET @sqls=CONCAT("CREATE TABLE if not exists ",@tbname," (
  `iid` int(11) unsigned NOT NULL,
  `act_time` bigint NOT NULL ,
  PRIMARY KEY (`iid`),
  KEY `act_time` (`act_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

	PREPARE pst FROM @sqls;   
  EXECUTE pst;
END;

drop procedure if exists create_at_table;
create procedure create_at_table()
BEGIN
DROP TABLE IF EXISTS `alive_test`;
CREATE TABLE `alive_test` (
  `id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO alive_test VALUES (1);
END;

DROP TABLE IF EXISTS `alive_test`;
CREATE TABLE `alive_test` (
  `id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO alive_test VALUES (1);

DROP TABLE IF EXISTS `default_rec`;
CREATE TABLE `default_rec` (
  `iid` int(11) NOT NULL,
  `mid` int(11) NOT NULL,
  `timestamp` bigint NOT NULL,
  PRIMARY KEY (`iid`),
  KEY `timestamp` (`timestamp`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
