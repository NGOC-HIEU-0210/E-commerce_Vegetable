package com.hieu.repository;

import com.hieu.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findByActiveTrue();

    List<Category> findByParentCategoryIsNullAndActiveTrue();
    @Query("SELECT c FROM Category c WHERE c.active = true "
            + "AND (:keyword = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Category> searchActive(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Category c "
            + "WHERE (:keyword = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Category> searchAll(@Param("keyword") String keyword, Pageable pageable);
}
