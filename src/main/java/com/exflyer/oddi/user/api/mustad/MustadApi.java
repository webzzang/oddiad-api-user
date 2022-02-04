package com.exflyer.oddi.user.api.mustad;

import com.exflyer.oddi.user.annotaions.LoginNeedApi;
import com.exflyer.oddi.user.api.mustad.dto.AwsKakaoAuthReq;
import com.exflyer.oddi.user.api.mustad.dto.AwsKakaoAuthRes;
import com.exflyer.oddi.user.api.mustad.dto.FederatedAuthRes;
import com.exflyer.oddi.user.api.mustad.dto.KakaoAccessTokenInfo;
import com.exflyer.oddi.user.api.mustad.dto.MustadKakaoRes;
import com.exflyer.oddi.user.api.mustad.dto.MustadResult;
import com.exflyer.oddi.user.api.mustad.dto.MustadToken;
import com.exflyer.oddi.user.api.mustad.service.MustadService;
import com.exflyer.oddi.user.enums.ApiResponseCodes;
import com.exflyer.oddi.user.exceptions.ApiException;
import com.exflyer.oddi.user.share.dto.ApiResponseDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "머스타드", protocols = "http")
@Slf4j
@RestController
public class MustadApi {

    @Autowired
    private MustadService mustadService;

    @InitBinder
    public void InitBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @ApiOperation(value = "카카오 토큰발행을 위한 인가절차 API", notes = "카카오 토큰발행을 위한 인가절차 API 입니다. ")
    @GetMapping(path = "/mustad/kakao/oauth/authorize")
    public void goMustadKakaoAuthorize() throws ApiException, Exception {
        mustadService.goMustadKakaoAuthorize();
    }

    @ApiOperation(value = "카카오 토큰 API", notes = "카카오 토큰 API 입니다. ")
    @PostMapping(path = "/mustad/kakao/oauth/token/{code}")
    public ApiResponseDto<MustadKakaoRes> goMustadKakaoToken(@PathVariable String code) throws ApiException, Exception {
        MustadKakaoRes result = mustadService.goMustadKakaoToken(code);
        return new ApiResponseDto(ApiResponseCodes.SUCCESS, result);
    }

    @ApiOperation(value = "카카오 토큰정보 API", notes = "카카오 토큰정보 API 입니다. ")
    @PostMapping(path = "/mustad/kakao/user/token/{accessTokenInfo}")
    public ApiResponseDto<KakaoAccessTokenInfo> goMustadKakaoAccessTokenInfo(@PathVariable String accessToken) throws ApiException, Exception {
        log.debug("kakao Token : " + accessToken);
        KakaoAccessTokenInfo result = mustadService.goMustadKakaoAccessTokenInfo(accessToken);
        return new ApiResponseDto(ApiResponseCodes.SUCCESS, result);
    }

    @ApiOperation(value = "카카오 로그아웃 API", notes = "카카오 로그아웃 API 입니다. ")
    @PostMapping(path = "/mustad/kakao/user/logout/{accessToken}")
    public ApiResponseDto<KakaoAccessTokenInfo> goMustadKakaoLogout(@PathVariable String accessToken) throws ApiException, Exception {
        KakaoAccessTokenInfo result = mustadService.goMustadKakaoLogout(accessToken);
        return new ApiResponseDto(ApiResponseCodes.SUCCESS, result);
    }

    @ApiOperation(value = "AWS_KAKAO_Auth API", notes = "AWS_KAKAO_Auth API 입니다. ")
    @PostMapping(path = "/mustad/kakao/auth/token")
    public ApiResponseDto<AwsKakaoAuthRes> goMustadAuthorize(@Validated @RequestBody AwsKakaoAuthReq awsKakaoAuthReq) throws ApiException, Exception {
        AwsKakaoAuthRes result = mustadService.goMustadAuthorize(awsKakaoAuthReq);
        log.debug("====================================================");
        log.debug("result : {} ", result);
        log.debug("====================================================");
        return new ApiResponseDto(ApiResponseCodes.SUCCESS, result);
    }


    @ApiOperation(value = "머스타드 토큰 조회 API", notes = "머스타드 토큰 조회 API 입니다. ")
    @LoginNeedApi
    @PostMapping(path = "/mustad/federated/token")
    public ApiResponseDto<FederatedAuthRes> goFederatedAuth(@Validated @RequestBody MustadToken mustadToken) throws ApiException, Exception {
        FederatedAuthRes result = mustadService.goFederatedAuth(mustadToken.getToken());
        log.debug("====================================================");
        log.debug("result : {} ", result);
        log.debug("====================================================");
        return new ApiResponseDto(ApiResponseCodes.SUCCESS, result);
    }

    @ApiOperation(value = "머스타드 리스트 조회 API", notes = "머스타드 리스트 조회 API 입니다. ")
    @LoginNeedApi
    @PutMapping(path = "/mustad/content/{token}")
    public ApiResponseDto<MustadResult> goMustadContentList(@PathVariable String token) throws ApiException, Exception {
        MustadResult result = mustadService.goMustadContentList(token);
        log.debug("====================================================");
        log.debug("result : {} ", result);
        log.debug("====================================================");
        return new ApiResponseDto(ApiResponseCodes.SUCCESS, result);
    }
}
