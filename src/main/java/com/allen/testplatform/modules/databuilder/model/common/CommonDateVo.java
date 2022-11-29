package com.allen.testplatform.modules.databuilder.model.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Fan QingChuan
 * @since 2021/12/14 19:13
 */
@Data
public class CommonDateVo implements Serializable {

    private static final long serialVersionUID = 678748307248894985L;
    /** 天数 */
    private long days ;
    /** 小时数 */
    private long hours ;
    /** 分钟数 */
    private long minutes;
    /** 秒数 */
    private long seconds;
    /** 天-小时-分钟-秒字符串 */
    private String dayStr;
}
