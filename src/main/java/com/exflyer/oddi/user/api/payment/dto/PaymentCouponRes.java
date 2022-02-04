package com.exflyer.oddi.user.api.payment.dto;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;

@Data
public class PaymentCouponRes implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "쿠폰번호", position = 0)
    private Long couponSeq;

    @ApiModelProperty(value = "쿠폰이름", position = 0)
    private String couponName;

    @ApiModelProperty(value = "할인금액", position = 0)
    private String discountPrice;

    @ApiModelProperty(value = "쿠폰코드", position = 0)
    private String couponCode;

}
