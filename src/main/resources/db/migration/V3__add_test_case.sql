CREATE TABLE `ui_test_case_step`
(
    `id`                   bigint(20) NOT NULL COMMENT 'case_step主键ID',
    `case_id`              bigint(20) NOT NULL COMMENT '测试用例ID',
    `case_code`            varchar(20)   DEFAULT NULL COMMENT '测试用例编码',
    `sort`                 int(5)     NOT NULL COMMENT '步骤顺序号',
    `description`          varchar(1000) DEFAULT NULL COMMENT '步骤描述',
    `action_keyword`       varchar(20)   DEFAULT NULL COMMENT '关键字(操作)编码',
    `element_locate_type`  varchar(50)   DEFAULT NULL COMMENT '元素定位 定位方式(类型)',
    `element_locate_value` varchar(255)  DEFAULT NULL COMMENT '元素定位信息表达式',
    `parameter`            varchar(255)  DEFAULT NULL COMMENT '输入(操作)值',
    `create_user`          varchar(50)   DEFAULT NULL COMMENT '创建人',
    `create_date`          datetime      DEFAULT NULL COMMENT '创建时间',
    `update_user`          varchar(50)   DEFAULT NULL COMMENT '更新人',
    `update_date`          datetime      DEFAULT NULL COMMENT '更新时间',
    `del_flag`             tinyint(1) NOT NULL COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='测试平台-UI测试用例步骤明细';

CREATE TABLE `test_case`
(
    `id`          bigint(20) NOT NULL COMMENT 'case主键ID',
    `suite_id`    bigint(20)    DEFAULT NULL COMMENT '测试集ID',
    `team_code`   varchar(20)   DEFAULT NULL COMMENT '团队code JX-匠星 XWH-希望云 DWWB-地网无边 JSPT-技术平台 DSJPT-大数据平台 ALL-所有团队',
    `case_code`   varchar(20)   DEFAULT NULL COMMENT '测试用例编码',
    `type`        tinyint(1) NOT NULL COMMENT '测试用例类型  1-接口测试 2-Web测试 3-安卓APP测试 4-IOS APP测试 9-其他混合测试',
    `sort`        int(5)     NOT NULL COMMENT '用例顺序号',
    `description` varchar(1000) DEFAULT NULL COMMENT '用例描述',
    `create_user` varchar(50)   DEFAULT NULL COMMENT '创建人',
    `create_date` datetime      DEFAULT NULL COMMENT '创建时间',
    `update_user` varchar(50)   DEFAULT NULL COMMENT '更新人',
    `update_date` datetime      DEFAULT NULL COMMENT '更新时间',
    `del_flag`    tinyint(1) NOT NULL COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='测试平台-测试用例';