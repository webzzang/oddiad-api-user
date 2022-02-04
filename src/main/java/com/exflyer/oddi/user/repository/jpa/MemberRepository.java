package com.exflyer.oddi.user.repository.jpa;

import com.exflyer.oddi.user.models.Member;
import com.exflyer.oddi.user.models.PartnerConfig;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MemberRepository extends JpaRepository<Member, String>, JpaSpecificationExecutor<Member> {

    @Modifying
    @Transactional
    @Query(value = "update member set member_gbn = :memberGbn where id = :memberId", nativeQuery = true)
    void updateMemberGbn(@Param("memberGbn") Boolean memberGbn, @Param("memberId") String memberId);

    @Query(value = "select count(*) from member where email = :email", nativeQuery = true)
    int isDuplicationId(String email);

    @Query(value = "select * from member where email = :email and phone_number = :phoneNumber", nativeQuery = true)
    Member findByIdEmail(String email, String phoneNumber);

    @Query(value = "select * from member where id = :id and password = :password", nativeQuery = true)
    Member findByIdPassword(String id, String password);

    @Query(value = "select * from member where id = :id", nativeQuery = true)
    Member findByPassword(String id);

    @Modifying
    @Transactional
    @Query(value = "update member set password = :password, password_reset = true where id = :id", nativeQuery = true)
    void updateMemberPassword(@Param("id") String id, @Param("password") String password);


    @Modifying
    @Transactional
    @Query(value = "update member set state_code = :stateCode where id = :id", nativeQuery = true)
    void modifyMyResign(@Param("id") String id, @Param("stateCode") String stateCode);

}
