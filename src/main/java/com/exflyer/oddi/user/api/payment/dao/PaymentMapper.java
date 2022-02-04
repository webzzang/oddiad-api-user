package com.exflyer.oddi.user.api.payment.dao;

import com.exflyer.oddi.user.api.payment.dto.AdvUserCancelReq;
import com.exflyer.oddi.user.api.payment.dto.PaymentCancelReq;
import com.exflyer.oddi.user.api.payment.dto.PaymentCancelRes;
import com.exflyer.oddi.user.api.payment.dto.PaymentCouponReq;
import com.exflyer.oddi.user.api.payment.dto.PaymentCouponRes;
import com.exflyer.oddi.user.api.payment.dto.PaymentDetailResult;
import com.exflyer.oddi.user.api.payment.dto.PaymentRes;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentMapper {

    PaymentDetailResult findAdvDetail(Long advSeq);
    List<PaymentCouponRes> findPaymentCoupon(PaymentCouponReq paymentCouponReq);
    PaymentCancelRes fnidPaymentInfo(PaymentCancelReq req);

    PaymentRes findByPayment(Long paymentSeq);

    Integer findByPromotionInfo(String couponCode);

    AdvUserCancelReq findAdvCancelUserInfo(Long advSeq);

}
