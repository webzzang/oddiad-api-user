package com.exflyer.oddi.user.api.payment.service;

import com.exflyer.oddi.user.OddiAdUserApiApplication;
import com.exflyer.oddi.user.api.payment.dao.PaymentMapper;
import com.exflyer.oddi.user.api.payment.dto.InicisAuthMobileResult;
import com.exflyer.oddi.user.api.payment.dto.InicisAuthResult;
import com.exflyer.oddi.user.api.payment.dto.InicisMobileReqResult;
import com.exflyer.oddi.user.api.payment.dto.InicisReqResult;
import com.exflyer.oddi.user.api.payment.dto.PaymentRes;
import com.exflyer.oddi.user.api.promotion.service.PromotionService;
import com.exflyer.oddi.user.config.InicisConfig;
import com.exflyer.oddi.user.models.Member;
import com.exflyer.oddi.user.models.Payment;
import com.exflyer.oddi.user.models.PgAccreditLog;
import com.exflyer.oddi.user.models.PgPaymentLog;
import com.exflyer.oddi.user.models.Promotion;
import com.exflyer.oddi.user.repository.AdvRepository;
import com.exflyer.oddi.user.repository.PaymentRepository;
import com.exflyer.oddi.user.repository.PgAccreditLogRepository;
import com.exflyer.oddi.user.repository.PgPaymentLogRepository;
import com.exflyer.oddi.user.repository.PromotionCouponRepository;
import com.exflyer.oddi.user.repository.PromotionRepository;
import com.exflyer.oddi.user.repository.jpa.MemberRepository;
import com.exflyer.oddi.user.share.LocalDateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.inicis.std.util.HttpUtil;
import com.inicis.std.util.SignatureUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

/**
 * ???????????? ???????????? ?????????
 */
@Component
@Slf4j
public class InicisService {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PgAccreditLogRepository pgAccreditLogRepository;

    @Autowired
    private PgPaymentLogRepository pgPaymentLogRepository;

    @Autowired
    private PromotionCouponRepository promotionCouponRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private AdvRepository advRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private InicisConfig inicisConfig;

    @Autowired
    private PaymentMapper paymentMapper;

    /**
     * ???????????? ??????????????? ????????? ??????
     *
     * @param paymentSeq ???????????? ?????????
     * @param couponCode ????????????
     * @param requestUrl ???????????? URL
     * @return Map ????????????
     */
    public Map reqAuthCondition(Long paymentSeq, String couponCode)
        throws Exception {

        Long timestamp = new Date().getTime();

        PaymentRes payment = paymentService.findPayment(paymentSeq, couponCode);

        Map condition = new HashMap();

        // ?????? ?????? (1.0 ??????)
        condition.put("version", "1.0");

        // ?????? ?????????
        condition.put("mid", inicisConfig.getInicisMid());

        // ????????????
        condition.put("oid", paymentSeq);

        // ????????????
        condition.put("price", payment.getPrice());

        // ??????????????????
        condition.put("gopaymethod", "Card");

        // SHA256 Hash??? (??????: mid??? ???????????? signkey)
        condition.put("mKey", SignatureUtil.hash(inicisConfig.getInicisSignKey(), "SHA-256"));

        // SHA256 Hash??? (??????: oid, price, timestamp)
        condition.put("signature", SignatureUtil
            .hash(nvp(condition.get("oid"), condition.get("price"), timestamp), "SHA-256"));

        // ???????????????
        condition.put("timestamp", timestamp);

        // ????????????
        condition.put("currency", "WON");

        // ?????????
        condition.put("goodname", payment.getGoodName());

        // ????????????
        condition.put("buyername", payment.getBuyerName());

        // ????????? ??????????????????
        condition.put("buyertel", payment.getBuyerTel());

        // ????????? ?????????
        condition.put("buyeremail", payment.getBuyerEmail());

        // ??????????????????
        condition.put("acceptmethod", "below1000");

        // ???????????? URL
        condition.put("returnUrl",
            StringUtils.join(inicisConfig.getInicisSiteUrl(), "/user/payment/inicis/result/", paymentSeq, "/", couponCode));

        // ????????? ??????  URL
        condition.put("closeUrl",
            StringUtils.join(inicisConfig.getInicisSiteUrl(), "/user/payment/inicis/close/", paymentSeq, "/", couponCode));

        return condition;
    }

    /**
     * ???????????? ????????????
     *
     * @param inicisReqResult ???????????? ??????
     * @param paymentSeq ???????????? ?????????
     * @param couponCode ????????????
     * @return PaymentAuthResult ???????????? ??????
     */
    public InicisAuthResult paymentProcessing(InicisReqResult inicisReqResult, Long paymentSeq, String couponCode) throws Exception {
        InicisAuthResult inicisAuthResult = new InicisAuthResult();
        String timestamp = SignatureUtil.getTimestamp();

        Map signatureParam = new HashMap();
        signatureParam.put("authToken", inicisReqResult.getAuthToken());
        signatureParam.put("timestamp", timestamp);

        Map authParam = new HashMap();
        authParam.put("mid", inicisReqResult.getMid());
        authParam.put("authToken", inicisReqResult.getAuthToken());
        authParam.put("signature", SignatureUtil.makeSignature(signatureParam));
        authParam.put("timestamp", timestamp);
        authParam.put("charset", inicisReqResult.getCharset());
        authParam.put("format", "JSON");

        try {

            //????????? ??????????????????
            //????????????????????? ??????????????????
            Payment payment = paymentRepository.findPaymentInfo(paymentSeq);
            String advTitle = payment.getAdvName();
            if(StringUtil.isBlank(payment.getAdvName())) {
                advTitle = payment.getProductName();
            }

            log.debug("=================================================================================");
            log.debug("paymentSeq{}", paymentSeq);
            log.debug("mid{}", inicisReqResult.getMid());
            log.debug("paymentType{}", payment.getType());
            log.debug("couponCode{}",couponCode);
            log.debug("=================================================================================");

            //????????????, ??????????????? ????????????
            if (!"PGT002".equals(payment.getType()) && !"PGT005".equals(payment.getType())) {
                if ("nothing".equals(couponCode) ||  promotionService.isValidMemberCouponCode(payment.getMemberId(), couponCode)) {

                    inicisReqResult.setPaymentSeq(paymentSeq);
                    inicisReqResult.setSignature((String) authParam.get("signature"));
                    inicisReqResult.setTimestamp(timestamp);

                    PgAccreditLog pgAccreditLog = new PgAccreditLog();
                    pgAccreditLog.setPgAccreditLog(inicisReqResult);

                    /**
                     * Todo PG ???????????? ?????? ?????? ??????
                     */
                    pgAccreditLogRepository.save(pgAccreditLog);

                    if ("0000".equals(inicisReqResult.getResultCode())) {
                        HttpUtil httpUtil = new HttpUtil();

                        try {
                            inicisAuthResult = new Gson().fromJson(httpUtil.processHTTP(authParam, inicisReqResult.getAuthUrl()),InicisAuthResult.class);

                            //???????????? ?????????
                            if ("0000".equals(inicisAuthResult.getResultCode())) {

                                try{
                                    /**
                                     * Todo ????????? - ?????? ?????? ?????? ??????
                                     * ?????? PGT001, ???????????? PGT002, ???????????? PGT003
                                     */
                                    payment.setPayment(inicisAuthResult, "PGT002");
                                    paymentRepository.save(payment);

                                    /**
                                     * Todo PG - PG ?????? ??????
                                     */
                                    PgPaymentLog pgPaymentLog = new PgPaymentLog(inicisAuthResult);
                                    pgPaymentLog.setPaymentSeq(paymentSeq);
                                    pgPaymentLogRepository.save(pgPaymentLog);
                                    log.debug("authResult {}", inicisAuthResult);

                                    /**
                                     * Todo ???????????? ?????? ??????????????????
                                     */
                                    promotionCouponRepository.saveByMemberCouponUsing(true,LocalDateUtils.krNow(), payment.getMemberId(), couponCode);
                                    Integer dicountPrice = paymentMapper.findByPromotionInfo(couponCode);
                                    /**
                                     * Todo ?????? ????????? ??????
                                     */
                                    advRepository.updateProgress("PGT002", payment.getAdvSeq(), couponCode, dicountPrice, inicisAuthResult.getTotPrice());
                                    //??????????????? ???????????? ?????? ?????????. ????????? ??????
                                    //inicisAuthResult = new Gson().fromJson(httpUtil.processHTTP(authParam, inicisReqResult.getNetCancelUrl()),InicisAuthResult.class);
                                    //log.debug("cancelResult {}", inicisAuthResult);
                                }catch(Exception e) {
                                    /**
                                     * Todo ????????? - ?????? ?????? ?????? ??????
                                     *              1. ?????? ??????
                                     *              2. db ??????
                                     */
                                    log.info("============================[ paymentProcessing ERROR START ]===========================");
                                    log.info("inicisAuthResult {}",  "[paymentSeq : "+paymentSeq+"]", "inicis db insert error");
                                    log.info("============================[ //paymentProcessing ERROR END ]===========================");

                                    log.error("============================[ paymentProcessing ??????DB ?????? ?????? ERROR START ]===========================");
                                    log.error("inicisAuthResult {}",  "[paymentSeq : "+paymentSeq+"]", "inicis db insert error");
                                    log.error("pg_accredit_log ==========================================");
                                    log.error("mid {}", pgAccreditLog.getMid());//
                                    log.error("order_number {}", pgAccreditLog.getOrderNumber());
                                    log.error("timestamp {}", pgAccreditLog.getOrderNumber());
                                    log.error("//pg_accredit_log ========================================");
                                    log.error("pg_payment_log ============================================");
                                    log.error("tid {}", inicisAuthResult.getTid());
                                    log.error("moid {}", inicisAuthResult.getMOID());
                                    log.error("goodName {}", inicisAuthResult.getGoodName());
                                    log.error("buyerName {}", inicisAuthResult.getBuyerName());
                                    log.error("price {}", inicisAuthResult.getTotPrice());
                                    log.error("coupon_number {}", couponCode);
                                    log.error("pay_method {}", inicisAuthResult.getPayMethod());//
                                    log.error("buyer_name {}", inicisAuthResult.getBuyerName());
                                    log.error("buyer_email {}", inicisAuthResult.getBuyerEmail());
                                    log.error("appl_num {}", inicisAuthResult.getApplNum());
                                    log.error("appl_date {}", inicisAuthResult.getApplDate());
                                    log.error("appl_time {}", inicisAuthResult.getApplTime());
                                    log.error("card_num {}", inicisAuthResult.getCARD_Num());
                                    log.error("card_code {}", inicisAuthResult.getCARD_Code());
                                    log.error("type {}", false);
                                    log.error("//pg_payment_log ============================================");

                                    log.error("============================[ //paymentProcessing ??????DB ?????? ?????? ERROR END ]===========================");
                                }
                            } else {

                                inicisAuthResult.setResultCode("9999");
                                inicisAuthResult.setResultMsg("???????????? ?????? ???????????????.\n" + inicisAuthResult.getResultMsg());

                                log.debug("=================================================================================");
                                log.debug("paymentProcessing :{}", "???????????? ?????? ???????????????.\n" + inicisAuthResult.getResultMsg());
                                log.debug("=================================================================================");
                                /**
                                 * Todo ??????????????? ???????????????
                                 * ?????? PGT001, ???????????? PGT002, ???????????? PGT003
                                 */
                                payment.setPayment(inicisAuthResult, "PGT003");
                                paymentRepository.save(payment);

                                //???????????? ????????? ?????????
                                inicisAuthResult = new Gson().fromJson(httpUtil.processHTTP(authParam, inicisReqResult.getNetCancelUrl()), InicisAuthResult.class);
                                PgPaymentLog pgPaymentLog = new PgPaymentLog(inicisAuthResult);
                                pgPaymentLog.setPaymentSeq(paymentSeq);
                                pgPaymentLogRepository.save(pgPaymentLog);
                            }

                        } catch (Exception e) {

                            //??????????????????
                            inicisAuthResult.setResultCode("9999");
                            inicisAuthResult.setResultMsg("????????????????????? ?????? ???????????????.");

                            /**
                             * Todo ????????? - ?????? ?????? ?????? ??????
                             * 1. ?????? ??????
                             */
                            payment.setPayment(inicisAuthResult, "PGT003");
                            paymentRepository.save(payment);
                            //???????????????
                            inicisAuthResult = new Gson().fromJson(httpUtil.processHTTP(authParam, inicisReqResult.getNetCancelUrl()),InicisAuthResult.class);
                            PgPaymentLog pgPaymentLog = new PgPaymentLog(inicisAuthResult);
                            pgPaymentLog.setPaymentSeq(paymentSeq);
                            pgPaymentLogRepository.save(pgPaymentLog);

                            log.error("=================================================================================");
                            log.error("paymentSeq{}", "["+paymentSeq+"]", "["+ advTitle+"] ???????????? ????????? ?????????????????????.");
                            log.error("=================================================================================");
                        }

                    } else {

                        //???????????? ??????
                        inicisAuthResult.setResultCode("9999");
                        inicisAuthResult.setResultMsg("??????????????? ?????? ???????????????.");

                        /**
                         * Todo ????????? - ?????? ?????? ?????? ??????
                         */
                        payment.setCancelPayment(inicisReqResult, "PGT003");
                        paymentRepository.save(payment);

                        log.debug("=================================================================================");
                        log.debug("paymentProcessing :{}", "??????????????? ?????? ???????????????.");
                        log.debug("=================================================================================");
                    }
                } else {
                    //???????????? ??????
                    inicisAuthResult.setResultCode("9999");
                    inicisAuthResult.setResultMsg("???????????? ?????? ?????? ?????? ???????????????.");

                    log.debug("=================================================================================");
                    log.debug("paymentProcessing :{}, couponCode {}", "???????????? ?????? ?????? ?????? ???????????????.", couponCode);
                    log.debug("=================================================================================");
                }
            } else {
                //???????????? ?????? ????????????
                inicisAuthResult.setResultCode("9999");
                inicisAuthResult.setResultMsg("???????????? ?????? ???????????? ????????????.");

                log.debug("=================================================================================");
                log.debug("paymentProcessing : {}", "???????????? ?????? ???????????? ????????????.");
                log.debug("=================================================================================");
            }
        } catch (Exception e) {

            HttpUtil httpUtil = new HttpUtil();
            String cancelResult = httpUtil.processHTTP(authParam, inicisReqResult.getNetCancelUrl());

            inicisAuthResult.setResultCode("9999");
            inicisAuthResult.setResultMsg("???????????? ???????????????.");

            log.debug("=================================================================================");
            log.debug("paymentProcessing :{}, cancelResult {}", "???????????? ???????????????.", cancelResult);
            log.debug("=================================================================================");
        }
        return inicisAuthResult;
    }

    /**
     * ???????????? ???????????? ??????
     *
     * @param paymentSeq ???????????? ?????????
     * @param couponCode ????????????
     */
    public void paymentClose(Long paymentSeq, String couponCode) {
        /**
         * Todo ?????????????????? ????????? ?????? ?????? ??????
         */
    }

    /**
     * name and value parameter (????????? ????????? ??????????????? ??????)
     *
     * @param oid ????????????
     * @param price ????????????
     * @param timestamp ??????
     * @return String
     */
    private String nvp(Object oid, Object price, Object timestamp) {
        return StringUtils.join("oid=", oid, "&price=", price, "&timestamp=", timestamp);
    }

    /**
     * ????????? ???????????? ??????????????? ????????? ??????
     *
     * @param paymentSeq ???????????? ?????????
     * @param couponCode ????????????
     * @param requestUrl ???????????? URL
     * @return Map ????????????
     */
    public Map reqAuthMobileCondition(Long paymentSeq, String couponCode) throws Exception {

        Long timestamp = new Date().getTime();

        PaymentRes payment = paymentService.findPayment(paymentSeq, couponCode);

        Map condition = new HashMap();

        // ?????? ?????? (1.0 ??????)
        condition.put("version", "1.0");

        // ?????? ?????????
        condition.put("mid", inicisConfig.getInicisMid());

        // ????????????
        condition.put("oid", paymentSeq);

        // ????????????
        condition.put("price", payment.getPrice());

        // ??????????????????
        condition.put("gopaymethod", "Card");

        // ?????????
        condition.put("goodname", payment.getGoodName());

        // ????????????
        condition.put("buyername", payment.getBuyerName());

        // ???????????? URL
        condition.put("returnUrl",
            StringUtils.join(inicisConfig.getInicisSiteUrl(), "/user/payment/inicis/mobile/result/", paymentSeq, "/", couponCode));

        return condition;
    }

    /**
     * ????????? ???????????? ????????????
     *
     * @param inicisReqResult ???????????? ??????
     * @param paymentSeq ???????????? ?????????
     * @param couponCode ????????????
     * @return PaymentAuthResult ???????????? ??????
     */
    public InicisAuthMobileResult paymentMobileProcessing(InicisMobileReqResult inicisMobileReqResult, Long paymentSeq, String couponCode) throws Exception {

        InicisAuthMobileResult inicisAuthMobileResult = new InicisAuthMobileResult();
        String timestamp = SignatureUtil.getTimestamp();

        Map authParam = new HashMap();

        authParam.put("P_MID", inicisConfig.getInicisMid());
        authParam.put("P_TID", inicisMobileReqResult.getP_TID());

        log.debug("=================================================================================");
        log.debug("mid {}", inicisConfig.getInicisMid());
        log.debug("authParam {}", authParam);
        log.debug("=================================================================================");

        //????????? ??????????????????
        //????????????????????? ??????????????????
        Payment payment = paymentRepository.findPaymentInfo(paymentSeq);
        String advTitle = payment.getAdvName();
        if(StringUtil.isBlank(payment.getAdvName())) {
            advTitle = payment.getProductName();
        }

        //????????????, ??????????????? ????????????
        if (!"PGT002".equals(payment.getType()) && !"PGT005".equals(payment.getType())) {
            if ("nothing".equals(couponCode) ||  promotionService.isValidMemberCouponCode(payment.getMemberId(), couponCode)) {

                inicisMobileReqResult.setPaymentSeq(paymentSeq);

                PgAccreditLog pgAccreditLog = new PgAccreditLog();
                inicisMobileReqResult.setMid(inicisConfig.getInicisMid());
                pgAccreditLog.setMobilePgAccreditLog(inicisMobileReqResult);

                /**
                 * Todo PG ???????????? ?????? ?????? ??????
                 */
                pgAccreditLogRepository.save(pgAccreditLog);

                if ("00".equals(inicisMobileReqResult.getP_STATUS())) {
                    HttpClient client = new HttpClient();

                    String P_REQ_URL = inicisMobileReqResult.getP_REQ_URL() + "?P_TID=" + inicisMobileReqResult.getP_TID() + "&P_MID=" + inicisConfig.getInicisMid();
                    log.debug("P_REQ_URL ::::::::::: " +P_REQ_URL );
                    GetMethod method = new GetMethod(P_REQ_URL);
                    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                        new DefaultHttpMethodRetryHandler(3, false));

                    try{
                        int statusCode = client.executeMethod(method);
                        log.debug("statusCode ####::::::::: " + statusCode );

                        if (statusCode != HttpStatus.SC_OK) {
                            log.debug("Method failed: " + method.getStatusLine());
                        }else {

                            InputStream rstream = method.getResponseBodyAsStream();
                            BufferedReader br = new BufferedReader(new InputStreamReader(rstream, "euc-kr"));

                            String line;
                            String resultString = "";
                            while ((line = br.readLine()) != null) {
                                resultString += line;
                            }
                            br.close();

                            String[] values = resultString.split("&");
                            HashMap<String, String> map = new HashMap<String, String>();

                            for( int x = 0; x < values.length; x++ ){
                                String[] value = values[x].split("=");
                                String key = value[0];
                                if(value.length > 1) {
                                    String val =  val = value[1];
                                    map.put(key.trim(), val.trim());
                                }else {
                                    map.put(key.trim(), "");
                                }
                            }

                            log.debug("###################################################### {}", map );
                            inicisAuthMobileResult.setInicisAuthMobileResult(map);
                            String resultCode = map.get("P_STATUS");
                            log.debug("resultCode ==>  {}", resultCode);
                            if("00".equals(resultCode)) {
                                /**
                                 * Todo ????????? - ?????? ?????? ?????? ??????
                                 * ?????? PGT001, ???????????? PGT002, ???????????? PGT003
                                 */
                                payment.setPayment(inicisAuthMobileResult, "PGT002");
                                paymentRepository.save(payment);

                                /**
                                 * Todo PG - PG ?????? ??????
                                 */
                                PgPaymentLog pgPaymentLog = new PgPaymentLog();
                                pgPaymentLog.setPgPaymentLog(inicisAuthMobileResult);
                                pgPaymentLog.setPaymentSeq(paymentSeq);
                                pgPaymentLogRepository.save(pgPaymentLog);

                                /**
                                 * Todo ???????????? ?????? ??????????????????
                                 */
                                promotionCouponRepository.saveByMemberCouponUsing(true,LocalDateUtils.krNow(), payment.getMemberId(), couponCode);

                                Integer dicountPrice = paymentMapper.findByPromotionInfo(couponCode);
                                /**
                                 * Todo ?????? ????????? ??????
                                 */
                                advRepository.updateProgress("PGT002", payment.getAdvSeq(), couponCode, dicountPrice, Integer.parseInt(inicisMobileReqResult.getP_AMT()));

                            }else {
                                inicisAuthMobileResult.setResultCode("9999");
                                inicisAuthMobileResult.setResultMsg("????????????.");
                            }
                        }
                    } catch (HttpException e) {

                        //??????????????????
                        inicisAuthMobileResult.setResultCode("9999");
                        inicisAuthMobileResult.setResultMsg("????????????????????? ?????? ???????????????.");

                        /**
                         * Todo ????????? - ?????? ?????? ?????? ??????
                         *              1. ?????? ??????
                         */
                        payment.setPayment(inicisAuthMobileResult, "PGT003");
                        paymentRepository.save(payment);

                        log.error("============================[ paymentMobileProcessing ERROR START ]===========================");
                        log.error("paymentSeq{}", "["+paymentSeq+"]", "["+ advTitle+"] ???????????? ????????? ?????????????????????.");
                        log.error("============================[ paymentMobileProcessing ERROR END ]===========================");

                        e.printStackTrace();

                    } catch (IOException e) {

                        /**
                         * Todo ????????? - ?????? ?????? ?????? ??????
                         *              1. ?????? ??????
                         *              2. db ??????
                         */
                        payment.setPayment(inicisAuthMobileResult, "PGT003");
                        paymentRepository.save(payment);

                        log.info("============================[ paymentMobileProcessing ERROR START ]===========================");
                        log.info("inicisAuthMobileResult {}",  "[paymentSeq : "+paymentSeq+"]", "inicis db insert error");
                        log.info("============================[ //paymentMobileProcessing ERROR END ]===========================");

                        log.error("============================[ paymentMobileProcessing ??????DB ?????? ?????? ERROR START ]===========================");
                        log.error("inicisAuthResult {}",  "[paymentSeq : "+paymentSeq+"]", "inicis db insert error");
                        log.error("pg_accredit_log ====================================================");
                        log.error("mid {}", pgAccreditLog.getMid());//
                        log.error("order_number {}", pgAccreditLog.getOrderNumber());
                        log.error("timestamp {}", pgAccreditLog.getOrderNumber());
                        log.error("//pg_accredit_log ==================================================");
                        log.error("pg_payment_log =====================================================");
                        log.error("tid {}", inicisAuthMobileResult.getP_TID());
                        log.error("moid {}", inicisAuthMobileResult.getP_MID());
                        log.error("P_AUTH_DT {}", inicisAuthMobileResult.getP_AUTH_DT());
                        log.error("P_OID {}", inicisAuthMobileResult.getP_OID());
                        log.error("P_AMT {}", inicisAuthMobileResult.getP_AMT());
                        log.error("coupon_number {}", couponCode);
                        log.error("P_UNAME {}", inicisAuthMobileResult.getP_UNAME());//
                        log.error("P_AUTH_NO {}", inicisAuthMobileResult.getP_AUTH_NO());
                        log.error("P_CARD_NUM {}", inicisAuthMobileResult.getP_CARD_NUM());
                        log.error("P_FN_CD1 {}", inicisAuthMobileResult.getP_FN_CD1());
                        log.error("type {}", true);
                        log.error("//pg_payment_log =====================================================");
                        log.error("============================[ paymentMobileProcessing ??????DB ?????? ?????? ERROR END ]===========================");

                        e.printStackTrace();
                    } finally {
                        method.releaseConnection();
                    }
                } else {

                    //???????????? ??????
                    inicisAuthMobileResult.setResultCode("9999");
                    inicisAuthMobileResult.setResultMsg("??????????????? ?????? ???????????????.");

                    /**
                     * Todo ????????? - ?????? ?????? ?????? ??????
                     */
                    payment.setCancelPayment(inicisMobileReqResult, "PGT003");
                    paymentRepository.save(payment);

                    log.debug("=================================================================================");
                    log.debug("paymentMobileProcessing :{}", "??????????????? ?????? ???????????????.");
                    log.debug("=================================================================================");
                }
            } else {
                //???????????? ??????
                inicisAuthMobileResult.setResultCode("9999");
                inicisAuthMobileResult.setResultMsg("???????????? ?????? ?????? ?????? ???????????????.");

                log.debug("=================================================================================");
                log.debug("paymentMobileProcessing :{}, couponCode {}", "???????????? ?????? ?????? ?????? ???????????????.", couponCode);
                log.debug("=================================================================================");
            }
        } else {
            //???????????? ?????? ????????????
            inicisAuthMobileResult.setResultCode("9999");
            inicisAuthMobileResult.setResultMsg("???????????? ?????? ???????????? ????????????.");

            log.debug("=================================================================================");
            log.debug("paymentMobileProcessing : {}", "???????????? ?????? ???????????? ????????????.");
            log.debug("=================================================================================");
        }

        return inicisAuthMobileResult;
    }
}


