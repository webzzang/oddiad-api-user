package com.exflyer.oddi.user.api.mustad.service;


import com.exflyer.oddi.user.api.files.config.AwsS3PublicConfig;
import com.exflyer.oddi.user.api.files.service.AwsS3Service;
import com.exflyer.oddi.user.api.mustad.dto.AwsKakaoAuthReq;
import com.exflyer.oddi.user.api.mustad.dto.AwsKakaoAuthRes;
import com.exflyer.oddi.user.api.mustad.dto.FederatedAuth;
import com.exflyer.oddi.user.api.mustad.dto.FederatedAuthRes;
import com.exflyer.oddi.user.api.mustad.dto.KakaoAccessTokenInfo;
import com.exflyer.oddi.user.api.mustad.dto.KakaoAuthorize;
import com.exflyer.oddi.user.api.mustad.dto.KakaoMustadAuthorize;
import com.exflyer.oddi.user.api.mustad.dto.KakaoToken;
import com.exflyer.oddi.user.api.mustad.dto.MustadKakaoRes;
import com.exflyer.oddi.user.api.mustad.dto.MustadResult;
import com.exflyer.oddi.user.api.mustad.dto.ProviderMetadata;
import com.exflyer.oddi.user.config.KakaoAuthorizeConfig;
import com.exflyer.oddi.user.exceptions.ApiException;
import com.exflyer.oddi.user.repository.FilesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MustadService {

    @Autowired
    KakaoAuthorizeConfig kakaoAuthorizeConfig;

    @Autowired
    private AwsS3PublicConfig awsS3PublicConfig;

    @Autowired
    private FilesRepository filesRepository;

    @Autowired
    private AwsS3Service awsS3Service;


    @Autowired
    private Gson gson;

    @Autowired
    private HttpClient httpClient;

    public static final String FEDERATEDAUTH_PARAM = "cognito-idp.us-west-2.amazonaws.com";

    public static final String SUCCESS_RESULT_CODE = "0000";
    public static final String ERROR_RESULT_CODE = "9999";
    public static final String X_APP_VERSION = "2.7.14";

    public static final String ACCESS_TOKEN_INFO = "/v1/user/access_token_info";

    public static final String KAKAO_LOGOUT = "/v1/user/logout";

    /**
     * ????????? ??????????????? ?????? ????????????
     * @param code
     * @return
     * @throws ApiException
     * @throws Exception
     */
    public void goMustadKakaoAuthorize() throws ApiException, Exception {

        MustadKakaoRes res = new MustadKakaoRes();
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoAuthorize kakaoReq = new KakaoAuthorize( kakaoAuthorizeConfig);

        try {

            String fullUrl = kakaoAuthorizeConfig.getOauthHost() + kakaoAuthorizeConfig.getOauthAuthorize();

            HttpGet httpGet = new HttpGet(fullUrl);
            httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
            httpGet.setHeader("charset", "utf-8");
            URI uri = new URIBuilder(httpGet.getURI())
                .addParameter("client_id", kakaoAuthorizeConfig.getKey())
                .addParameter("redirect_uri", "http://localhost:28090/user/mustad/kakao/oauth/token")
                .addParameter("response_type", "code")
                .addParameter("state", "oddi")
                .build();
            httpGet.setURI(uri);
            HttpResponse response = httpClient.execute(httpGet);
            response.getStatusLine().getStatusCode();

            if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                res.setResultCode(String.valueOf(response.getStatusLine().getStatusCode()));
                res.setResultMessage("HttpStatus Error");
                log.debug("===================[goMustadKakaoAuthorize ERROR START]====================================");
                log.debug("res {}, ", res);
                log.debug("===================[goMustadKakaoAuthorize ERROR END]====================================");
            }

            String resBody = EntityUtils.toString(response.getEntity());

//            Map result = gson.fromJson(resBody, Map.class);

         //   goMustadKakaoToken(String.valueOf(result.get("code")));

        } catch (Exception e) {
            res.setResultCode(ERROR_RESULT_CODE);
            res.setResultMessage("?????? ??????.");
        }

    }

    /**
     * ????????? ?????? ??????
     * @param code
     * @return
     * @throws ApiException
     * @throws Exception
     */
    public MustadKakaoRes goMustadKakaoToken(String code) throws ApiException, Exception {

        MustadKakaoRes res = new MustadKakaoRes();
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoToken kakaoReq = new KakaoToken(code, kakaoAuthorizeConfig);

        try {

            String fullUrl = kakaoAuthorizeConfig.getOauthHost() + kakaoAuthorizeConfig.getTokenUri();

            HttpPost httpPost = new HttpPost(fullUrl);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
            httpPost.setEntity(new StringEntity(new Gson().toJson(kakaoReq), "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);

            if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                res.setResultCode(String.valueOf(response.getStatusLine().getStatusCode()));
                res.setResultMessage("HttpStatus Error");
                return res;
            }

            String resBody = EntityUtils.toString(response.getEntity());

            MustadKakaoRes result = gson.fromJson(resBody, MustadKakaoRes.class);

            if (StringUtils.isBlank(result.getError())) {
                res.setResultCode(SUCCESS_RESULT_CODE);
                res.setResultMessage("????????????");
            } else {
                res.setError(result.getError());
                res.setResultCode(result.getError_code());
                res.setResultMessage(result.getError_description());
            }

        } catch (Exception e) {
            res.setResultCode(ERROR_RESULT_CODE);
            res.setResultMessage("?????? ??????.");
        }

        return res;
    }

    public KakaoAccessTokenInfo goMustadKakaoAccessTokenInfo(String accessToken) throws ApiException, Exception {
        KakaoAccessTokenInfo result = new KakaoAccessTokenInfo();
        ObjectMapper objectMapper = new ObjectMapper();

        String fullUrl = kakaoAuthorizeConfig.getApiHost() + ACCESS_TOKEN_INFO;

        try {

            HttpGet httpGet = new HttpGet(fullUrl);
            httpGet.setHeader("Authorization", "Bearer " + accessToken);
            URI uri = new URIBuilder(httpGet.getURI()).build();
            httpGet.setURI(uri);
            HttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                result.setResultCode(String.valueOf(response.getStatusLine().getStatusCode()));
                result.setResultMessage("HttpStatus Error");
                return result;
            }

            String resBody = EntityUtils.toString(response.getEntity());
            result = gson.fromJson(resBody, KakaoAccessTokenInfo.class);

            if(result != null) {
                result.setResultCode(SUCCESS_RESULT_CODE);
                result.setResultMessage("????????????");
            }else {
                result.setResultCode(ERROR_RESULT_CODE);
                result.setResultMessage("????????? ????????? ???????????? ????????????.\n ?????? ????????? ????????? ????????????.");
                //????????????????????????
            }

        } catch (Exception e) {
            result.setResultCode(ERROR_RESULT_CODE);
            result.setResultMessage("?????? ??????.");
        }

        return result;
    }

    public KakaoAccessTokenInfo goMustadKakaoLogout(String accessToken) throws ApiException, Exception {
        KakaoAccessTokenInfo result = new KakaoAccessTokenInfo();
        ObjectMapper objectMapper = new ObjectMapper();

        String fullUrl = kakaoAuthorizeConfig.getApiHost() + KAKAO_LOGOUT;

        try {

            HttpPost httpPost = new HttpPost(fullUrl);
            httpPost.setHeader("Authorization", "Bearer " + accessToken);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
            HttpResponse response = httpClient.execute(httpPost);

            String resBody = EntityUtils.toString(response.getEntity());

            result = gson.fromJson(resBody, KakaoAccessTokenInfo.class);

            if(result != null) {
                result.setResultCode(SUCCESS_RESULT_CODE);
                result.setResultMessage("???????????? ???????????????.");
            }else {
                result.setResultCode(ERROR_RESULT_CODE);
                result.setResultMessage("???????????? ??????.");
                log.debug("== [ goMustadKakaoLogout ] ================================");
                log.debug("???????????? ??????");
                log.debug("== [ //goMustadKakaoLogout ] ==============================");
            }

        } catch (Exception e) {
            result.setResultCode(ERROR_RESULT_CODE);
            result.setResultMessage("?????? ??????.");
            log.debug("== [ goMustadKakaoLogout Exception] ================================");
            log.debug("?????? ??????, {}", e);
            log.debug("== [ //goMustadKakaoLogout Exception] ==============================");
        }

        return result;
    }

    /**
     * ???????????? ????????????
     * 1?????? ????????? ??????????????? ????????? ?????? (Cognito ???????????? ????????????)
     * @param awsKakaoAuthReq
     * @return
     * @throws ApiException
     * @throws Exception
     */
    public AwsKakaoAuthRes goMustadAuthorize(AwsKakaoAuthReq awsKakaoAuthReq)
        throws ApiException, Exception {

        AwsKakaoAuthRes res = new AwsKakaoAuthRes();
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoMustadAuthorize kakaoMustadReq = new KakaoMustadAuthorize(awsKakaoAuthReq);

        try {

            String fullUrl = kakaoAuthorizeConfig.getKakaoAwsAuth();

            HttpPost httpPost = new HttpPost(fullUrl);
            httpPost.setHeader("Authorization", "user " + awsKakaoAuthReq.getUserAccessToken());
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "raw");
            httpPost.setEntity(new StringEntity(new Gson().toJson(kakaoMustadReq), "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);


            String resBody = EntityUtils.toString(response.getEntity());

            Map resultMap = gson.fromJson(resBody, Map.class);

            /**
             * TODO ????????? ?????? error ?????? ?????? ???????????? error ????????? ?????? ??? ????????????.
             */
            if (resultMap.get("error") != null) {
                res.setResultCode(resultMap.get("code").toString());
                res.setResultMessage(resultMap.get("message").toString());
            } else {
                res = gson.fromJson(resultMap.get("AuthenticationResult").toString(),AwsKakaoAuthRes.class);
                //???????????? ????????????????????? ??????
                FederatedAuthRes federatedAuthRes  = goFederatedAuth(res.getIdToken());
                res.setFederatedAuth(federatedAuthRes);
                res.setResultCode(SUCCESS_RESULT_CODE);
                res.setResultMessage("????????? ?????? ??????");
            }

        } catch (Exception e) {
            res.setResultCode(ERROR_RESULT_CODE);
            res.setResultMessage("?????? ??????.");
        }

        return res;
    }


    /**
     * ???????????? federatedAuth ??????
     * 2?????? Cognito ???????????? ????????? ????????? ???????????? ??????
     * @param awsKakaoAuthReq
     * @return
     * @throws ApiException
     * @throws Exception
     */
    public FederatedAuthRes goFederatedAuth(String idToken) throws ApiException, Exception {

        log.info("???????????? federatedAuth ?????? START =============================================");

        FederatedAuthRes federatedAuthRes = new FederatedAuthRes();
        String fullUrl = kakaoAuthorizeConfig.getFederatedAuth();

        try{

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(FEDERATEDAUTH_PARAM + kakaoAuthorizeConfig.getUserpoolId(),idToken);

            FederatedAuth federatedAuth = new FederatedAuth(paramMap);

            HttpPost httpPost = new HttpPost(fullUrl);
            httpPost.setHeader("x-device-platform", "ADR");
            httpPost.setHeader("x-device-country", "KR");
            httpPost.setHeader("x-device-language", "ko-KR");
            httpPost.setHeader("x-device-type", "M01");
            httpPost.setHeader("x-app-version", "2.7.15");
            httpPost.setHeader("isEnableAdPush", "1");
            httpPost.setHeader("isEnableAdEmail","1" );
            httpPost.setHeader("x-geo-lon", "120");
            httpPost.setHeader("x-geo-lat", "33");
            httpPost.setHeader("x-is-enable-ad-push", "1");
            httpPost.setHeader("zenabled-ad-push", "1");
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            httpPost.setEntity(new StringEntity(new Gson().toJson(federatedAuth), "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);

            if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                federatedAuthRes.setResultCode(ERROR_RESULT_CODE);
                federatedAuthRes.setResultMessage("Token Error " + String.valueOf(response.getStatusLine().getStatusCode()));
                return federatedAuthRes;
            }

            String resBody = EntityUtils.toString(response.getEntity());

            Map resultMap = gson.fromJson(resBody, Map.class);

            if(resultMap != null) {
                if (resultMap.get("error") != null) {
                    Map errorMap = gson.fromJson(resultMap.get("error").toString(),Map.class);
                    federatedAuthRes.setResultCode(errorMap.get("code").toString());
                    federatedAuthRes.setResultMessage(errorMap.get("message").toString());
                }else {
                    Map resMap = (Map) resultMap.get("result");
                    resMap.put("resultCode", SUCCESS_RESULT_CODE);
                    resMap.put("resultMessage", "????????? ?????? ?????? ??????");
                    federatedAuthRes.setFederatedAuthRes(resMap);
                }

            }else {
                federatedAuthRes.setResultCode(ERROR_RESULT_CODE);
                federatedAuthRes.setResultMessage("????????? ????????? ???????????? ????????????.");
            }

            EntityUtils.consumeQuietly(response.getEntity());
            httpPost.releaseConnection();

            log.info("// ???????????? federatedAuth ?????? END MSG:{},{}", federatedAuthRes.getResultCode(), federatedAuthRes.getResultMessage());

        }catch(Exception e) {
            federatedAuthRes.setResultCode(ERROR_RESULT_CODE);
            federatedAuthRes.setResultMessage("????????? ???????????? ??????.");

            log.info("// ???????????? federatedAuth ?????? ????????? ???????????? ?????? END ========================");
        }
        return federatedAuthRes;
    }

    /**
     * ???????????? ????????? ??????
     * @param idToken
     * @return
     * @throws ApiException
     * @throws Exception
     */
    public MustadResult goMustadContentList(String idToken) throws ApiException, Exception {

        MustadResult result = new MustadResult();

        FederatedAuthRes federatedAuth = goFederatedAuth(idToken);

        log.info("???????????? ????????? ?????? goMustadContentList ?????? START =============================================");

        if (ERROR_RESULT_CODE.equals(federatedAuth.getResultCode())) {
            result.setResultCode(federatedAuth.getResultCode());
            result.setResultMessage(federatedAuth.getResultMessage());
            return result;
        }

        String fullUrl = kakaoAuthorizeConfig.getMyContent();

        try{

            HttpGet httpGet = new HttpGet(fullUrl);
            httpGet.setHeader("X-App-Version", X_APP_VERSION);
            httpGet.setHeader("Authorization", "user " + federatedAuth.getToken());
            URI uri = new URIBuilder(httpGet.getURI())
                .addParameter("page", "1")
                .addParameter("provider", "SignageContent")
                .addParameter("resourcetype", "Content.userboardlist")
                .addParameter("partnerCode", "ODDI")
                .build();
            httpGet.setURI(uri);
            HttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                result.setResultCode(String.valueOf(response.getStatusLine().getStatusCode()));
                result.setResultMessage("HttpStatus Error");
                return result;
            }

            String resBody = EntityUtils.toString(response.getEntity());
            Map resultMap = gson.fromJson(resBody, Map.class);

            if(resultMap != null) {
                if (resultMap.get("error") != null) {
                    Map errorMap = gson.fromJson(resultMap.get("error").toString(),Map.class);
                    result.setResultCode(errorMap.get("code").toString());
                    result.setResultMessage(errorMap.get("message").toString());
                }else {
                    Map resMap = (Map) resultMap.get("result");
                    if(resMap != null) {
                        List<ProviderMetadata> resourcesList = (List<ProviderMetadata>)resMap.get("resources");
                        log.debug("====" + resourcesList );
                        result.setProviderMetadata(resourcesList);
                        result.setResultCode(SUCCESS_RESULT_CODE);
                        result.setResultMessage("????????????");
                        result.setMustadToken("user " + federatedAuth.getToken());
                    }
                }

            }else {
                result.setResultCode(ERROR_RESULT_CODE);
                result.setResultMessage("????????? ????????? ???????????? ????????????.");
            }

            EntityUtils.consumeQuietly(response.getEntity());
            httpGet.releaseConnection();

            log.info("// ???????????? ????????? ??????  END ===== MSG :{},{}", result.getResultCode(), result.getResultMessage());
        }catch(Exception e) {
            result.setResultCode(ERROR_RESULT_CODE);
            result.setResultMessage("???????????? ????????? ?????? ??????.");

            log.info("// ???????????? ????????? ?????? ??????. END =============================================");
        }

        return result;
    }

}
