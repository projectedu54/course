package com.course.repository;

import com.course.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    List<Tag> findByNameIn(Set<String> names);

    @Modifying
    @Transactional
    @Query(
            value = "INSERT IGNORE INTO tag_tbl(name) VALUES (:names)", nativeQuery = true
    )
    void bulkInsertIgnoreDuplicates(@Param("names") Set<String> names);
}