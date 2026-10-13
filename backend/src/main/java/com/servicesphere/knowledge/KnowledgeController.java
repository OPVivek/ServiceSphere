package com.servicesphere.knowledge;

import com.servicesphere.common.ApiResponse;
import com.servicesphere.common.PagedResponse;
import com.servicesphere.knowledge.dto.ArticleCreateRequest;
import com.servicesphere.knowledge.dto.KnowledgeDto;
import com.servicesphere.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/knowledge")
@Tag(name = "Knowledge Base", description = "Troubleshooting articles and FAQ endpoints")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @GetMapping
    @Operation(summary = "Search knowledge base articles")
    public ResponseEntity<ApiResponse<PagedResponse<KnowledgeDto>>> searchArticles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<KnowledgeDto> articles = knowledgeService.searchArticles(query, category, principal, page, size);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get knowledge article details")
    public ResponseEntity<ApiResponse<KnowledgeDto>> getArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        KnowledgeDto article = knowledgeService.getArticleById(id, principal);
        return ResponseEntity.ok(ApiResponse.success(article));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Create a knowledge article (Agent/Admin only)")
    public ResponseEntity<ApiResponse<KnowledgeDto>> createArticle(
            @Valid @RequestBody ArticleCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        KnowledgeDto article = knowledgeService.createArticle(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Article created", article));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Update a knowledge article (Agent/Admin only)")
    public ResponseEntity<ApiResponse<KnowledgeDto>> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleCreateRequest request
    ) {
        KnowledgeDto article = knowledgeService.updateArticle(id, request);
        return ResponseEntity.ok(ApiResponse.success("Article updated", article));
    }

    @PostMapping("/{id}/feedback")
    @Operation(summary = "Submit helpful/not helpful feedback on article")
    public ResponseEntity<ApiResponse<Void>> recordFeedback(
            @PathVariable Long id,
            @RequestParam boolean helpful
    ) {
        knowledgeService.recordFeedback(id, helpful);
        return ResponseEntity.ok(ApiResponse.success("Feedback recorded", null));
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Get suggested articles for case keywords")
    public ResponseEntity<ApiResponse<List<KnowledgeDto>>> getSuggestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "3") int limit
    ) {
        List<KnowledgeDto> suggestions = knowledgeService.getSuggestions(keyword, limit);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }
}
