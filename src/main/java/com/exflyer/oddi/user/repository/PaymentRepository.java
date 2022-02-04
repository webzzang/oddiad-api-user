package com.exflyer.oddi.user.repository;

import com.exflyer.oddi.user.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PaymentRepository extends JpaRepository<Payment, Long>,
    JpaSpecificationExecutor<Payment> {

    @Query(value = "select * from payment where seq  = :paymentSeq", nativeQuery = true)
    Payment findPaymentInfo(@Param("paymentSeq") Long paymentSeq);

    @Modifying
    @Transactional
    @Query(value = "update payment set type = :type where seq = :paymentSeq", nativeQuery = true)
    void saveByIdType(@Param("type") String type, @Param("paymentSeq") Long paymentSeq);

    @Query(value = "select * from payment where seq  = :paymentSeq and  member_id = :id", nativeQuery = true)
    Payment findPaymentList(@Param("paymentSeq") Long paymentSeq, @Param("id") String id);

    @Query(value = "select count(1) from payment where member_id = :id and type = 'PGT002'", nativeQuery = true)
    int findByFirstId(@Param("id") String id);


}

