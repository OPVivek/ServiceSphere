package com.servicesphere.knowledge;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    @Query("SELECT k FROM KnowledgeArticle k WHERE " +
           "(:publishedOnly = false OR k.published = true) AND " +
           "(:category IS NULL OR k.category = :category) AND " +
           "(:query IS NULL OR LOWER(k.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(k.summary) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(k.body) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(k.tags) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<KnowledgeArticle> searchArticles(
            @Param("publishedOnly") boolean publishedOnly,
            @Param("category") String category,
            @Param("query") String query,
            Pageable pageable
    );

    @Query("SELECT k FROM KnowledgeArticle k WHERE k.published = true AND (" +
           "LOWER(k.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(k.tags) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(k.category) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<KnowledgeArticle> findSuggestionsByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
