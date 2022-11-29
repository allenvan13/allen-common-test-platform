package com.allen.testplatform.modules.databuilder.model.pile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author sxf
 * @version 1.0.0
 * @Description TODO
 * @createTime 2021年07月08日 16:15:00
 */
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class PileDetailsVO {

	private Long id;
	/**
	 * 标段id
	 */
	@NotNull(message = "不能为空")
	private Long sectionId;
	/**
	 * 桩基类型id
	 */
	private Long typeId;
	/**
	 * 桩基类型名称
	 */
	private String typeName;
	/**
	 * 检查项全路径名称
	 */
	private String typePath;

	/**
	 * 桩号
	 */
	private String pileSn;
	/**
	 * 位置X轴
	 */
	private Double pointX;
	/**
	 * 位置Y轴
	 */
	private Double pointY;
	/**
	 * stageCode
	 */
	private String stageCode;
	/**
	 * 桩基区域
	 */
	private String banName;
	/**
	 * 桩基区域code
	 */
	private String banCode;
	/**
	 * 桩基图
	 */
	private String drawing;
	/**
	 * 验收点列表
	 */
	private List<DetailsPointDTO> detailsPoint;
	/**
	 * acceptor
	 */
	private List<PersonnelDTO> acceptor;
	/**
	 * ccor
	 */
	private List<PersonnelDTO> ccor;

	/**
	 * 验收点列表Item
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DetailsPointDTO {
		/**
		 * id
		 */
		private Long id;
		private String title;
		private String remark;
		/**
		 * picture
		 */
		private List<String> picture;
	}

	/**
	 * @author sxf
	 * @version 1.0.0
	 * @Description 人员dto
	 * @createTime 2021年07月07日 17:59:00
	 */
	@NoArgsConstructor
	@Data
	@Builder
	@AllArgsConstructor
	public static class PersonnelDTO {
		private Long userId;
		private String realName;
		private String companyGuid;
		private String companyName;
	}



}
