package com.medikiosk.repository;

import com.medikiosk.entity.TriageRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TriageRuleRepository extends JpaRepository<TriageRule, UUID> {

    Optional<TriageRule> findByRuleId(String ruleId);

    List<TriageRule> findByEnabledTrue();

    List<TriageRule> findByEnabledTrueOrderByCreatedAtAsc();
}
