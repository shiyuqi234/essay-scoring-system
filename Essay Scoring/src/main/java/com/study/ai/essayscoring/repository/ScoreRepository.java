package com.study.ai.essayscoring.repository;

import com.study.ai.essayscoring.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 评分Repository
 */
@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    
    /**
     * 根据作文ID查询评分
     */
    Optional<Score> findByEssayId(Long essayId);
}
