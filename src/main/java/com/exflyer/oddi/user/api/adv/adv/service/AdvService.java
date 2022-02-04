package com.exflyer.oddi.user.api.adv.adv.service;

import com.exflyer.oddi.user.api.adv.adv.dao.AdvMapper;
import com.exflyer.oddi.user.api.adv.adv.dto.AdvAddReq;
import com.exflyer.oddi.user.api.adv.adv.dto.AdvPartnerRes;
import com.exflyer.oddi.user.api.adv.adv.dto.AdvProductPartnerRes;
import com.exflyer.oddi.user.api.adv.adv.dto.AdvProductSearchResult;
import com.exflyer.oddi.user.api.adv.adv.dto.AdvSearchReq;
import com.exflyer.oddi.user.api.adv.adv.dto.AdvSearchResult;
import com.exflyer.oddi.user.api.adv.adv.dto.MemberCompanyRes;
import com.exflyer.oddi.user.api.adv.adv.dto.PartnerConfigReq;
import com.exflyer.oddi.user.api.adv.adv.dto.PartnerFiles;
import com.exflyer.oddi.user.api.files.service.FileService;
import com.exflyer.oddi.user.api.user.account.dto.TermsReq;
import com.exflyer.oddi.user.enums.ApiResponseCodes;
import com.exflyer.oddi.user.exceptions.ApiException;
import com.exflyer.oddi.user.models.Adv;
import com.exflyer.oddi.user.models.AdvFile;
import com.exflyer.oddi.user.models.AdvPartner;
import com.exflyer.oddi.user.models.AdvPartnerPk;
import com.exflyer.oddi.user.models.AdvProduct;
import com.exflyer.oddi.user.models.AdvProductPk;
import com.exflyer.oddi.user.models.Code;
import com.exflyer.oddi.user.models.Member;
import com.exflyer.oddi.user.models.MemberCompany;
import com.exflyer.oddi.user.models.MemberTerms;
import com.exflyer.oddi.user.repository.AdvFileRepository;
import com.exflyer.oddi.user.repository.AdvPartnerRepository;
import com.exflyer.oddi.user.repository.AdvProductRepository;
import com.exflyer.oddi.user.repository.AdvRepository;
import com.exflyer.oddi.user.repository.CodeRepository;
import com.exflyer.oddi.user.repository.MemberCompanyRepository;
import com.exflyer.oddi.user.repository.jpa.MemberRepository;
import com.exflyer.oddi.user.repository.jpa.MemberTermsRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Component
@Slf4j
public class AdvService {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private AdvMapper advMapper;

    @Autowired
    private CodeRepository codeRepository;

    @Autowired
    private AdvRepository advRepository;

    @Autowired
    private AdvPartnerRepository advPartnerRepository;

    @Autowired
    private MemberCompanyRepository memberCompanyRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AdvFileRepository advFileRepository;

    @Autowired
    private MemberTermsRepository memberTermsRepository;

    @Autowired
    private AdvProductRepository advProductRepository;

    @Autowired
    private FileService fileService;

    public AdvSearchResult findAdvPartnerComm(AdvSearchReq req) {

        List<AdvPartnerRes> advPartnerResList =  advMapper.findAdvPartnerCode(req);
        List<Code> advTypeCodeOptional =  codeRepository.findByGroupCodeAndUsable("BST", true);

        List<PartnerConfigReq> partnerConfig =  advMapper.findPartnerConfig(req.getChannelType());
        List<PartnerFiles> fileList = advMapper.findDefaultAdvFiles(req.getChannelType());
        MemberCompanyRes memberCompanyRes = advMapper.findMemberCompany(req);

        return new AdvSearchResult(advPartnerResList,advTypeCodeOptional, partnerConfig, fileList, memberCompanyRes);
    }

    public AdvProductSearchResult findAdvPartnerProductComm(AdvSearchReq req) {

        List<AdvProductPartnerRes> advPartnerResList =  advMapper.findAdvPartnerProductCode(req);
        //묶음일 경우 슬롯수 1개 통일
        advPartnerResList.forEach(datas -> {
            datas.setTotalSlot(1);
        });

        List<Code> advTypeCodeOptional =  codeRepository.findByGroupCodeAndUsable("BST", true);
        List<PartnerConfigReq> partnerConfig =  advMapper.findPartnerConfig(req.getChannelType());
        List<PartnerFiles> fileList = advMapper.findDefaultAdvFiles(req.getChannelType());

        return new AdvProductSearchResult(advPartnerResList,advTypeCodeOptional, partnerConfig, fileList);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public Long save(AdvAddReq advAddReq) throws ApiException {
        return saveAd(advAddReq, "I");
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public Long modify(AdvAddReq advAddReq) throws ApiException {

        Optional<Adv>  advOptional = advRepository.findById(advAddReq.getAdvSeq());
        Adv adv = advOptional.orElseThrow(() -> new ApiException(ApiResponseCodes.NOT_FOUND));
        advAddReq.setProgressCode(adv.getProgressCode());

        //ADT001 대기, ADT002 승인, ADT003 보류
        if (adv == null) {throw new ApiException(ApiResponseCodes.NOT_FOUND);}
        if(!"ADT001".equals(adv.getAuditCode()) && !"ADT003".equals(adv.getAuditCode())) {
            throw new ApiException(ApiResponseCodes.AUDIT_NOT_FOUND);
        }
        return saveAd(advAddReq, "U");
    }

    private Long saveAd(AdvAddReq advAddReq, String saveType) throws ApiException {

        Adv adv = new Adv(advAddReq);

        //대기 또는 보류일 경우
        if(StringUtils.isBlank(adv.getAuditCode()) || "ADT003".equals(adv.getAuditCode())) {
            adv.setAuditCode("ADT001");
            adv.setSendCode("ADM001");//대기상태
        }

        List<MemberTerms> memberTerms = new ArrayList<>();
        if(!CollectionUtils.isEmpty(memberTerms)) {
            for(TermsReq term: advAddReq.getTerms()) {
                memberTerms.add(new MemberTerms(advAddReq.getRegId(), term.getTermsSeq(), term.getTermsAgree()));
            }
            memberTermsRepository.saveAll(memberTerms);
        }

        Optional<Member> memberOptional = memberRepository.findById(advAddReq.getRegId());
        Member member = memberOptional.orElseThrow(() -> new ApiException(ApiResponseCodes.NOT_FOUND));

        //개인이라면
        if("BCT003".equals(advAddReq.getCode())) {
            member.setMemberGbn(true);
        }else {
            member.setMemberGbn(false);
            MemberCompany memberCompany = new MemberCompany(advAddReq.getMemberInfo(), advAddReq.getRegId(), advAddReq.getCode());
            memberCompanyRepository.save(memberCompany);
            if(advAddReq.getMemberInfo().getSeq() == null){ em.persist(memberCompany);}
            adv.setCompanySeq(memberCompany.getSeq());
        }

        memberRepository.updateMemberGbn(member.getMemberGbn(),advAddReq.getRegId());
        memberRepository.save(member);

        //광고정보저장
        if("U".equals(saveType)){
            adv.setSeq(advAddReq.getAdvSeq());
            adv.setProgressCode(advAddReq.getProgressCode());
        }

        //사용자 알림확인여부
        adv.setUserCheck(false);
        advRepository.save(adv);
        if(!"U".equals(saveType)){em.persist(adv);}

        //묶음상품일 경우
        if(advAddReq.getProductSeq() != null) {
            AdvProduct advProduct = new AdvProduct(1);//묶음상품일경우 1슬롯
            AdvProductPk advProductPk = new AdvProductPk();
            advProductPk.setAdvSeq(adv.getSeq());
            advProductPk.setProductSeq(advAddReq.getProductSeq());
            advProduct.setAdvProductPk(advProductPk);

            advProductRepository.save(advProduct);
        }

        //매장 광고 슬롯
        advAddReq.getPartnerList().forEach(datas -> {
            AdvPartner advPartner = new AdvPartner(datas.getRequestSlot());
            AdvPartnerPk advPartnerPk = new AdvPartnerPk(adv.getSeq(),datas.getPartnerSeq());
            advPartner.setAdvPartnerPk(advPartnerPk);

            advPartnerRepository.save(advPartner);
        });

        // 기존 파일 정보 조회
        List<AdvFile> beforeFileItems = advFileRepository.findByAdvSeq(adv.getSeq());

        List<AdvFile> advFileList = advAddReq.getAdvFileList();

        if(!CollectionUtils.isEmpty(advFileList)){
            List<Long> fileSeqIs = new ArrayList<>();
            advFileList.forEach(datas -> {
                datas.setAdvSeq(adv.getSeq());
                datas.setRegDate(advAddReq.getRegDate());
                fileSeqIs.add(datas.getFileSeq());
            });

            if(!CollectionUtils.isEmpty(beforeFileItems)){

                // 변경전 fileSeq추출
                List<Long> isNotInFileSeq = advFileRepository.isNotInFileSeq(adv.getSeq(), fileSeqIs);

                // adv_file 삭제
                advFileRepository.deleteAll(beforeFileItems);

                // S3, files 삭제
                if(isNotInFileSeq != null){
                    fileService.delete(isNotInFileSeq);
                }
            }

            advFileRepository.saveAll(advFileList);     // 새로 등록

            // 매핑여부 변경
            fileService.updateMappingDone(fileSeqIs);
        }

        return adv.getSeq();
    }
}
