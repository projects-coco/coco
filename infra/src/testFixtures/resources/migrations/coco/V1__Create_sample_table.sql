CREATE TABLE `sample` (
                          `id` BINARY(16) NOT NULL,
                          `changeable_value1` varchar(255),
                          `changeable_value2` varchar(255),
                          `changeable_value3` varchar(255),
                          `changeable_int1` INT,
                          `changeable_int2` INT,
                          `created_at` DATETIME NOT NULL,
                          `updated_at` DATETIME NOT NULL,
                          `deleted_at` DATETIME,
                          PRIMARY KEY (`ID`)
) ENGINE=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=utf8mb4_general_ci;