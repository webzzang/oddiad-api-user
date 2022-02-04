package com.exflyer.oddi.user.exceptions;

import com.exflyer.oddi.user.enums.ApiResponseCodes;
import com.exflyer.oddi.user.share.dto.ApiResponseDto;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception 이 발생 할 경우 캐칭해서 응답을 만들어 주는 클래스 ControllerAdvice 를 이용하여 구현함.
 *
 * 프로젝트 성격에 따라 다르지만 본 소스는 http.status.ok(200) 고정 response body 에서 code 와 message 를 응답함
 */
@ControllerAdvice
@ResponseBody
@Slf4j
public class ApiExceptionAdvice {

  @ExceptionHandler(value = {ApiException.class})
  @ResponseStatus(HttpStatus.OK)
  public Object handleEpisodeAuthException(ApiException e) {
    return new ApiResponseDto(e.getApiResponseCodes());
  }

  @ResponseStatus(HttpStatus.OK)
  @ExceptionHandler(value = {NullPointerException.class})
  public Object handleNPE(NullPointerException e) {
    e.printStackTrace();
    return new ApiResponseDto(ApiResponseCodes.INTERNAL);
  }

  @ResponseStatus(HttpStatus.OK)
  @ExceptionHandler(value = {IOException.class})
  public Object handleIOException(IOException e) {
    return new ApiResponseDto(ApiResponseCodes.INTERNAL);
  }


  @ResponseStatus(HttpStatus.OK)
  @ExceptionHandler(value = {NoSuchFieldError.class})
  public Object handleNoSuchFieldError(NoSuchFieldError e) {
    return new ApiResponseDto(ApiResponseCodes.INTERNAL);
  }

  @ResponseStatus(HttpStatus.OK)
  @ExceptionHandler(value = {HttpMessageNotReadableException.class})
  public Object handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    return new ApiResponseDto(ApiResponseCodes.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.OK)
  @ExceptionHandler(value = {BindException.class})
  public Object handleBindException(BindException e) {
    String errorField = e.getBindingResult().getFieldErrors().get(0).getField();
    String errorMessage = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
    return new ApiResponseDto(ApiResponseCodes.BAD_REQUEST, errorField + " " + errorMessage);
  }

  @ResponseStatus(HttpStatus.OK)
  @ExceptionHandler(value = {MissingPathVariableException.class})
  public Object handleMissingPathVariableException(MissingPathVariableException e) {
    String message = String.format("Missing URI template variable '%s'", e.getVariableName());
    return new ApiResponseDto(ApiResponseCodes.BAD_REQUEST, message);
  }
}
