LOCK TABLES `property_value_history` WRITE;
/*!40000 ALTER TABLE `property_value_history` DISABLE KEYS */;
INSERT INTO `property_value_history` VALUES
(1,24,NULL,'Test Val',NULL,NULL,'2021-02-04 13:48:41',1,'2021-02-04 13:48:41',NULL,NULL,NULL),
(2,24,NULL,'updatedValue',NULL,NULL,'2021-02-04 13:48:41',1,'2021-02-04 13:48:41',NULL,NULL,NULL),
(3,23,NULL,'-','',NULL,'2021-02-04 13:48:41',1,'2021-02-04 13:48:41',NULL,NULL,NULL),
(4,23,NULL,'Ordered','',NULL,'2021-02-04 13:51:53',1,'2021-02-04 13:51:53',NULL,NULL,NULL),
(5,27,NULL,'Test Val',NULL,NULL,'2021-02-04 13:51:53',1,'2021-02-04 13:51:53',NULL,NULL,NULL),
(6,27,NULL,'updatedValue',NULL,NULL,'2021-02-04 13:51:53',1,'2021-02-04 13:51:53',NULL,NULL,NULL);
/*!40000 ALTER TABLE `property_value_history` ENABLE KEYS */;
UNLOCK TABLES;
