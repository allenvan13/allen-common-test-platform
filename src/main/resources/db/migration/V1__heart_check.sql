CREATE TABLE `heart_check_target`
(
    `id`            tinyint(5)   NOT NULL AUTO_INCREMENT COMMENT 'id',
    `url`           varchar(500) NOT NULL COMMENT '被测URL',
    `params`        varchar(500) DEFAULT NULL COMMENT '拼接参数',
    `service_name`  varchar(255) DEFAULT NULL COMMENT '被测服务名称',
    `notice_users`  varchar(500) DEFAULT NULL COMMENT '相关人员 多个,分割',
    `notice_groups` varchar(500) DEFAULT NULL COMMENT '群组名称 多个,分割',
    `terminal_code` varchar(50)  DEFAULT NULL COMMENT '被测端口Code',
    `username`      varchar(50)  DEFAULT NULL COMMENT '测试账号 用户名',
    `password`      varchar(50)  DEFAULT NULL COMMENT '测试账号 密码(加密后)',
    `env`           varchar(10)  NOT NULL COMMENT '被测环境 UAT FAT PRO',
    `del_flag`      tinyint(1)   DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;