CREATE DATABASE IF NOT EXISTS e_identity_service
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- 1. identity permissions
CREATE TABLE IF NOT EXISTS `e_identity_service`.`permissions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `api_path` VARCHAR(100) DEFAULT NULL,
    `method` VARCHAR(10) DEFAULT NULL,
    `module` VARCHAR(100) DEFAULT NULL,
    `service` VARCHAR(100) DEFAULT NULL,
    `activated` TINYINT(1) DEFAULT 1,
    
    -- Audit fields
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 2. identity roles
CREATE TABLE IF NOT EXISTS `e_identity_service`.`roles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `slug` VARCHAR(100) NOT NULL,
    `activated` TINYINT(1) DEFAULT 1,

    -- Audit fields
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_slug` (`slug`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

--  3. identity permission_role (mapping table)
CREATE TABLE IF NOT EXISTS `e_identity_service`.`permission_role` (
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`role_id`, `permission_id`),
    CONSTRAINT `fk_role_perm` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_perm_role` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 4. identity users    
CREATE TABLE IF NOT EXISTS `e_identity_service`.`users` (
    `id` CHAR(36) NOT NULL COMMENT 'Primary key (UUID)',
    `user_name` VARCHAR(100) NOT NULL COMMENT 'Unique username',
    `email` VARCHAR(100) DEFAULT NULL COMMENT 'Unique email',
    `password` VARCHAR(255) NOT NULL COMMENT 'Hashed password',
    `full_name` VARCHAR(100) NOT NULL COMMENT 'User full name',
    `phone_number` VARCHAR(11) NOT NULL COMMENT 'User phone number',
    `gender` VARCHAR(20) DEFAULT NULL COMMENT 'Gender enum (STRING)',
    `dob` DATE DEFAULT NULL COMMENT 'Date of birth',
    `refresh_token` MEDIUMTEXT DEFAULT NULL COMMENT 'JWT refresh token',
    `email_verified` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Is email verified?',
    `verification_code` VARCHAR(64) DEFAULT NULL COMMENT 'Code for email verification',
    `verification_expiration` DATETIME DEFAULT NULL COMMENT 'Verification code expiration',
    `activated` TINYINT(1) DEFAULT 1 COMMENT 'Is account active?',
    `role_id` BIGINT DEFAULT NULL COMMENT 'Foreign key to roles table', -- Giả định Role ID là BIGINT

    -- Audit fields từ AbstractAuditingEntity
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_name` (`user_name`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_phone_number` (`phone_number`),
    KEY `idx_email` (`email`),
    KEY `idx_email_full_name_phone_number` (`email`, `full_name`, `phone_number`),
    KEY `idx_created_by` (`created_by`),
    KEY `idx_role_id` (`role_id`),
    CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'User account information table';

ALTER TABLE `e_identity_service`.`users` 
MODIFY COLUMN `gender` VARCHAR(20) DEFAULT NULL,
ADD CONSTRAINT `chk_gender` CHECK (`gender` IN ('MALE', 'FEMALE'));

-- 5. identity address
CREATE TABLE IF NOT EXISTS `e_identity_service`.`addresses` (
    `id` CHAR(36) NOT NULL COMMENT 'Primary key (UUID)',
    `address` VARCHAR(255) DEFAULT NULL COMMENT 'Street address detail',
    `province` VARCHAR(100) DEFAULT NULL COMMENT 'Province or City name',
    `district` VARCHAR(100) DEFAULT NULL COMMENT 'District name',
    `ward` VARCHAR(100) DEFAULT NULL COMMENT 'Ward name',
    `activated` TINYINT(1) DEFAULT 1 COMMENT 'Boolean: 1 for true, 0 for false',
    `current_user_address` TINYINT(1) DEFAULT 0 COMMENT 'Default user address',
    `user_id` CHAR(36) DEFAULT NULL COMMENT 'Foreign key to users table',
    `shop_management_id` CHAR(36) DEFAULT NULL COMMENT 'UUID for shop management reference',
    
    -- Các cột kế thừa từ AbstractAuditingEntity (thường là thế này)
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    -- Tối ưu Index cho việc truy vấn địa chỉ theo User hoặc Shop
    KEY `idx_user_id` (`user_id`),
    KEY `idx_shop_management_id` (`shop_management_id`),
    CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Table storing user and shop addresses';
