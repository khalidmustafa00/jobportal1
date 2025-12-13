package com.talimhire.jobportal.repository;

import com.talimhire.jobportal.entity.PasswordResetToken;
import com.talimhire.jobportal.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    PasswordResetToken findByToken(String token);
    @Transactional
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.user = ?1")
    void deleteByUser(Users user);
}
