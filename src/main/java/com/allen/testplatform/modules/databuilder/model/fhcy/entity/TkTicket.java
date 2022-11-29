package com.allen.testplatform.modules.databuilder.model.fhcy.entity;

import cn.nhdc.common.database.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 工单表(com.allen.testplatform.modules.databuilder.model.fhcy.entity.TkTicket)实体类
 *
 * @author makejava
 * @since 2020-07-27 16:08:59
 */
@Data
@TableName("tk_ticket")
public class TkTicket extends BaseEntity<TkTicket> {

    /**  table:tk_ticket*/
    /**
     * 分户查验：XX.XXXXXXX.FHCY  承接查验：XX.XXXXXXX.CJCY  工程检查：XX.XXXXXXX.GCJC
     * 景观检查:XX.XXXXXXX.JGJC  装饰检查:XX.XXXXXXX.ZSJC
     */
    private String category;
    /**  table:tk_ticket*/
    /**
     * 问题编号
     */
    private String sn;
    /**
     * 问题所在点
     */
    private String location;
    /**  table:tk_ticket*/
    /**
     * 标题
     */
    private String title;
    /**  table:tk_ticket*/
    /**
     * 工单描述
     */
    private String content;
    /**  table:tk_ticket*/
    /**
     * 附件与图片
     */
    private String attachments;
    /**  table:tk_ticket*/
    /**
     * 是否超时 0:未超时 1：已超时
     */
    private Boolean ifExpire;
    /**  table:tk_ticket*/
    /**
     * 超时时间
     */
    private Date expireTime;

    /**  table:tk_ticket*/
    /**
     * 扩展信息
     */
    private Date complateTime;
    /**  table:tk_ticket*/
    /**
     * 00:未受理  01:已受理 10:已分配 11:处理中 21:处理完成 90:无效工单 91:非正常关闭 92:正常关闭  99:工单完成
     */
    private String status;
    /**  table:tk_ticket*/
    /**
     * 数据字典
     */
    private String importance;
    /**  table:tk_ticket*/
    /**
     * 是否重新激活,0:未重新激活 1：重新激活
     */
    private Boolean ifReactivate;
    /**  table:tk_ticket*/
    /**
     * 扩展信息
     */
    private String extensions;
    /**  table:tk_ticket*/
    /**
     * 城市公司
     */
    private String orgName;
    /**  table:tk_ticket*/
    /**
     * 城市公司编号
     */
    private String orgCode;
    /**  table:tk_ticket*/
    /**
     * 项目名称
     */
    private String projectName;
    /**  table:tk_ticket*/
    /**
     * 项目编号
     */
    private String projectCode;
    /**  table:tk_ticket*/
    /**
     * 项目分期名称
     */
    private String stageName;
    /**  table:tk_ticket*/
    /**
     * 项目分期编号
     */
    private String stageCode;
    /**  table:tk_ticket*/
    /**
     * 项目分期楼栋名称
     */
    private String banName;
    /**  table:tk_ticket*/
    /**
     * 项目分期楼栋编号
     */
    private String banCode;
    /**
     * 单元
     */
    private String unit;
    /**  table:tk_ticket*/
    /**
     * 创建人姓名
     */
    private String creatorName;
    /**  table:tk_ticket*/
    /**
     * 创建人
     */
    private String creator;
    /**  table:tk_ticket*/
    /**
     * 创建人ID
     */
    private String creatorId;
    /**  table:tk_ticket*/
    /**
     * 数据字典
     */
    private String source;
    /**  table:tk_ticket*/
    /**
     * 房间编号
     */
    private String roomCode;
    /**  table:tk_ticket*/
    /**
     * 房间名称
     */
    private String roomName;
    /**  table:tk_ticket*/
    /**
     * 承建单位
     */
    private String providerName;
    /**  table:tk_ticket*/
    /**
     * 承建单位GUID
     */
    private String providerGuid;
    /**  table:tk_ticket*/
    /**
     * 组织ID
     */
    private String departmentId;
    /**  table:tk_ticket*/
    /**
     * 组织名称
     */
    private String departmentName;

//2020-03-09新增
    /**
     * 责任人
     */
    private Long dutyUserId;
    /**
     * 责任人中文名
     */
    private String dutyUserName;
    /**
     * 方位类型
     */
    private String bearingType;
    /**
     * 部位id
     */
    private Long projectSiteId;
    /**
     * 部位名称
     */
    private String projectSiteName;
    /**
     * 批次id
     */
    private Long checkBatchId;

    /**
     * 退回次数
     */
    private Integer returnNum;
    /**
     * 是否发生退回
     */
    private Boolean ifReturn;

    /**
     * 唯一标记
     */
    private String checkSign;

    @Override
    protected Serializable pkVal() {
        return this.getId();
    }


}