package com.allen.testplatform.modules.databuilder.model.pile;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author
 * @description 桩基明细
 * @date 2021-07-12 15:47
 */
@Data
public class DetailQueryVO  implements Serializable {
    private Long id;
    private String orgCode;
    private String projectCode;
    private String stageCode;
    private Long sectionId;
    private String name;
    private String pileSn;
    private String createUserName;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date beginTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endTime;
    private Long typeId;
    private String TypeName;
    private String orgName;
    private String projectName;
    private String stageName;
    private String sectionName;
    private String  typePath;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createDate;
//    private List<DetailPointVO> points;
    private String partName;
    private Integer commitType;

}