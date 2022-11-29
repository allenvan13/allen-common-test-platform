package com.allen.testplatform.common.dto;

import cn.nhdc.common.enums.CodeConstants;
import cn.nhdc.common.enums.IEnum;
import lombok.Data;

@Data
public class ResponseData<T> {

    /**
     * 状态
     */
    private boolean status = true;

    /**
     * 错误编码
     */
    private String code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 数据体
     */
    protected T body;

    /**
     * @param status
     * @param message
     * @param body
     * @param code
     */
    public ResponseData(boolean status, String message, T body, IEnum<String> code) {
        this.status = status;
        this.message = message;
        this.body = body;
        if (code == null) {
            this.code = status ? CodeConstants.SUCCESS.toString() : CodeConstants.FAIL.toString();
        } else {
            this.code = code.getValue();
        }
    }

    /**
     * 无参构造
     */
    public ResponseData() {

    }

    /**
     * 设置错误信息
     *
     * @param code
     * @param message
     */
    public void setError(String code, String message) {
        this.code = code;
        this.message = message;
        this.status = false;
    }

    /**
     * 设置错误信息
     *
     * @param msg
     * @param msg
     */
    public void setError(String msg) {
        this.status = false;
        this.message = msg;
    }

    /**
     * 通用失败
     *
     * @return
     */
    public static ResponseData fail() {
        return new ResponseData(false, "操作失败", null, CodeConstants.FAIL);
    }
    /**
     * 通用失败
     *
     * @return
     */
    public static ResponseData fail(Object body) {
        return new ResponseData(false, "操作失败", body, CodeConstants.FAIL);
    }


    public static ResponseData fail(String message, IEnum<String> code) {
        return new ResponseData(false, message, null, code);
    }

    /**
     * 带参数的通用失败
     *
     * @return
     */
    public static ResponseData fail(String message) {
        return new ResponseData(false, message, null, CodeConstants.FAIL);
    }

    /**
     * 通用成功
     *
     * @return
     */
    public static ResponseData success() {
        return new ResponseData(true, "操作成功", null, CodeConstants.SUCCESS);
    }

    /**
     * 通用成功
     *
     * @return
     */
    public static ResponseData success(Object data) {
        return new ResponseData(true, "操作成功", data, CodeConstants.SUCCESS);
    }

    /**
     * 通用成功
     *
     * @return
     */
    public static ResponseData success(String message, Object data) {
        return new ResponseData(true, message, data, CodeConstants.SUCCESS);
    }


}

