package com.exflyer.oddi.user.repository;

import com.exflyer.oddi.user.models.Adv;
import com.exflyer.oddi.user.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AdvRepository  extends JpaRepository<Adv, Long>, JpaSpecificationExecutor<Adv> {

    @Modifying
    @Transactional
    @Query(value = "update adv set progress_code = :progressCode"
        + "     , coupon_number = :couponNumber "
        + "     , discount_price = :dicountPrice "
        + "     , price = :price "
        + "where seq = :advSeq", nativeQuery = true)
    void updateProgress(@Param("progressCode") String progressCode
                , @Param("advSeq") Long advSeq, @Param("couponNumber") String couponNumber
                , @Param("dicountPrice") Integer dicountPrice, @Param("price") Integer price);

    @Modifying
    @Transactional
    @Query(value = "update adv set progress_code = :progressCode "
        + "where seq = :advSeq", nativeQuery = true)
    void updateCancelProgress(@Param("progressCode") String progressCode, @Param("advSeq") Long advSeq);

    @Modifying
    @Transactional
    @Query(value = "update adv set payment_seq = :paymentSeq where seq = :advSeq", nativeQuery = true)
    void updateCoupon(@Param("advSeq") Long advSeq, @Param("paymentSeq") Long paymentSeq);

    @Modifying
    @Transactional
    @Query(value = "update adv set user_check = :userCheck where seq = :advSeq and member_id = :memberId", nativeQuery = true)
    void updateByUserCheck(@Param("advSeq") Long advSeq,@Param("memberId") String memberId, @Param("userCheck") Boolean userCheck);


    @Query(value = "select count(1) from adv where member_id = :memberId and user_check = false", nativeQuery = true)
    int findByMemberId(@Param("memberId") String memberId);

}