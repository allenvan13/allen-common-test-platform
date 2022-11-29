package com.allen.testplatform.modules.databuilder.service;

/**
 * @author Fan QingChuan
 * @since 2022/4/11 10:39
 */
public interface ZxxjAlgorithmService {

    /**
     * 重置清空指定批次-模板-检查项打分 数据向上校验必填  例如 存在模板 则必须存在批次名称
     * @param batchName 批次名称
     * @param templateName 模板名称(展示名称)
     * @param checkItemName 检查项名称
     */
    void resetScore(String batchName,
                    String templateName,
                    String checkItemName);

    /**
     * 重置指定批次-模板 查验状态为未关闭-0  数据向上校验必填  例如 存在模板 则必须存在批次名称
     * @param batchName 批次名称
     * @param templateName 模板名称(展示名称)
     */
    void resetBatch(String batchName,
                    String templateName);

    /**
     * 单次打分:指定批次-模板-检查项-条件名称进行打分 匹配规则:除条件名称 其余需全匹配
     * @param batchName 批次名称 必填
     * @param templateName  模板名称 必填
     * @param checkItemName   检查项名称 必填
     * @param itemName  条件名称  非必填
     */
    void testScoreItem(
            String batchName,
            String templateName,
            String checkItemName,
            String itemName);

    /**
     * 单次打分&断言
     * @param batchName 批次名称 必填
     * @param templateName  模板名称 必填
     * @param checkItemName   检查项名称 必填
     * @param itemName  条件名称  非必填
     */
    void testAssertScoreItem(
            String batchName,
            String templateName,
            String checkItemName,
            String itemName);

    /**
     * 批量打分&断言 指定批次-模板-检查项 中已打分/未打分的检查项进行打分  数据向上校验必填  例如 存在模板 则必须存在批次名称
     * @param batchName  批次名称
     * @param templateName   模板名称
     * @param checkItemName  检查项名称
     * @param hasBeenScored   是否已打分  选填 默认全量  null -> 全量
     */
    void testAssertScoreBatch(String batchName,
                              String templateName,
                              String checkItemName,
                              Boolean hasBeenScored);

    /**
     * 批量打分 指定批次-模板-检查项 中已打分/未打分的检查项进行打分
     * @param batchName  批次名称
     * @param templateName   模板名称
     * @param checkItemName  检查项名称
     * @param hasBeenScored   是否已打分  选填 默认全量  null -> 全量
     */
    void testScoreBatch(String batchName,
                        String templateName,
                        String checkItemName,
                        Boolean hasBeenScored);
}
