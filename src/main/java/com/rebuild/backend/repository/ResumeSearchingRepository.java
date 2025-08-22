package com.rebuild.backend.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeSearchingRepository extends ElasticsearchRepository<ResumeSearchingRepository, String> {
}
