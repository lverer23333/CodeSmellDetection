/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50727
Source Host           : localhost:3306
Source Database       : testdata666

Target Server Type    : MYSQL
Target Server Version : 50727
File Encoding         : 65001

Date: 2019-09-04 17:29:02
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `projectinfo`
-- ----------------------------
DROP TABLE IF EXISTS `projectinfo`;
CREATE TABLE `projectinfo` (
  `InfoId` int(11) identity(1,1) NOT NULL,
  `MethodName` varchar(1000),
  `ClassName` varchar(1000),
  `TargetClassName` varchar(1000),
  `Distance1` varchar(1000),
  `Distane2` varchar(1000),
  PRIMARY KEY (`InfoId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of projectinfo
-- ----------------------------
