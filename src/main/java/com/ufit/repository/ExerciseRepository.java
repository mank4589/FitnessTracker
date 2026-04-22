package com.ufit.repository;

import com.ufit.model.Exercise;
import com.ufit.model.enums.DifficultyLevel;
import com.ufit.model.enums.Equipment;
import com.ufit.model.enums.ExerciseType;
import com.ufit.model.enums.MuscleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    
    List<Exercise> findByPrimaryMuscle(MuscleGroup muscleGroup);
    
    List<Exercise> findByExerciseType(ExerciseType exerciseType);
    
    List<Exercise> findByEquipment(Equipment equipment);
    
    List<Exercise> findByDifficultyLevel(DifficultyLevel difficultyLevel);
    
    List<Exercise> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT e FROM Exercise e WHERE e.primaryMuscle = :muscle AND e.exerciseType = :type")
    List<Exercise> findByMuscleAndType(
        @Param("muscle") MuscleGroup muscle,
        @Param("type") ExerciseType type
    );
    
    @Query("SELECT e FROM Exercise e WHERE e.primaryMuscle IN :muscles")
    List<Exercise> findByPrimaryMuscleIn(@Param("muscles") List<MuscleGroup> muscles);
    
    @Query("SELECT e FROM Exercise e WHERE e.exerciseType = 'CARDIO'")
    List<Exercise> findAllCardioExercises();
    
    List<Exercise> findByIsCustom(boolean isCustom);
    
    @Query("SELECT e FROM Exercise e WHERE e.primaryMuscle = :muscle AND e.difficultyLevel = :level")
    List<Exercise> findByMuscleAndDifficulty(
        @Param("muscle") MuscleGroup muscle,
        @Param("level") DifficultyLevel level
    );
}
