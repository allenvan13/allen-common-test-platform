package com.allen.testplatform.modules.databuilder.model.feishu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 * 飞书应用消息 卡片模板实体
 * @author Fan QingChuan
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeishuCardMessage {

    private Config config;
    private Header header;
    private List<Element> elements;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Config{
        /**
         * true
         */
        private Boolean wide_screen_mode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Header{
        /**
         * green
         */
        private String template;
        private Text title;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Element{
        private String tag;
        private List<Button> actions;
        private Text text;
        private List<Field> fields;
        private List<Img> elements;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Img{
        private String tag;
        private String img_key;
        private Text alt;
        private String content;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Field{
        private Boolean is_short;
        private Text text;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Button{

        private String tag;
        private String url;
        private String type;
        private Text text;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Div{
        private String tag;
        private Text text;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Text{
        private String content;
        private String tag;
    }
}
