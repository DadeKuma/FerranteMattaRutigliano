package com.github.ferrantemattarutigliano.software.server.repository;

import com.github.ferrantemattarutigliano.software.server.model.entity.IndividualRequest;
import com.github.ferrantemattarutigliano.software.server.model.entity.ThirdParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

public interface IndividualRequestRepository extends JpaRepository<IndividualRequest, Long> {
    Collection<IndividualRequest> findByThirdParty(ThirdParty thirdParty);
    Collection<IndividualRequest> findBySsn(String ssn);

    @Query("SELECT subscription FROM IndividualRequest WHERE id = ?1")
    Boolean isSubscriptionRequest(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE IndividualRequest SET accepted = ?2 WHERE id = ?1")
    void handleRequest(Long id, Boolean accepted);
}
