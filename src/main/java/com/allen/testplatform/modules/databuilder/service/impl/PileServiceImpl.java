package com.allen.testplatform.modules.databuilder.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.constant.HostCommon;
import com.allen.testplatform.modules.databuilder.enums.RoleTypeEnum;
import com.allen.testplatform.common.utils.*;
import com.allen.testplatform.config.CurrentEnvironmentConfig;
import com.allen.testplatform.modules.databuilder.mapper.PileMapper;
import com.allen.testplatform.modules.databuilder.mapper.ProcessV2Mapper;
import com.allen.testplatform.modules.databuilder.mapper.UserCenterMapper;
import com.allen.testplatform.modules.databuilder.model.common.SectionInfo;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.modules.databuilder.model.pile.DetailQueryVO;
import com.allen.testplatform.modules.databuilder.model.pile.PileDetailsVO;
import com.allen.testplatform.modules.databuilder.service.PileService;
import com.allen.testplatform.testscripts.api.ApiPile;
import cn.nhdc.common.exception.BusinessException;
import cn.nhdc.common.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Fan QingChuan
 * @since 2022/5/18 15:09
 */
@Slf4j
@Service
public class PileServiceImpl implements PileService {

    @Resource
    private PileMapper pileMapper;

    @Resource
    private ProcessV2Mapper processMapper;

    @Resource
    private UserCenterMapper ucMapper;

    @Resource
    private CurrentEnvironmentConfig currentEnv;

    @Resource
    @Qualifier(value = "callerRunsThreadPoolTaskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    @Override
    public void submitBatchDetail(String checkTypeName,String pileAreaName,String reportName,String acceptorName,String sectionName,String ccorName,Integer pictureNum,Double pointX,Double pointY,String pileSn,Integer testCount,Integer commitType) {

        UcUser reporter = getSectionReportUsers(sectionName,reportName);

        Map<String,String> reportHeader = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(reporter.getSource().equals(Constant.SUPPLIER_SOURCE) ? reporter.getPhone() : reporter.getUserName(), EncryptUtils.decrypt(reporter.getPassword()),currentEnv.getENV()));

        for (int i = 0; i < testCount; i++) {
            taskExecutor.execute(() -> {
                PileDetailsVO pileDetailsVO = buildDetailVo(checkTypeName, pileAreaName, acceptorName, sectionName, ccorName, pictureNum, pointX, pointY, pileSn);
                String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiPile.SAVE_DETAIL), reportHeader, JSONObject.toJSONString(pileDetailsVO));
                log.info("{}" , JSON.parseObject(rs));
                if (commitType > 1) {
                    Long detailId = null;
                    try {
                        detailId = Long.valueOf(JsonPath.read(rs, "$.body").toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (ObjectUtil.isNotEmpty(detailId)) {
                        pileDetailsVO.setId(detailId);
                        String params = "id=".concat(pileDetailsVO.getId().toString());
                        String rs2 = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiPile.COMMIT_PILE_DETAIL), reportHeader, params);
                        log.info("{}" , JSON.parseObject(rs2));
                    }else {
                        log.info("????????????,???????????????ID");
                    }
                }
            });
        }
    }

    public PileDetailsVO buildDetailVo(String checkTypeName,String pileAreaName,String acceptorName,String sectionName,String ccorName,Integer pictureNum,Double pointX,Double pointY,String pileSn){

        if (ObjectUtil.isEmpty(sectionName)) {
            throw new BusinessException("????????????????????????!");
        }

        PileDetailsVO detailsVO = new PileDetailsVO();
        List<PileDetailsVO.PersonnelDTO> acceptor = new ArrayList<>();
        List<PileDetailsVO.PersonnelDTO> ccor = new ArrayList<>();

        //?????? ??????????????? ???????????? ???????????????1???
        setCheckType(checkTypeName,detailsVO);

        //?????? ??????????????????????????? ???????????? ???????????????1???
        setPileArea(sectionName,pileAreaName,detailsVO);

        //???????????????????????????
        List<JSONObject> pointList = pileMapper.getPoint(detailsVO.getTypeId());
        List<PileDetailsVO.DetailsPointDTO> detailsPointDTOList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(pointList)) {
            pointList.forEach(i -> {
                detailsPointDTOList.add(PileDetailsVO.DetailsPointDTO.builder()
                        .id(i.getLong("pointId"))
                        .title(i.getString("title"))
                        .remark(i.getString("remark"))
                        .picture(TestDataUtils.getPicture(ObjectUtil.isNotEmpty(pictureNum) ? pictureNum : RandomUtil.randomInt(6))).build());
            });
        }

        //????????????????????? ???????????? ????????????
        if (StringUtils.isNotEmpty(acceptorName)){
            acceptor = getSectionUser(acceptorName , sectionName);
        }

        //??????????????? ???????????? ????????????
        if (StringUtils.isNotEmpty(ccorName)){
            ccor = getSectionUser(ccorName ,sectionName);
        }

        //?????????????????? X Y?????? ????????????
        detailsVO.setPointX(ObjectUtil.isNotEmpty(pointX) ? pointX : (float) RandomUtil.randomDouble(0, 0.999999999999));
        detailsVO.setPointY(ObjectUtil.isNotEmpty(pointY) ? pointY : (float) RandomUtil.randomDouble(0, 0.999999999999));
        detailsVO.setPileSn(ObjectUtil.isNotEmpty(pileSn) ? pileSn : TestDataUtils.getRandomStrNum("???????????????-????????????: ",100000));
//        detailsVO.setPileSn(ObjectUtil.isNotEmpty(pileSn) ? TestDataUtils.getRandomStrNum(pileSn,100000) : TestDataUtils.getRandomStrNum("???????????????-????????????: ",100000));

        detailsVO.setDetailsPoint(detailsPointDTOList);
        detailsVO.setAcceptor(acceptor);
        detailsVO.setCcor(ccor);

        return detailsVO;
    }

    @Override
    public void deleteDetails(String orgName,String projectName,String stageName,String sectionName,String partName,String typePath,String pileSn,String createUserName,Integer commitType) {

        Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxCheckAuthToken("ATE001", "a123456", Constant.UAT_ENV));

        DetailQueryVO detailQueryVO = new DetailQueryVO();
        detailQueryVO.setOrgName(orgName);
        detailQueryVO.setProjectName(projectName);
        detailQueryVO.setStageName(stageName);
        detailQueryVO.setSectionName(sectionName);
        detailQueryVO.setPartName(partName);
        detailQueryVO.setTypePath(typePath);
        detailQueryVO.setPileSn(pileSn);
        detailQueryVO.setCreateUserName(createUserName);
        detailQueryVO.setCommitType(commitType);

        List<Long> idList = pileMapper.getDetailIdList(detailQueryVO, null);

        if (CollectionUtils.isNotEmpty(idList)) {
            idList.forEach(id -> {
                taskExecutor.execute(() -> {
                    try {
                        String params = "?id="+id;
                        String rs = HttpUtils.doPost(HostCommon.UAT.concat(ApiPile.DELETE_PILE_DETAIL_BY_ID).concat(params), header, null);
                        log.info("{}",JSON.parseObject(rs));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        }else {
            throw new BusinessException("??????????????????????????????");
        }

    }

    /**
     * ?????????????????????
     */
    public UcUser getSectionReportUsers(String sectionName,String reportName){
        if (ObjectUtil.isEmpty(sectionName)) {
            throw new BusinessException("????????????????????????");
        }
        SectionInfo sectionInfo = processMapper.getSectionInfo(sectionName,null,null);
        Integer sectionType = sectionInfo.getSectionType();
        List<String> companyList = new ArrayList<>();
        companyList.addAll(sectionInfo.getConstructionGuid());
        companyList.add(sectionInfo.getContractorGuid());
        companyList.add(sectionInfo.getSupervisorGuid());
        List<UcUser> reportCompanyUserList = ucMapper.getSupplierUsersByList(companyList);
        String category = setCategory(sectionType);
        List<UcUser> managerUserList = ucMapper.getUsersByIdList(processMapper.getBatchUserId(sectionInfo.getStageCode(), RoleTypeEnum.ManageUser.getRoleCode(), category), Constant.PS_SOURCE);
        reportCompanyUserList.addAll(managerUserList);

        UcUser reporter;
        if (ObjectUtil.isNotEmpty(reportName)) {
            reporter = reportCompanyUserList.stream().filter(user -> user.getRealName().contains(reportName)).findAny().orElse(reportCompanyUserList.get(RandomUtil.randomInt(reportCompanyUserList.size())));
        }else {
            reporter = reportCompanyUserList.get(RandomUtil.randomInt(reportCompanyUserList.size()));
        }
        return  reporter;
    }

    //?????? ??????????????? ???????????? ???????????????1???
    private void setCheckType(String checkTypeName,PileDetailsVO detailsVO){
        List<JSONObject> checkTypeList = pileMapper.getPileCheckType();
        JSONObject target = null;
        if (ObjectUtil.isNotEmpty(checkTypeName)) {
            target = checkTypeList.stream().filter(i -> i.getString("typeName").contains(checkTypeName)).findAny().orElse(null);
            if (ObjectUtil.isNotNull(target)) {
                log.info("?????????????????? : "+target.getString("typeName"));
            }else {
                target = checkTypeList.get(RandomUtil.randomInt(checkTypeList.size()));
                log.info("??????????????????????????????, ???????????????????????? : "+target.getString("typeName"));
            }
        }else {
            // ?????? ???????????????
            target = checkTypeList.get(RandomUtil.randomInt(checkTypeList.size()));
            log.info("??????????????????, ???????????????????????? : "+target.getString("typeName"));
        }
        detailsVO.setTypeId(target.getLong("typeId"));
        detailsVO.setTypeName(target.getString("typeName"));
        detailsVO.setTypePath(target.getString("typePath"));
    }

    //?????? ??????????????????????????? ???????????? ???????????????1???
    private void setPileArea(String sectionName,String pileAreaName,PileDetailsVO detailsVO){
        List<JSONObject> pileSection = pileMapper.getPileSection(sectionName);
        if (ObjectUtil.isEmpty(pileSection)) {
            throw new BusinessException("??????????????????,??????????????????????????????");
        }
        if (StringUtils.isNotEmpty(pileAreaName)){
            List<JSONObject> pileAreaInfos = pileSection.stream().filter(i -> i.getString("banName").contains(pileAreaName)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(pileAreaInfos)) {
                JSONObject pileArea = pileAreaInfos.get(RandomUtil.randomInt(pileAreaInfos.size()));
                detailsVO.setBanCode(pileArea.getString("banCode"));
                detailsVO.setBanName(pileArea.getString("banName"));
                detailsVO.setSectionId(pileArea.getLong("sectionId"));
                detailsVO.setDrawing(pileArea.getString("picture"));
            }else {
                throw new BusinessException("????????????pileAreaName?????????");
            }
        }else {
            // ?????? ???????????????
            int index = RandomUtil.randomInt(pileSection.size());
            detailsVO.setBanCode(pileSection.get(index).getString("banCode"));
            detailsVO.setBanName(pileSection.get(index).getString("banName"));
            detailsVO.setSectionId(pileSection.get(index).getLong("sectionId"));
            detailsVO.setDrawing(pileSection.get(index).getString("picture"));
        }
    }

    //????????????????????? ??????????????????????????????
    private List<PileDetailsVO.PersonnelDTO> getSectionUser(String userName,String sectionName){

        JSONObject sectionInfo = pileMapper.getSectionCompany(sectionName);
        List<String> sectionCompany = Arrays.asList(sectionInfo.getString("constructionGuid"),
                sectionInfo.getString("contractorGuid"),
                sectionInfo.getString("supervisorGuid"));

        List<JSONObject> supplierListUser = ucMapper.getSupplierListUser(sectionCompany);
        List<JSONObject> userList = supplierListUser.stream().filter(i -> i.getString("realName").contains(userName)).collect(Collectors.toList());

        List<PileDetailsVO.PersonnelDTO> personnelDTOList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(userList)) {
            userList.forEach(i -> {
                PileDetailsVO.PersonnelDTO personnelDTO = new PileDetailsVO.PersonnelDTO();
                personnelDTO.setUserId(i.getLong("userId"));
                personnelDTO.setRealName(i.getString("realName"));
                personnelDTO.setCompanyGuid(i.getString("providerGuid"));
                personnelDTO.setCompanyName(i.getString("providerName"));
                personnelDTOList.add(personnelDTO);
            });
        }else {
            log.info("???????????????????????????!");
        }
        return personnelDTOList;
    }

    public String setCategory(int sectionType) {
        String category;
        switch (sectionType) {
            case 1:
                category = Constant.GCJC;
                break;
            case 2:
                category = Constant.ZSJC;
                break;
            case 3:
                category = Constant.JGJC;
                break;
            default:
                throw new IllegalStateException("??????????????????! sectionType:" + sectionType);
        }
        return category;
    }
}
