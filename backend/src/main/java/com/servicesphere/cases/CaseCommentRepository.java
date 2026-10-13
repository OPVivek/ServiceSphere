package com.servicesphere.cases;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseCommentRepository extends JpaRepository<CaseComment, Long> {

    @Query("SELECT c FROM CaseComment c WHERE c.supportCase.id = :caseId ORDER BY c.createdAt ASC")
    List<CaseComment> findAllByCaseIdStaff(@Param("caseId") Long caseId);

    @Query("SELECT c FROM CaseComment c WHERE c.supportCase.id = :caseId AND c.internal = false ORDER BY c.createdAt ASC")
    List<CaseComment> findPublicByCaseIdCustomer(@Param("caseId") Long caseId);
}
