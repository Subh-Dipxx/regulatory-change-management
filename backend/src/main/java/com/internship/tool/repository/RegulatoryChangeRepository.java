package com.internship.tool.repository;

import com.internship.tool.entity.RegulatoryChange;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegulatoryChangeRepository extends JpaRepository<RegulatoryChange, Long> {

    boolean existsByTitleIgnoreCaseAndSourceIgnoreCase(String title, String source);

    boolean existsByTitleIgnoreCaseAndSourceIgnoreCaseAndIdNot(String title, String source, Long id);

    List<RegulatoryChange> findByEffectiveDateBeforeAndStatusNotIn(LocalDate effectiveDate, Collection<String> statuses);
}
