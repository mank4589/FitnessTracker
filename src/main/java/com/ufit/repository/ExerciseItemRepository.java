package com.ufit.repository;

import com.ufit.model.ExerciseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseItemRepository extends JpaRepository<ExerciseItem, Long> {

    /** Exact exerciseId match (used for duplicate detection during seeding) */
    Optional<ExerciseItem> findByExerciseId(String exerciseId);

    /** Exact name match (case-insensitive) */
    Optional<ExerciseItem> findByNameIgnoreCase(String name);

    /**
     * Full-text search across name, primaryMuscle, equipment, secondaryMuscles, category.
     * Results ordered by relevance: exact name start first, then name contains, then other field matches.
     */
    @Query("SELECT e FROM ExerciseItem e WHERE " +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.primaryMuscle) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.equipment) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.secondaryMuscles) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.category) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.level) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "ORDER BY " +
           "CASE WHEN LOWER(e.name) = LOWER(:q) THEN 0 " +
           "     WHEN LOWER(e.name) LIKE LOWER(CONCAT(:q, '%')) THEN 1 " +
           "     WHEN LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')) THEN 2 " +
           "     WHEN LOWER(e.primaryMuscle) LIKE LOWER(CONCAT('%', :q, '%')) THEN 3 " +
           "     WHEN LOWER(e.category) LIKE LOWER(CONCAT('%', :q, '%')) THEN 4 " +
           "     ELSE 5 END, " +
           "e.name")
    List<ExerciseItem> searchByTerm(@Param("q") String query);

    /** Count items (used to check if DB is already seeded) */
    long count();
}
