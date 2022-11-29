package com.allen.testplatform.common.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.enums.TestTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.*;

/**
 * @author Fan QingChuan
 * @since 2022/3/14 9:28
 */
public class TestDataUtils {

    //根据所需数量 生成图片链接List
    public static List<String> getPicture(int num){
        List<String> sourcePicture = Arrays.asList(
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/165024946140289.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/165024946140377.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/165024946140457.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/165024946140344.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/165024946140497.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/165024946140473.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/165024946140331.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164690642057095.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164690642071580.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164690642103349.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164791559757879.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164749954957734.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164618977221477.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164618977221214.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164618977221442.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164690642089727.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164690641977232.jpg",
                "http://nhfcloms.oss-cn-beijing.aliyuncs.com/NewHopeQC/iOS/Question/90FF679C-9744-4872-8B09-27B75A8B4DEB.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/16469064202293.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164791559716725.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164791559740581.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/165024946140289.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/165024946140377.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/165024946140457.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/165024946140344.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/165024946140497.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/165024946140473.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/165024946140331.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164690642057095.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164690642071580.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164690642103349.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164791559757879.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164749954957734.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164618977221477.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164618977221214.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164618977221442.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164690642089727.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164690641977232.jpg",
                "http://nhfcloms.oss-cn-beijing.aliyuncs.com/NewHopeQC/iOS/Question/90FF679C-9744-4872-8B09-27B75A8B4DEB.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/16469064202293.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164791559716725.jpg",
                "http://nhdc-jx-dev.oss-cn-beijing.aliyuncs.com/NewHopeJXNew/android/Question/164791559740581.jpg"
        );

        Collections.shuffle(sourcePicture);

        if (num > 0 && num <= sourcePicture.size()) {
            return CollectionUtil.sub(sourcePicture,0,num);
        }else return CollectionUtil.sub(sourcePicture,0,3);
    }

    //生成随机编号数据 类似 ABC253753647362
    public static String getRandomStrNum(String preName,int num){
        if (StringUtils.isNull(preName)) {
            return num > 0 ? String.valueOf(RandomUtil.randomInt(num)):String.valueOf(RandomUtil.randomInt(2000));
        }else {
            return num > 0 ? preName.concat(String.valueOf(RandomUtil.randomInt(num))):preName.concat(String.valueOf(RandomUtil.randomInt(2000)));
        }
    }

    public static String getUiTestContent(String businessType,String processName) {
        return Constant.UI_AUTO_TEST +Constant.EXPORT_CASE + businessType +"_" + processName +"_"+ DateUtils.getTimeSuffix();
    }

    public static String getApiTestContent(String businessType,String processName) {
        return Constant.API_AUTO_TEST +Constant.EXPORT_CASE + businessType +"_" + processName +"_"+ DateUtils.getTimeSuffix();
    }

    /**
     * 拼接测试content
     * @param testType 测试用例类型  1-接口测试 2-Web测试 3-安卓APP测试 4-IOS APP测试 9-其他混合测试
     * @param businessType 业务类型 接受自定义
     * @param processName 业务流程节点名称 接收自定义
     * @param otherStrings 其他需拼接的字符串 接收多个
     * @return
     */
    public static String getTestContent(Integer testType,String businessType,String processName,String ...otherStrings) {
        TestTypeEnum typeEnum = TestTypeEnum.getInstance(testType);
        if (typeEnum == null) {
            throw new IllegalArgumentException("测试类型不合法! 1-接口测试 2-Web测试 3-安卓APP测试 4-IOS APP测试 9-其他混合测试");
        }
        StringBuilder stringBuilder =  new StringBuilder(typeEnum.getPrefix());
        stringBuilder.append(Constant.EXPORT_CASE).append(businessType).append("_").append(processName).append("_");

        for (String string : otherStrings) {
            stringBuilder.append(string).append("_");
        }
        return stringBuilder.append(DateUtils.getTimeSuffix()).toString();
    }

    /**
     * List<json>转List<Map>
     */
    public static List<Map<String,String>> jsonListToMapList(List<JSONObject> target){
        List<Map<String,String>> result = new ArrayList<>();
        Map temp;
        for (int i = 0; i < target.size(); i++) {
            temp = (Map) JSONObject.parseObject(JSON.toJSONString(target.get(i)));
            result.add(i,temp);
        }
        return result;
    }
}
