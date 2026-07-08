package com.study.ai.essayscoring.repository;

import com.study.ai.essayscoring.entity.Essay;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 作文Repository
 */
@Repository
public interface EssayRepository extends JpaRepository<Essay, Long> {

    /**
     * 根据学生ID查询作文列表（饿加载 Score，避免 LAZY 序列化异常）
     */
    @EntityGraph(attributePaths = {"score"})
    List<Essay> findByStudentIdOrderBySubmitTimeDesc(@Param("studentId") String studentId);

    /**
     * 查询所有作文（饿加载 Score）
     */
    @EntityGraph(attributePaths = {"score"})
    List<Essay> findAll();

    /**
     * 根据年级查询作文列表
     */
    @EntityGraph(attributePaths = {"score"})
    List<Essay> findByGradeOrderBySubmitTimeDesc(@Param("grade") String grade);

    /**
     * 根据文体类型查询作文列表
     */
    @EntityGraph(attributePaths = {"score"})
    List<Essay> findByEssayTypeOrderBySubmitTimeDesc(@Param("essayType") String essayType);

    /**
     * 查询作文及其评分结果
     */
    @Query("SELECT e FROM Essay e LEFT JOIN FETCH e.score WHERE e.id = :id")
    Optional<Essay> findByIdWithScore(@Param("id") Long id);

    /**
     * 查询作文及其所有反馈
     */
    @Query("SELECT e FROM Essay e LEFT JOIN FETCH e.feedbacks WHERE e.id = :id")
    Optional<Essay> findByIdWithFeedbacks(@Param("id") Long id);
}
