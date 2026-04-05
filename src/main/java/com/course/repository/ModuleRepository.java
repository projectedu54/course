package com.course.repository;

import com.course.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findByCourseId(Long courseId);

    List<Module> findByCourseIdOrderByDisplayOrderAsc(Long courseId);

    @Query("SELECT MAX(m.displayOrder) FROM Module m WHERE m.course.id = :courseId")
    Integer findMaxDisplayOrderByCourseId(@Param("courseId") Long courseId);

    boolean existsByCourseIdAndTitle(Long courseId, String title);

    boolean existsByCourseIdAndTitleAndIdNot(Long courseId, String title, Long id);

    boolean existsByCourseId(Long courseId);


    @Query("SELECT COUNT(m) FROM Module m WHERE m.course.id = :courseId " +
            "AND (SELECT COUNT(t) FROM Topic t WHERE t.module.id = m.id) = 0")
    long countModulesWithoutTopics(@Param("courseId") Long courseId);


}
