package com.allen.testplatform.modules.admin.service;

import com.allen.testplatform.modules.admin.model.dto.VersionManageDto;
import com.allen.testplatform.modules.admin.model.entity.VersionManage;
import com.allen.testplatform.modules.admin.model.vo.VersionQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Fan QingChuan
 * @since 2022/5/25 17:35
 */
public interface VersionManageService extends IService<VersionManage> {


    long uploadFiles(String versionCode,String versionContent,String versionType,
                        String teamName, String terminalName,Boolean hasReleased,String releaseTime,
                        String developer, String tester, MultipartFile file);

    IPage<VersionManageDto> getVersionPage(VersionQueryVo versionQueryVo);

}
