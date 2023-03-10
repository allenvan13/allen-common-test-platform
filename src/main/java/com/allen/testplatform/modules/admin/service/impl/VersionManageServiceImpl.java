package com.allen.testplatform.modules.admin.service.impl;

import cn.hutool.core.text.StrSplitter;
import cn.hutool.core.util.ObjectUtil;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.enums.TestTeamEnum;
import com.allen.testplatform.common.enums.TestTerminalEnum;
import com.allen.testplatform.common.enums.TestVersionEnum;
import com.allen.testplatform.common.utils.DateUtils;
import com.allen.testplatform.common.utils.DownloadUtils;
import com.allen.testplatform.common.utils.MyRuntimeUtil;
import com.allen.testplatform.config.OSSConfig;
import com.allen.testplatform.modules.admin.mapper.VersionManageMapper;
import com.allen.testplatform.modules.admin.model.dto.VersionManageDto;
import com.allen.testplatform.modules.admin.model.entity.VersionManage;
import com.allen.testplatform.modules.admin.model.vo.VersionQueryVo;
import com.allen.testplatform.modules.admin.service.VersionManageService;
import cn.nhdc.common.exception.BusinessException;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoleilu.hutool.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/5/25 17:35
 */
@Slf4j
@DS("test")
@Service("VersionManageService")
public class VersionManageServiceImpl extends ServiceImpl<VersionManageMapper, VersionManage> implements VersionManageService {

    public static final String SUFFIX_IOS = ".ipa";
    public static final String SUFFIX_ANDROID = ".apk";

    @Resource
    private OSSConfig ossConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long uploadFiles(String versionCode,String versionContent,String versionType,
                               String teamName, String terminalName, Boolean hasReleased, String releaseTime,
                               String developer, String tester, MultipartFile file) {

        //??????
        TestTeamEnum testTeamEnum = TestTeamEnum.getInstance(teamName);
        if (ObjectUtil.isEmpty(testTeamEnum)) {
            throw new BusinessException("???????????????????????????????????????");
        }

        //????????????
        TestTerminalEnum terminalEnum = TestTerminalEnum.getInstance(terminalName);
        String suffix;
        if (ObjectUtil.isEmpty(terminalEnum)) {
            throw new BusinessException("??????????????????????????????????????????");
        }
        if (terminalEnum.getTerminalCode().equalsIgnoreCase("JX_APP_IOS")) {
            suffix = SUFFIX_IOS;
        }else {
            suffix = SUFFIX_ANDROID;
        }

        //??????
        TestVersionEnum testVersionEnum = TestVersionEnum.getInstance(versionType);
        if (ObjectUtil.isEmpty(testVersionEnum)) {
            throw new BusinessException("??????????????????code?????????????????????");
        }

        if (ObjectUtil.isEmpty(hasReleased)) {
            throw new BusinessException("??????????????? ????????????");
        }

        if (hasReleased) {
            if (ObjectUtil.isEmpty(releaseTime)) {
                throw new BusinessException("?????????! ???????????? ????????????");
            }
        }

        //??????????????? ???????????????????????? ???????????????false
        //????????? ????????????????????????????????????   ???????????????????????????,?????????????????????????????????
        if (hasReleased) {
            LambdaQueryWrapper<VersionManage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(VersionManage::getTeamCode, testTeamEnum.getTeamCode())
                    .eq(VersionManage::getTerminalCode, terminalEnum.getTerminalCode())
                    .eq(VersionManage::getVersionType,versionType)
                    .eq(VersionManage::getDelFlag,false)
                    .eq(VersionManage::getLatestVersionFlag,true);

            VersionManage currentVersion = baseMapper.selectOne(wrapper);

            if (ObjectUtil.isNotEmpty(currentVersion)) {

                if (DateUtils.compare(DateUtils.parseDate(releaseTime), DateUtils.parseDate(currentVersion.getReleaseTime()),"yyyy-MM-dd") < 0 ) {
                    throw new BusinessException("?????????????????? ?????? ???????????????????????? ?????????!");
                }

                //??????????????????????????????false
                currentVersion.setLatestVersionFlag(false);
                updateById(currentVersion);
            }
        }


        String ossKey = Constant.VERSION_PATH + testTeamEnum.getTeamCode() + "/" +
                terminalEnum.getTerminalCode()+"-"+testVersionEnum.getCode()+"-"+versionCode+"-"+new Date().getTime()+suffix;
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            log.error("???????????????", e);
            throw new BusinessException("????????????" + e.getMessage());
        }
        //????????????OSS
        String url = ossConfig.putObject(ossKey, inputStream);

        //????????????????????????
        removeByCode(testTeamEnum.getTeamCode(),terminalEnum.getTerminalCode(),versionCode,testVersionEnum.getCode());

        long id = IdWorker.getId();
        VersionManage versionManage = new VersionManage();
        versionManage.setId(id);
        versionManage.setDeveloper(developer);
        versionManage.setTester(tester);
        versionManage.setTeamCode(testTeamEnum.getTeamCode());
        versionManage.setTeamName(testTeamEnum.getTeamName());
        versionManage.setTerminalCode(terminalEnum.getTerminalCode());
        versionManage.setTerminalName(terminalEnum.getTerminalName());
        versionManage.setFileName(file.getOriginalFilename());
        versionManage.setOssKey(ossKey);
        versionManage.setUrl(url);
        versionManage.setVersionCode(versionCode);
        versionManage.setVersionContent(versionContent);
        versionManage.setVersionType(testVersionEnum.getCode());
        versionManage.setHasReleased(hasReleased);
        if (hasReleased) {
            versionManage.setReleaseTime(releaseTime);
            versionManage.setLatestVersionFlag(true);
        }else {
            versionManage.setLatestVersionFlag(false);
        }

        //??????
        save(versionManage);
        return id;
    }

    @Override
    public IPage<VersionManageDto> getVersionPage(VersionQueryVo versionQueryVo) {
        IPage<VersionManage> p = new Page<>(versionQueryVo.getCurrent(), versionQueryVo.getSize());
        QueryWrapper<VersionManage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag",false);
        queryWrapper.orderByDesc(true,"create_date");
        if (ObjectUtil.isNotEmpty(versionQueryVo.getTeamCode())) queryWrapper.eq("team_code",versionQueryVo.getTeamCode());
        if (ObjectUtil.isNotEmpty(versionQueryVo.getTerminalCode())) queryWrapper.eq("terminal_code",versionQueryVo.getTerminalCode());
        if (ObjectUtil.isNotEmpty(versionQueryVo.getTeamName())) queryWrapper.eq("team_name",versionQueryVo.getTeamName());
        if (ObjectUtil.isNotEmpty(versionQueryVo.getTerminalName())) queryWrapper.eq("terminal_name",versionQueryVo.getTerminalName());

        if (ObjectUtil.isNotEmpty(versionQueryVo.getLatestVersionFlag())) queryWrapper.eq("latest_version_flag",versionQueryVo.getLatestVersionFlag());
        if (ObjectUtil.isNotEmpty(versionQueryVo.getHasReleased())) queryWrapper.eq("has_released",versionQueryVo.getHasReleased());

        if (ObjectUtil.isNotEmpty(versionQueryVo.getVersionCode())) queryWrapper.like("version_code",versionQueryVo.getVersionCode());
        if (ObjectUtil.isNotEmpty(versionQueryVo.getVersionContent())) queryWrapper.like("version_content",versionQueryVo.getVersionContent());
        if (ObjectUtil.isNotEmpty(versionQueryVo.getVersionType())) queryWrapper.eq("version_type",versionQueryVo.getVersionType());
        if (ObjectUtil.isNotEmpty(versionQueryVo.getReleaseTime())) queryWrapper.like("release_time",versionQueryVo.getReleaseTime());
        if (ObjectUtil.isNotEmpty(versionQueryVo.getDeveloper()))  queryWrapper.like("developer",versionQueryVo.getDeveloper());
        if (ObjectUtil.isNotEmpty(versionQueryVo.getTester()))  queryWrapper.like("tester",versionQueryVo.getTester());

        IPage<VersionManage> versionManageIPage = baseMapper.selectPage(p, queryWrapper);

        IPage<VersionManageDto> page = new Page<>(versionQueryVo.getCurrent(), versionQueryVo.getSize());
        List<VersionManageDto> versionManageDtoList =  new ArrayList<>(versionQueryVo.getSize());

        versionManageIPage.getRecords().forEach(version -> {
            VersionManageDto dto = new VersionManageDto();
            BeanUtil.copyProperties(version,dto);
            dto.setCreateDate(DateUtils.formatDateTime(version.getCreateDate()));
            versionManageDtoList.add(dto);
        });

        page.setRecords(versionManageDtoList);
        page.setTotal(versionManageIPage.getTotal());
        page.setPages(versionManageIPage.getPages());
        return page;

    }

    public VersionManage getTargetVersion(String versionCode,String versionType,String teamCode,String terminalCode) {
        LambdaQueryWrapper<VersionManage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VersionManage::getTeamCode, teamCode)
                .eq(VersionManage::getTerminalCode, terminalCode)
                .eq(VersionManage::getVersionCode,versionCode)
                .eq(VersionManage::getVersionType,versionType)
                .eq(VersionManage::getDelFlag,false);
        VersionManage versionManage = baseMapper.selectOne(wrapper);
        if (ObjectUtil.isEmpty(versionManage)) {
            throw new BusinessException("??????????????????????????????!");
        }
        return versionManage;
    }

    /**
     * windows??????????????? ??????????????????apk and ??????(????????????)??????apk
     * download the target version apk from alibaba OSS server and then install apk to mobile phone  in windows system server
     * ?????? ?????????????????? adb shell???????????????
     * @param versionCode
     * @param versionType
     * @param teamCode
     * @param terminalCode
     * @param phoneSerialNumber ???????????????
     */
    public void installAndroidApk(String versionCode,String versionType,String teamCode,String terminalCode, String phoneSerialNumber) {
        //???????????????
        VersionManage targetVersion = getTargetVersion(versionCode, versionType, teamCode, terminalCode);
        //??????????????????
        String url = targetVersion.getUrl();
        List<String> strings = StrSplitter.split(url, "/", -1, true, true);
        String filePath = "D:\\download\\download\\".concat(strings.get(strings.size()-1));
        //???????????????
        DownloadUtils.downloadFileToDirectory(url,filePath);
        //??????apk
        String command = "adb -s "+phoneSerialNumber+ " install " + filePath;
        MyRuntimeUtil.exec(command);
    }

    private void removeByCode(String teamCode,String terminalCode,String versionCode, String versionType) {
        LambdaQueryWrapper<VersionManage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VersionManage::getTeamCode, teamCode)
                .eq(VersionManage::getTerminalCode, terminalCode)
                .eq(VersionManage::getVersionCode,versionCode)
                .eq(VersionManage::getVersionType,versionType)
                .eq(VersionManage::getDelFlag,false);
        remove(wrapper);
    }

}
