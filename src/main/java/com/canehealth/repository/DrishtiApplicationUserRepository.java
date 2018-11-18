package com.canehealth.repository;

import org.gtri.hdap.mdata.jpa.entity.ApplicationUser;
import org.gtri.hdap.mdata.jpa.entity.ApplicationUserId;
import org.gtri.hdap.mdata.jpa.repository.ApplicationUserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DrishtiApplicationUserRepository extends JpaRepository<ApplicationUser, ApplicationUserId> {
    ApplicationUser findByShimmerId(String shimmerId);
    ApplicationUser findByApplicationUserIdEhrIdAndApplicationUserIdShimKey(String ehrId, String shimKey);

    Optional<ApplicationUser> findByApplicationUserId(ApplicationUserId applicationUserId);
}
