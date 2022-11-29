package com.allen.testplatform.common.listener;


import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DataEasyExcelListener<T> extends AnalysisEventListener {

	private List<T> list = new ArrayList<>();

	@Override
	public void invoke(Object data, AnalysisContext analysisContext) {
		log.info("解析到一条数据:{}", JSON.toJSONString(data));
	}

	@Override
	public void doAfterAllAnalysed(AnalysisContext analysisContext) {
		log.info("所有数据解析完成！");
	}

	@Override
	public void onException(Exception exception, AnalysisContext context) {
		log.error("解析失败，但是继续解析下一行:{}", exception.getMessage());
	}

	public List<T> getData() {
		return list;
	}

}