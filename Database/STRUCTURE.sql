CREATE TABLE `comments` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `message_id` int(10) unsigned NOT NULL,
  `user_id` int(10) unsigned NOT NULL,
  `text_encrypted` blob NOT NULL,
  `comment_secret` varchar(255) NOT NULL,
  `private_to_user` int(10) unsigned DEFAULT NULL,
  `time_inserted` int(10) unsigned NOT NULL,
  `deleted` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `selection` (`message_id`,`time_inserted`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
-- 
CREATE TABLE `connections` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `from_user` int(10) unsigned NOT NULL,
  `type` enum('friend','block') NOT NULL,
  `to_user` int(10) unsigned NOT NULL,
  `time_inserted` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `combination` (`from_user`,`to_user`),
  KEY `selection` (`to_user`,`type`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
-- 
CREATE TABLE `favorites` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(10) unsigned NOT NULL,
  `message_id` int(10) unsigned NOT NULL,
  `degree` int(10) unsigned NOT NULL,
  `time_added` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `combination` (`user_id`,`message_id`),
  KEY `selection` (`user_id`,`time_added`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
-- 
CREATE TABLE `feeds` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(10) unsigned NOT NULL,
  `message_id` int(10) unsigned NOT NULL,
  `degree` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `combination` (`user_id`,`message_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
-- 
CREATE TABLE `ids_in_threads` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `content_type` enum('message','comment') NOT NULL,
  `content_id` int(10) unsigned NOT NULL,
  `private_id` int(10) unsigned NOT NULL,
  `public_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `combination` (`content_type`,`content_id`,`private_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
-- 
CREATE TABLE `messages` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(10) unsigned NOT NULL,
  `color_hex` varchar(7) NOT NULL,
  `pattern_id` int(10) unsigned NOT NULL,
  `text_encrypted` blob NOT NULL,
  `message_secret` varchar(255) NOT NULL,
  `favorites_count` int(10) unsigned NOT NULL DEFAULT '0',
  `comments_count` int(10) unsigned NOT NULL DEFAULT '0',
  `time_published` int(10) unsigned NOT NULL,
  `time_active` int(10) unsigned NOT NULL DEFAULT '2147483647',
  `language_iso3` varchar(3) DEFAULT NULL,
  `country_iso3` varchar(3) DEFAULT NULL,
  `topic` enum('','politics','art','business','work','culture','health','science','sports','technology','sex','dating','beauty','books','movies','music','family','food','life','love','meta') NOT NULL DEFAULT '',
  `score` decimal(8,6) unsigned NOT NULL DEFAULT '0.000000',
  `dispatched` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `deleted` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `time_published` (`time_published`),
  KEY `dispatcher` (`dispatched`,`time_published`),
  KEY `popular_by_language` (`language_iso3`,`score`),
  KEY `latest_by_language` (`language_iso3`,`time_published`),
  KEY `time_active` (`time_active`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
-- 
CREATE TABLE `reports` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(10) unsigned NOT NULL,
  `content_type` enum('message','comment') NOT NULL,
  `content_id` int(10) unsigned NOT NULL,
  `reason` int(10) unsigned NOT NULL,
  `weight` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `time_reported` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `combination` (`user_id`,`content_type`,`content_id`),
  KEY `selection_by_user` (`user_id`,`time_reported`),
  KEY `selection_by_content` (`content_type`,`content_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
-- 
CREATE TABLE `subscriptions` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `message_id` int(10) unsigned NOT NULL,
  `user_id` int(10) unsigned NOT NULL,
  `degree` int(10) unsigned NOT NULL DEFAULT '3',
  `counter` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `combination` (`message_id`,`user_id`),
  KEY `selection` (`user_id`,`counter`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
-- 
CREATE TABLE `throttling` (
  `username` varchar(255) NOT NULL,
  `date_str` char(8) NOT NULL,
  `action_type` enum('failed_login') NOT NULL,
  `action_count` smallint(8) unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`username`,`date_str`,`action_type`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
-- 
CREATE TABLE `users` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `reported_count` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `write_lock_until` int(10) unsigned NOT NULL DEFAULT '0',
  `login_throttled_until` int(10) unsigned NOT NULL DEFAULT '0',
  `time_last_active` int(10) unsigned DEFAULT NULL,
  `time_registered` int(10) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `login` (`username`(166),`password`(166))
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
-- 
CREATE TABLE `verifications` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(10) unsigned NOT NULL,
  `new_password` varchar(255) NOT NULL,
  `verification_code` varchar(255) NOT NULL,
  `time_created` int(10) unsigned NOT NULL,
  `time_until` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `selection_by_user` (`user_id`,`time_until`),
  KEY `selection_by_code` (`verification_code`,`time_until`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
