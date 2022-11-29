package com.allen.testplatform.modules.databuilder.model.process.entity;

import cn.nhdc.common.database.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("process_detail_issue")
public class ProcessDetailIssue extends BaseEntity<ProcessDetailIssue> {

    private static final long serialVersionUID = 795458478529305692L;

    /**
     * 验收流程id
     */
    @TableField("flow_id")
    private Long flowId;

    /**
     * 验收明细id
     */
    @TableField("detail_id")
    private Long detailId;

    /**
     * 问题编号
     */
    @TableField("code")
    private String code;

    /**
     * 数字编号
     */
    @TableField("serial_number")
    private Integer serialNumber;

    /**
     * 检查项id
     */
    @TableField("check_id")
    private Long checkId;

    /**
     * 检查项名称
     */
    @TableField("check_name")
    private String checkName;

    /**
     * 检查项路径名称
     */
    @TableField("check_path_name")
    private String checkPathName;

    /**
     * 楼栋编码
     */
    @TableField("ban_code")
    private String banCode;

    /**
     * 楼栋名称
     */
    @TableField("ban_name")
    private String banName;

    /**
     * 单元
     */
    @TableField("unit")
    private String unit;

    /**
     * 楼层
     */
    @TableField("floor")
    private String floor;

    /**
     * 房间编码
     */
    @TableField("room_code")
    private String roomCode;

    /**
     * 房间名称
     */
    @TableField("room_name")
    private String roomName;

    /**
     * 检验区域名称
     */
    @TableField("part_name")
    private String partName;

    /**
     * 户型图id
     */
    @TableField("house_type_id")
    private Long houseTypeId;

    /**
     * 问题位置X轴
     */
    @TableField("point_x")
    private Float pointX;

    /**
     * 问题位置Y轴
     */
    @TableField("point_y")
    private Float pointY;

    /**
     * 户型图
     */
    @TableField("check_image_url")
    private String checkImageUrl;

    /**
     * 严重程度(1:一般,2:重大,3:紧急)
     */
    @TableField("severity")
    private Integer severity;

    /**
     * 状态(1:待整改,2:重新整改,3:待复验,4:非正常关闭,5:正常关闭)
     */
    @TableField("status")
    private String status;

    /**
     * 补充说明
     */
    @TableField("remark")
    private String remark;

    /**
     * 整改期限
     */
    @TableField("deadline_day")
    private Integer deadlineDay;

    /**
     * 截止时间
     */
    @TableField("deadline")
    private Date deadline;

    /**
     * 整改人用户id
     */
    @TableField("rectify_user_id")
    private Long rectifyUserId;

    /**
     * 整改人用户姓名
     */
    @TableField("rectify_real_name")
    private String rectifyRealName;

    /**
     * 整改人岗位名称
     */
    @TableField("rectify_position_name")
    private String rectifyPositionName;

    /**
     * 整改人供应商公司guid
     */
    @TableField("rectify_company_guid")
    private String rectifyCompanyGuid;

    /**
     * 整改人供应商公司名字
     */
    @TableField("rectify_company_name")
    private String rectifyCompanyName;

    /**
     * 整改时间
     */
    @TableField("rectify_time")
    private Date rectifyTime;

    /**
     * 复验人用户id
     */
    @TableField("review_user_id")
    private Long reviewUserId;

    /**
     * 复验人用户姓名
     */
    @TableField("review_real_name")
    private String reviewRealName;

    /**
     * 复验时间
     */
    @TableField("review_time")
    private Date reviewTime;

    /**
     * 当前待办人用户id
     */
    @TableField("to_do_user_id")
    private Long toDoUserId;

    /**
     * 当前待办人用户姓名
     */
    @TableField("to_do_real_name")
    private String toDoRealName;


}
