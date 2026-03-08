-- WMS feature extension SQL
-- Execute this script manually on the `wms` database before using the new features.

ALTER TABLE `user`
    ADD COLUMN `email` varchar(128) NULL COMMENT 'email' AFTER `phone`;

ALTER TABLE `goods`
    ADD COLUMN `barcode` varchar(128) NULL COMMENT 'barcode' AFTER `remark`,
    ADD COLUMN `rfid_tag` varchar(128) NULL COMMENT 'rfid tag' AFTER `barcode`,
    ADD COLUMN `min_stock` int NULL DEFAULT 0 COMMENT 'low stock threshold' AFTER `rfid_tag`,
    ADD COLUMN `max_stock` int NULL DEFAULT 0 COMMENT 'high stock threshold' AFTER `min_stock`,
    ADD COLUMN `supplier_id` int NULL COMMENT 'default supplier id' AFTER `max_stock`;

ALTER TABLE `record`
    ADD COLUMN `batch_id` int NULL COMMENT 'batch id' AFTER `remark`,
    ADD COLUMN `supplier_id` int NULL COMMENT 'supplier id' AFTER `batch_id`,
    ADD COLUMN `action_type` varchar(32) NULL COMMENT 'INBOUND/OUTBOUND/TRANSFER_IN/TRANSFER_OUT' AFTER `supplier_id`,
    ADD COLUMN `scan_code` varchar(128) NULL COMMENT 'barcode or rfid' AFTER `action_type`;

CREATE TABLE IF NOT EXISTS `supplier` (
    `id` int NOT NULL AUTO_INCREMENT,
    `code` varchar(64) NOT NULL,
    `name` varchar(128) NOT NULL,
    `contact_person` varchar(64) NULL,
    `phone` varchar(32) NULL,
    `email` varchar(128) NULL,
    `address` varchar(255) NULL,
    `level` varchar(32) NULL,
    `status` varchar(32) NULL DEFAULT 'ACTIVE',
    `remark` varchar(255) NULL,
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_supplier_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `goods_batch` (
    `id` int NOT NULL AUTO_INCREMENT,
    `goods_id` int NOT NULL,
    `storage_id` int NOT NULL,
    `supplier_id` int NULL,
    `batch_no` varchar(64) NOT NULL,
    `barcode` varchar(128) NULL,
    `rfid_tag` varchar(128) NULL,
    `production_date` date NULL,
    `expiry_date` date NULL,
    `quantity` int NOT NULL DEFAULT 0,
    `alert_days` int NULL DEFAULT 30,
    `status` varchar(32) NULL DEFAULT 'ACTIVE',
    `remark` varchar(255) NULL,
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_goods_batch_no` (`batch_no`, `storage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `warehouse_transfer` (
    `id` int NOT NULL AUTO_INCREMENT,
    `goods_id` int NOT NULL,
    `goods_name` varchar(128) NOT NULL,
    `batch_id` int NULL,
    `batch_no` varchar(64) NULL,
    `from_storage_id` int NOT NULL,
    `from_storage_name` varchar(128) NOT NULL,
    `to_storage_id` int NOT NULL,
    `to_storage_name` varchar(128) NOT NULL,
    `quantity` int NOT NULL,
    `status` varchar(32) NOT NULL DEFAULT 'PENDING',
    `reason` varchar(255) NULL,
    `operator_id` int NULL,
    `operator_name` varchar(64) NULL,
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
    `execute_time` datetime NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `inventory_alert` (
    `id` int NOT NULL AUTO_INCREMENT,
    `source_key` varchar(128) NOT NULL,
    `goods_id` int NULL,
    `goods_name` varchar(128) NULL,
    `storage_id` int NULL,
    `storage_name` varchar(128) NULL,
    `batch_id` int NULL,
    `batch_no` varchar(64) NULL,
    `alert_type` varchar(32) NOT NULL,
    `alert_level` varchar(32) NOT NULL,
    `content` varchar(255) NOT NULL,
    `current_value` decimal(18,2) NULL,
    `threshold_value` decimal(18,2) NULL,
    `status` varchar(32) NOT NULL DEFAULT 'UNREAD',
    `notify_channels` varchar(64) NULL DEFAULT 'SYSTEM',
    `notified` tinyint(1) NOT NULL DEFAULT 0,
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_alert_source_key` (`source_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `notification_message` (
    `id` int NOT NULL AUTO_INCREMENT,
    `title` varchar(128) NOT NULL,
    `content` varchar(255) NOT NULL,
    `channel` varchar(32) NOT NULL DEFAULT 'SYSTEM',
    `recipient` varchar(128) NULL,
    `status` varchar(32) NOT NULL DEFAULT 'UNREAD',
    `related_type` varchar(32) NULL,
    `related_id` int NULL,
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` int NOT NULL AUTO_INCREMENT,
    `operator_id` int NULL,
    `operator_name` varchar(64) NULL,
    `operator_no` varchar(64) NULL,
    `role_id` int NULL,
    `module_name` varchar(64) NULL,
    `action_name` varchar(128) NULL,
    `request_method` varchar(16) NULL,
    `request_path` varchar(255) NULL,
    `request_params` text NULL,
    `success` tinyint(1) NOT NULL DEFAULT 1,
    `message` varchar(255) NULL,
    `ip_address` varchar(64) NULL,
    `user_agent` varchar(255) NULL,
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
