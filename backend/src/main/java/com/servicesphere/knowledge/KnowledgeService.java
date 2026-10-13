package com.servicesphere.knowledge;

import com.servicesphere.common.PagedResponse;
import com.servicesphere.common.exception.ResourceNotFoundException;
import com.servicesphere.common.exception.UnauthorizedException;
import com.servicesphere.knowledge.dto.ArticleCreateRequest;
import com.servicesphere.knowledge.dto.KnowledgeDto;
import com.servicesphere.security.Role;
import com.servicesphere.security.UserPrincipal;
import com.servicesphere.user.User;
import com.servicesphere.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeService {

    private final KnowledgeArticleRepository articleRepository;
    private final UserRepository userRepository;

    public KnowledgeService(KnowledgeArticleRepository articleRepository, UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<KnowledgeDto> searchArticles(String query, String category, UserPrincipal principal, int page, int size) {
        boolean publishedOnly = (principal == null || principal.getRole() == Role.CUSTOMER);
        Page<KnowledgeArticle> articles = articleRepository.searchArticles(
                publishedOnly,
                category,
                query,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return PagedResponse.of(articles.map(KnowledgeDto::fromEntity));
    }

    @Transactional
    public KnowledgeDto getArticleById(Long id, UserPrincipal principal) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge article", "id", id));

        boolean isCustomer = (principal == null || principal.getRole() == Role.CUSTOMER);
        if (isCustomer && !article.isPublished()) {
            throw new ResourceNotFoundException("Knowledge article", "id", id);
        }

        // Increment view count
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);

        return KnowledgeDto.fromEntity(article);
    }

    @Transactional
    public KnowledgeDto createArticle(ArticleCreateRequest request, UserPrincipal principal) {
        User author = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        KnowledgeArticle article = new KnowledgeArticle(
                request.getTitle().trim(),
                request.getSummary().trim(),
                request.getBody(),
                request.getCategory().trim(),
                request.getTags(),
                request.isPublished(),
                author
        );
        return KnowledgeDto.fromEntity(articleRepository.save(article));
    }

    @Transactional
    public KnowledgeDto updateArticle(Long id, ArticleCreateRequest request) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge article", "id", id));

        article.setTitle(request.getTitle().trim());
        article.setSummary(request.getSummary().trim());
        article.setBody(request.getBody());
        article.setCategory(request.getCategory().trim());
        article.setTags(request.getTags());
        article.setPublished(request.isPublished());

        return KnowledgeDto.fromEntity(articleRepository.save(article));
    }

    @Transactional
    public void recordFeedback(Long id, boolean helpful) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge article", "id", id));
        if (helpful) {
            article.setHelpfulCount(article.getHelpfulCount() + 1);
        } else {
            article.setNotHelpfulCount(article.getNotHelpfulCount() + 1);
        }
        articleRepository.save(article);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeDto> getSuggestions(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return articleRepository.findSuggestionsByKeyword(keyword.trim(), PageRequest.of(0, limit))
                .stream()
                .map(KnowledgeDto::fromEntity)
                .collect(Collectors.toList());
    }
}
