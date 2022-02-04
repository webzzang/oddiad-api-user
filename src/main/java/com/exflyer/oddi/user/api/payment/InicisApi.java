package com.exflyer.oddi.user.api.payment;

import com.exflyer.oddi.user.annotaions.OddiEncrypt;
import com.exflyer.oddi.user.api.payment.dto.InicisMobileReqResult;
import com.exflyer.oddi.user.api.payment.dto.InicisReqResult;
import com.exflyer.oddi.user.api.payment.service.InicisService;
import io.swagger.annotations.ApiOperation;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 이니시스 결제 API
 */
@Slf4j
@Controller
public class InicisApi {

  @Resource
  private InicisService inicisService;

  public static final String IS_MOBILE = "MOBILE";

  private final String PC_VIEW_NAME = "payment/auth";
  private final String MOBILE_VIEW_NAME = "payment/mobile_auth";

  @InitBinder
  public void InitBinder(WebDataBinder dataBinder) {
    StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
    dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
  }

  /**
   * 이니시스 결제 페이지 로드용
   *
   * @param paymentSeq 결제정보 시퀀스
   * @param couponCode 쿠폰코드
   * @param modelMap   view 데이타 연동 데이타
   * @param request    요청정보
   * @return String
   * @throws NoSuchAlgorithmException
   */
  @GetMapping(path = "/payment/inicis/auth/{paymentSeq}/{couponCode}")
  public ModelAndView goAuth(@PathVariable Long paymentSeq, @PathVariable String couponCode,
                       ModelMap modelMap, HttpServletRequest request) throws Exception {

    ModelAndView mv = new ModelAndView();

    log.debug("========== goAuth");
    log.debug("paymentSeq: {} ", paymentSeq);

    String userAgent = request.getHeader("User-Agent").toUpperCase();

    log.debug("==========================userAgent==========================");
    log.debug("userAgent :" + userAgent);
    log.debug("==========================userAgent//========================");
    Boolean isMobile = false;

    if (userAgent.indexOf(IS_MOBILE) > -1) {
        isMobile = true;
    }
    log.debug("isMobile ===> " + isMobile);

      String requestUrl = request.getRequestURL().toString().replace(request.getRequestURI(),"");
      log.debug("requestUrl {}", requestUrl);

    if(isMobile) {
      modelMap.putAll(inicisService.reqAuthMobileCondition(paymentSeq, couponCode));
      mv.setViewName(MOBILE_VIEW_NAME);
    }else {
      modelMap.putAll(inicisService.reqAuthCondition(paymentSeq, couponCode));
      mv.setViewName(PC_VIEW_NAME);
    }

    return mv;
  }

  /**
   * 이니시스 결제 결과 처리용
   *
   * @param paymentSeq 결제정보 시퀀스
   * @param couponCode 쿠폰코드
   * @param request    요청정보
   * @return String
   * @throws Exception
   */
  @ApiOperation(value = "이니시스 결제 처리용 API", notes = "이니시스 결제 처리용 API 입니다. ")
  @PostMapping(path = "/payment/inicis/result/{paymentSeq}/{couponCode}")
  public String paymentResult(@PathVariable Long paymentSeq, @PathVariable String couponCode,
                              ModelMap modelMap, HttpServletRequest request) throws Exception {
    log.debug("========== paymentResult");

    Enumeration elems = request.getParameterNames();
    InicisReqResult inicisReqResult = new InicisReqResult();

    Field field;
    String name;

    while(elems.hasMoreElements())
    {
      name = (String) elems.nextElement();
      field = ReflectionUtils.findField(inicisReqResult.getClass(), name);

      if (null != field) {
        field.setAccessible(true);
        field.set(inicisReqResult, request.getParameter(name));
      }
    }

    log.debug("paymentReqResult: {}", inicisReqResult);
    modelMap.put("result", this.inicisService.paymentProcessing(inicisReqResult, paymentSeq, couponCode));

    return "payment/result";
  }

  /**
   * 이니시스 결제 중지 처리용
   *
   * @param paymentSeq 결제정보 시퀀스
   * @param couponCode 쿠폰코드
   * @return String
   */
  @ApiOperation(value = "이니시스 결제 중지 API", notes = "이니시스 결제 중지 API 입니다. ")
  @GetMapping(path = "/payment/inicis/close/{paymentSeq}/{couponCode}")
  public String paymentClose(@PathVariable Long paymentSeq, @PathVariable String couponCode) {
    log.debug("========== paymentClose");
    log.debug("paymentSeq: {}", paymentSeq);

    this.inicisService.paymentClose(paymentSeq, couponCode);

    return "payment/close";
  }

  /**
   * 모바일 이니시스 결제 페이지 로드용
   *
   * @param paymentSeq 결제정보 시퀀스
   * @param couponCode 쿠폰코드
   * @param modelMap   view 데이타 연동 데이타
   * @param request    요청정보
   * @return String
   * @throws NoSuchAlgorithmException
   */
  @GetMapping(path = "/payment/inicis/mobile/auth/{paymentSeq}/{couponCode}")
  public String goMobileAuth(@PathVariable Long paymentSeq, @PathVariable String couponCode,
      ModelMap modelMap, HttpServletRequest request) throws Exception {
    log.debug("========== goMobileAuth");
    log.debug("paymentSeq: {} ", paymentSeq);
    modelMap.putAll(inicisService.reqAuthMobileCondition(paymentSeq, couponCode));

    log.debug("==========================모바일 이니시스 결제 페이지 로드 ==========================");
    log.debug("modelMap : " +modelMap);
    log.debug("==========================모바일 이니시스 결제 페이지 로드 //========================");
    return "payment/mobile_auth";
  }

  /**
   * 모바일 이니시스 결제 결과 처리용
   *
   * @param paymentSeq 결제정보 시퀀스
   * @param couponCode 쿠폰코드
   * @param request    요청정보
   * @return String
   * @throws Exception
   */
  @ApiOperation(value = "모바일 이니시스 결제 처리용 API", notes = "이니시스 결제 처리용 API 입니다. ")
  @PostMapping(path = "/payment/inicis/mobile/result/{paymentSeq}/{couponCode}")
  public String paymentMobileResult(@PathVariable Long paymentSeq, @PathVariable String couponCode,
      ModelMap modelMap, HttpServletRequest request) throws Exception {
    log.debug("========== paymentMobileResult");

    request.setCharacterEncoding("euc-kr");
    Enumeration elems = request.getParameterNames();
    InicisMobileReqResult inicisMobileReqResult = new InicisMobileReqResult();

    Field field;
    String name;

    while(elems.hasMoreElements())
    {
      name = (String) elems.nextElement();
      field = ReflectionUtils.findField(inicisMobileReqResult.getClass(), name);

      if (null != field) {
        field.setAccessible(true);
        field.set(inicisMobileReqResult, request.getParameter(name));
      }
    }

    log.debug("==========================모바일 이니시스 결제 처리용==========================");
    log.debug("inicisMobileReqResult : " + inicisMobileReqResult);
    log.debug("==========================모바일 이니시스 결제 처리용//========================");

    modelMap.put("result", this.inicisService.paymentMobileProcessing(inicisMobileReqResult, paymentSeq, couponCode));
    log.debug("==========================modelMap=-===");
    log.debug("modelMap : " + modelMap);
    log.debug("==========================modelMap//-===");

    return "payment/mobile_result";
  }
}
