package com.exflyer.oddi.user.api.payment.service;

import com.exflyer.oddi.user.annotaions.OddiEncrypt;
import com.exflyer.oddi.user.api.adv.adv.dto.AdvFileRes;
import com.exflyer.oddi.user.api.adv.adv.dto.AdvPartnerDetailRes;
import com.exflyer.oddi.user.api.adv.oddi.dao.OddiMapper;
import com.exflyer.oddi.user.api.notification.service.KakaoNotificationService;
import com.exflyer.oddi.user.api.payment.dao.PaymentMapper;
import com.exflyer.oddi.user.api.payment.dto.AdvUserCancelReq;
import com.exflyer.oddi.user.api.payment.dto.InicisCancelReq;
import com.exflyer.oddi.user.api.payment.dto.InicisCancelRes;
import com.exflyer.oddi.user.api.payment.dto.PaymentCancelReq;
import com.exflyer.oddi.user.api.payment.dto.PaymentCancelRes;
import com.exflyer.oddi.user.api.payment.dto.PaymentCouponReq;
import com.exflyer.oddi.user.api.payment.dto.PaymentCouponRes;
import com.exflyer.oddi.user.api.payment.dto.PaymentDetailResult;
import com.exflyer.oddi.user.api.payment.dto.PaymentRes;
import com.exflyer.oddi.user.api.voc.terms.dto.TermsServiceRes;
import com.exflyer.oddi.user.config.InicisConfig;
import com.exflyer.oddi.user.enums.ApiResponseCodes;
import com.exflyer.oddi.user.enums.KakaoNoticationCodes;
import com.exflyer.oddi.user.exceptions.ApiException;
import com.exflyer.oddi.user.models.KakaoTemplate;
import com.exflyer.oddi.user.models.Member;
import com.exflyer.oddi.user.models.Notification;
import com.exflyer.oddi.user.models.PartnerConfig;
import com.exflyer.oddi.user.models.Payment;
import com.exflyer.oddi.user.models.PgPaymentLog;
import com.exflyer.oddi.user.repository.AdvRepository;
import com.exflyer.oddi.user.repository.PatnerConfigRepository;
import com.exflyer.oddi.user.repository.PaymentRepository;
import com.exflyer.oddi.user.repository.PgPaymentLogRepository;
import com.exflyer.oddi.user.repository.PromotionCouponRepository;
import com.exflyer.oddi.user.repository.jpa.MemberTermsRepository;
import com.exflyer.oddi.user.share.LocalDateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.inicis.std.util.HttpUtil;
import com.inicis.std.util.SignatureUtil;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class PaymentService {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OddiMapper oddiMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AdvRepository advRepository;

    @Autowired
    private PromotionCouponRepository promotionCouponRepository;

    @Autowired
    private PgPaymentLogRepository pgPaymentLogRepository;

    @Autowired
    private PatnerConfigRepository patnerConfigRepository;

    @Autowired
    private MemberTermsRepository memberTermsRepository;

    @Autowired
    private KakaoNotificationService kakaoNotificationService;

    @Autowired
    private InicisConfig inicisConfig;

    /**
    * 결제정보 조회
    *
    * @param paymentSeq
    * @return
    */
    @OddiEncrypt
    public PaymentRes findPayment(Long paymentSeq, String couponCode) {
        return paymentMapper.findByPayment(paymentSeq);
    }

    public PaymentDetailResult findList(Long advSeq, String id) {

        PaymentDetailResult resultList = paymentMapper.findAdvDetail(advSeq);
        List<AdvFileRes> findAdvFileList = oddiMapper.findAdvFileList(advSeq); //파일조회
        List<TermsServiceRes> termsList = oddiMapper.findTermsList(id);         //약관조회

        List<PartnerConfig> partnerConfig =  patnerConfigRepository.findAll();  //파트너설정(광고취소일자)

        int paymentFirst = paymentRepository.findByFirstId(id);

        //쿠폰할인 조회
        PaymentCouponReq paymentCouponReq = new PaymentCouponReq();
        paymentCouponReq.setMemberId(id);
        paymentCouponReq.setCouponUsable(false);
        paymentCouponReq.setPromotionUsable(true);
        paymentCouponReq.setExpiredDate(LocalDateUtils.dateConvertFormatter());
        paymentCouponReq.setPaymentFirst(paymentFirst);
        List<PaymentCouponRes> couponList = paymentMapper.findPaymentCoupon(paymentCouponReq);

        int totalPrice = 0;

        List<AdvPartnerDetailRes> advPartnerResList = null;

        //묶음상품이 아닐경우는 신청금액 합산
        if(resultList.getProductSeq() == null) {
            advPartnerResList = oddiMapper.findAdvPartnerList(advSeq); //파트너조회
            for (AdvPartnerDetailRes advPartnerDetailRes: advPartnerResList) {
                totalPrice += advPartnerDetailRes.getSlotPrice() * advPartnerDetailRes.getRequestSlot();
            }

        } else {

            advPartnerResList = oddiMapper.findAdvPartnerProductList(resultList.getProductSeq(), resultList.getAdvSeq());
            for (AdvPartnerDetailRes advPartnerDetailRes: advPartnerResList) {
                totalPrice += advPartnerDetailRes.getSlotPrice() * advPartnerDetailRes.getRequestSlot();
            }
        }

        resultList.setPartnerConfig(partnerConfig);
        resultList.setTotalPrice(totalPrice);
        resultList.setAdvFileList(findAdvFileList);
        resultList.setAdvPartnerList(advPartnerResList);
        resultList.setMemberTerms(termsList);
        resultList.setCouponList(couponList);
        return resultList;
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public Long save(PaymentRes req) throws ApiException {

        req.setType("PGT001");
        if(req.getPrice() == 0) {
            req.setType("PGT002");
            req.setSuccess(true);
        }

        Payment payment = new Payment(req);
        paymentRepository.save(payment);
        em.persist(payment);

        //광고에 결제 payment저장해 쿠폰 저장해.
        advRepository.updateCoupon(req.getAdvSeq(), payment.getSeq());

        //프로모션 쿠폰 저장하기
        promotionCouponRepository.saveByMemberCouponUsing(true,req.getRegDate(),req.getMemberId(), req.getCouponNumber());
        return payment.getSeq();
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public InicisCancelRes paymentCancel(Long paymentSeq,  Long advSeq,Boolean isMobile) throws ApiException, Exception {

        InicisCancelRes inicisCancelRes = new InicisCancelRes();//결과값 리턴
        PaymentCancelReq paymentCancelReq = new PaymentCancelReq(paymentSeq, advSeq);
        PaymentCancelRes paymentCancelRes = paymentMapper.fnidPaymentInfo(paymentCancelReq);

        if(paymentCancelRes == null) {
            throw new ApiException(ApiResponseCodes.NOT_FOUND);
        }

        //결제안하고 광고취소했을경우
        if("PGT001".equals(paymentCancelRes.getType())) {
            advRepository.updateCancelProgress("PGT005",advSeq);
            inicisCancelRes.setResultCode("00");
            inicisCancelRes.setResultMsg("광고취소");
            return inicisCancelRes;
        }

        //금액이 0일경우 광고취소, 광고상태가 신청상태일 경우
        if(paymentCancelRes.getPrice() < 1 || "PGT001".equals(paymentCancelRes.getType())) {

            inicisCancelRes.setResultCode("00");
            inicisCancelRes.setResultMsg("정상완료");
            //payment에 광고취소
            paymentRepository.saveByIdType("PGT005",paymentSeq);
            advRepository.updateCancelProgress("PGT005",advSeq);

            //프로모션쿠폰 취소
            if(!StringUtils.isBlank(paymentCancelRes.getCouponNumber()) && !"nothing".equals(paymentCancelRes.getCouponNumber())) {
                promotionCouponRepository.saveByMemberCouponUsing(false,LocalDateUtils.krNow(),paymentCancelRes.getMemberId(), paymentCancelRes.getCouponNumber());
            }

            return inicisCancelRes;
        }

        // hash(INIAPIKey+ type + paymethod + timestamp + clientIp + mid + tid)
        String hashData = inicisConfig.getInicisKey() + "Refund"+ paymentCancelRes.getPayMethod() + LocalDateUtils.krNowByFormatter("yyyyMMddHHmmss")
            + inicisConfig.getInicisClientIpl() + paymentCancelRes.getMid() + paymentCancelRes.getTid();
        paymentCancelRes.setHashData(SignatureUtil.hash(hashData, "SHA-512"));

        log.debug("=================================================================================");
        log.debug("InicisKey, {} ", inicisConfig.getInicisKey());
        log.debug("PayMethod, {} ", paymentCancelRes.getPayMethod());
        log.debug("InicisClientIpl, {} ", inicisConfig.getInicisClientIpl());
        log.debug("Mid, {} ", paymentCancelRes.getMid());
        log.debug("Tid, {} ", paymentCancelRes.getTid());
        log.debug("hashData, {} ", hashData);
        log.debug("=================================================================================");
        InicisCancelReq inicisCancelReq = new InicisCancelReq(paymentCancelRes, inicisConfig.getInicisClientIpl());

        //이니시스 취소URL 호출
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> paramMap = objectMapper.convertValue(inicisCancelReq, Map.class);
        HttpUtil httpUtil = new HttpUtil();
        inicisCancelRes = new Gson().fromJson(httpUtil.processHTTP(paramMap, inicisConfig.getInicisCancelUrl()), InicisCancelRes.class);
        log.info("inicisCancelRes, {} ", inicisCancelRes);
        try{

            if("00".equals(inicisCancelRes.getResultCode()) || "01".equals(inicisCancelRes.getResultCode())) {
                //payment에 결제취소
                paymentRepository.saveByIdType("PGT004",paymentSeq);

                log.debug("saveByIdType, {} ", paymentSeq);

                PgPaymentLog pgPaymentLog = new PgPaymentLog();
                pgPaymentLog.setCancelPgPayment(inicisCancelRes,paymentCancelRes, isMobile);
                pgPaymentLogRepository.save(pgPaymentLog);

                log.debug("saveByIdType, {} ", paymentSeq);

                /**
                 * TODO 결제취소, 광고취소, 쿠폰취소
                 */
                paymentRepository.saveByIdType("PGT004",paymentSeq);
                advRepository.updateCancelProgress("PGT004",advSeq);
                if(!StringUtils.isBlank(paymentCancelRes.getCouponNumber()) && !"nothing".equals(paymentCancelRes.getCouponNumber())) {
                    promotionCouponRepository.saveByMemberCouponUsing(false,LocalDateUtils.krNow(),paymentCancelRes.getMemberId(), paymentCancelRes.getCouponNumber());
                }
            }

        }catch(Exception e) {

            log.error("============================[ paymentCancel ERROR START ]===========================");
            log.error("inicisCancelRes e {}", e);
            log.error("inicisCancelRes {}",  "["+paymentCancelRes.getPaymentSeq()+"]", "["+paymentCancelRes.getAdvTitle()+"]","["+ paymentCancelRes.getBuyerName() +"] 결제취소후 DB저장시 에러가 발생하였습니다.");
            log.error("============================[ paymentCancel ERROR END ]===========================");

            log.info("============================[ paymentCancel ERROR START ]===========================");
            log.info("inicisCancelRes {}",  "[advSeq : "+paymentCancelRes.getPaymentSeq()+"]", "inicis db insert error");
            log.info("============================[ paymentCancel ERROR END ]===========================");
        }

        return inicisCancelRes;
    }

    public Payment findPaymentList(Long paymentSeq, String id) {
        return paymentRepository.findPaymentList(paymentSeq, id);
    }
}
