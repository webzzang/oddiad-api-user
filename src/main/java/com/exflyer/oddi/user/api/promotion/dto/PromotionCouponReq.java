package com.exflyer.oddi.user.api.promotion.dto;

import com.exflyer.oddi.user.share.LocalDateUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PromotionCouponReq {

    @ApiModelProperty(value = "회원아이디", position = 0)
    private String memberId;

    @ApiModelProperty(value = "쿠폰코드", position = 0)
    private String couponCode;

    @ApiModelProperty(value = "만료날짜", position = 0)
    private String expiredDate;

    public PromotionCouponReq(String couponCode, String memberId) {
        this.memberId = memberId;
        this.couponCode = couponCode;
        this.expiredDate = LocalDateUtils.krNowByFormatter("yyyyMMdd");
    }
}
