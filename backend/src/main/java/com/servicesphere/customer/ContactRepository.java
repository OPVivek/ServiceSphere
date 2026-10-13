package com.servicesphere.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    Optional<Contact> findByUserId(Long userId);
    List<Contact> findByAccountId(Long accountId);

    @Query("SELECT c FROM Contact c JOIN c.user u LEFT JOIN c.account a WHERE " +
           "(:accountId IS NULL OR a.id = :accountId) AND " +
           "(:query IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Contact> searchContacts(@Param("accountId") Long accountId, @Param("query") String query, Pageable pageable);
}
