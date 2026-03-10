package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.HomePageData;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.service.util_services.ElasticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserHomePageService {

    private final ResumeRepository resumeRepository;

    private final ElasticSearchService elasticSearchService;

    private final ResumeService resumeService;

    @Autowired
    public UserHomePageService(ResumeRepository resumeRepository, ElasticSearchService elasticSearchService,
                               ResumeService resumeService) {
        this.resumeRepository = resumeRepository;
        this.elasticSearchService = elasticSearchService;
        this.resumeService = resumeService;
    }

    @Transactional
    public HomePageData getHomePageData(User user, int pageNumber, int pageSize){
        PageRequest request =
                PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "creationDate"));

        Page<Resume> foundPage = resumeRepository.findByUser(user, request);

        return new HomePageData(foundPage.getContent(), foundPage.getNumber(), foundPage.getTotalElements(),
                foundPage.getTotalPages(), foundPage.getSize());
    }

    @Transactional
    public HomePageData getSearchResult(String name,
                                        User user, int pageNumber, int pageSize){
        List<UUID> matchedResults = elasticSearchService.executeResumeSearch(name);

        PageRequest request = PageRequest.of(pageNumber, pageSize, Sort.by(
                Sort.Order.desc("lastModifiedTime").nullsLast(),
                Sort.Order.desc("creationTime")));


        Page<Resume> matchedResumes = resumeRepository.findByUserAndIdIn(user, matchedResults, request);
        return new HomePageData(matchedResumes.getContent(), matchedResumes.getNumber(),
                matchedResumes.getTotalElements(),
                matchedResumes.getTotalPages(), matchedResumes.getSize());

    }
}
