package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.HomePageResponse;
import com.rebuild.backend.model.responses.resume_responses.ResumePreviewResponse;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.utils.exceptions.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserHomePageService {

    private static final int DEFAULT_PAGE_SIZE = 15;

    private final ResumeRepository resumeRepository;

    @Autowired
    public UserHomePageService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    @Transactional
    public HomePageResponse getHomePageData(User user, int pageNumber) {

        return getSearchResult(null, user, pageNumber);
    }

    @Transactional
    public HomePageResponse getSearchResult(String name,
                                            User user, int pageNumber){

        if (pageNumber < 0)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page number must be greater than or equal to 0");
        }

        PageRequest request = PageRequest.of(pageNumber, DEFAULT_PAGE_SIZE);

        Slice<ResumePreviewResponse> resumeResponses = resumeRepository.findByUserAndNameContaining(user, name, request);
        return new HomePageResponse(resumeResponses.getContent(),
                resumeResponses.hasNext());

    }
}
