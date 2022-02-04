package com.exflyer.oddi.user.api.payment.dto;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;

@Data
public class PaymentCouponReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "회원id", position = 0)
    private String memberId;

    @ApiModelProperty(value = "순번", position = 0)
    private String expiredDate;

    @ApiModelProperty(value = "쿠폰사용여부", position = 0)
    private boolean couponUsable;

    @ApiModelProperty(value = "사용여부", position = 0)
    private boolean promotionUsable;

    @ApiModelProperty(value = "첫결제여부", position = 0)
    private int paymentFirst;

    @ApiModelProperty(value = "가입자 쿠폰 사용기한(첫결제시,가입시)", position = 0)
    private String memberCouponCode;


}
