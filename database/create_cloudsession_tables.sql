/*
 * Base Cloud Session database schema.
 */

USE cloudsession;


--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(250) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `salt` varchar(50) DEFAULT NULL,
  `auth_source` varchar(250) DEFAULT NULL,
  `locale` varchar(50) DEFAULT NULL,
  `blocked` tinyint(1) DEFAULT NULL,
  `confirmed` tinyint(1) DEFAULT NULL,
  `screen_name` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB
  AUTO_INCREMENT=0
  DEFAULT CHARSET=utf8;


--
-- Table structure for table `authentication_token`
--
DROP TABLE IF EXISTS `authentication_token`;
CREATE TABLE `authentication_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_user` bigint(20) DEFAULT NULL,
  `browser` varchar(200) DEFAULT NULL,
  `server` varchar(1000) DEFAULT NULL,
  `ip_address` varchar(200) DEFAULT NULL,
  `validity` datetime DEFAULT NULL,
  `token` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `id_user` (`id_user`),
  CONSTRAINT `authentication_token_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB
  AUTO_INCREMENT=0
  DEFAULT CHARSET=utf8;

--
-- Table structure for table `bucket`
--

DROP TABLE IF EXISTS `bucket`;
CREATE TABLE `bucket` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_user` bigint(20) DEFAULT NULL,
  `type` varchar(200) DEFAULT NULL,
  `content` int(11) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_type_unique` (`id_user`,`type`),
  CONSTRAINT `bucket_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB
  AUTO_INCREMENT=0
  DEFAULT CHARSET=utf8;

--
-- Table structure for table `confirmtoken`
--

DROP TABLE IF EXISTS `confirm_token`;
CREATE TABLE `confirm_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_user` bigint(20) DEFAULT NULL,
  `validity` datetime DEFAULT NULL,
  `token` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_user` (`id_user`),
  UNIQUE KEY `token` (`token`),
  CONSTRAINT `confirm_token_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB
  AUTO_INCREMENT=0
  DEFAULT CHARSET=utf8;


--
-- Table structure for table `resettoken`
--

DROP TABLE IF EXISTS `reset_token`;
CREATE TABLE `reset_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_user` bigint(20) DEFAULT NULL,
  `validity` datetime DEFAULT NULL,
  `token` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_user` (`id_user`),
  UNIQUE KEY `token` (`token`),
  CONSTRAINT `reset_token_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB
  AUTO_INCREMENT=0
  DEFAULT CHARSET=latin1;
