package com.allen.testplatform.modules.admin.controller;

import com.allen.testplatform.modules.admin.model.dto.VersionManageDto;
import com.allen.testplatform.modules.admin.model.vo.VersionQueryVo;
import com.allen.testplatform.modules.admin.service.VersionManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Fan QingChuan
 * @since 2022/5/25 17:34
 */

@Slf4j
@RestController
@RequestMapping("/version")
@Validated
public class VersionManageController {

    @Resource
    private VersionManageService versionManageService;

    @PostMapping("/upload")
    public long uploadTemplate(@RequestParam @NotBlank(message = "缺少版本号参数") String versionCode,
                               @RequestParam @NotBlank(message = "缺少版本内容参数") String versionContent,
                               @RequestParam @NotBlank(message = "缺少版本类型参数") String versionType,
                               @RequestParam @NotBlank(message = "缺少开发团队名称参数") String teamName,
                               @RequestParam @NotBlank(message = "缺少被测客户端名称参数") String terminalName,
                               @RequestParam @NotNull(message = "是否已发版 参数不能为空") Boolean hasReleased,
                               @RequestParam String releaseTime,
                               @RequestParam String developer,
                               @RequestParam String tester,
                               @RequestParam @NotNull(message = "缺少参数file") MultipartFile file) {

        return versionManageService.uploadFiles(versionCode, versionContent,versionType,teamName,terminalName,hasReleased,releaseTime,developer,tester,file);
    }

    @GetMapping("/page")
    public IPage<VersionManageDto> getVersionPage(@RequestBody @Validated VersionQueryVo versionQueryVo) {
        return versionManageService.getVersionPage(versionQueryVo);
    }

}
