/*
Navicat MySQL Data Transfer

Source Server         : easyrec
Source Server Version : 50617
Source Host           : localhost:3306
Source Database       : advisor_headline

Target Server Type    : MYSQL
Target Server Version : 50617
File Encoding         : 65001

Date: 2016-09-23 19:42:36
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for rec_789
-- ----------------------------
DROP TABLE IF EXISTS `rec_789`;
CREATE TABLE `rec_789` (
  `iid` int(11) NOT NULL,
  `mid` int(11) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`iid`),
  KEY `timestamp` (`timestamp`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
