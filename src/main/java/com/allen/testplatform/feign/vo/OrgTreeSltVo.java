package com.allen.testplatform.feign.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wangpengngui
 * @since 2020-07-23 16:34:03
 */
@Data
public class OrgTreeSltVo implements Serializable {

    private static final long serialVersionUID = 2143660015943444760L;
    private String id;
    private String code;
    private String name;
    private List<OrgTreeSltVo> children;

}
