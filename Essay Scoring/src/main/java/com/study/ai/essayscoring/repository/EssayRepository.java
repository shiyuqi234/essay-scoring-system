package com.study.ai.essayscoring.repository;

import com.study.ai.essayscoring.dto.EssaySummaryDTO;
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

    /**
     * 统计聚合：作文总数 / 学生数 / 平均分。
     * 下推到数据库计算，避免「查出所有作文再在内存遍历」的全表扫描 + 内存压力。
     * 返回单行 Object[]：[总作文数(Long), 学生数(Long), 平均分(Double|null)]
     */
    @Query("SELECT COUNT(e), COUNT(DISTINCT e.studentId), AVG(s.totalScore) FROM Essay e LEFT JOIN e.score s")
    Object[] aggregateStatistics();

    /**
     * 列表摘要投影（不含 content / feedbacks，避免关 OSIV 后序列化触发懒加载）。
     */
    @Query("SELECT new com.study.ai.essayscoring.dto.EssaySummaryDTO(" +
            "e.id, e.studentName, e.studentId, e.title, e.grade, e.essayType, e.wordCount, e.submitTime, s.totalScore) " +
            "FROM Essay e LEFT JOIN e.score s ORDER BY e.submitTime DESC")
    List<EssaySummaryDTO> findAllSummaries();

    /**
     * 按学生查询列表摘要投影。
     */
    @Query("SELECT new com.study.ai.essayscoring.dto.EssaySummaryDTO(" +
            "e.id, e.studentName, e.studentId, e.title, e.grade, e.essayType, e.wordCount, e.submitTime, s.totalScore) " +
            "FROM Essay e LEFT JOIN e.score s WHERE e.studentId = :studentId ORDER BY e.submitTime DESC")
    List<EssaySummaryDTO> findSummariesByStudentId(@Param("studentId") String studentId);
}
