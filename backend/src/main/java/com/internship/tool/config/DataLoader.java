package com.internship.tool.config;

import com.internship.tool.entity.RegulatoryChange;
import com.internship.tool.repository.RegulatoryChangeRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataLoader implements ApplicationRunner {

    private final RegulatoryChangeRepository regulatoryChangeRepository;

    public DataLoader(RegulatoryChangeRepository regulatoryChangeRepository) {
        this.regulatoryChangeRepository = regulatoryChangeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (regulatoryChangeRepository.count() > 0) {
            return;
        }

        regulatoryChangeRepository.saveAll(buildSeedRecords());
    }

    private List<RegulatoryChange> buildSeedRecords() {
        return List.of(
                seed(1, "RBI", "India", "Banking", "RBI KYC Update", "Updated KYC verification standards for retail accounts.", "Compliance", "NEW", 12),
                seed(2, "SEBI", "India", "Capital Markets", "SEBI Disclosure Refresh", "Quarterly disclosure templates revised for listed entities.", "Disclosure", "UNDER_REVIEW", 18),
                seed(3, "IRDAI", "India", "Insurance", "Policy Servicing Rules", "Customer servicing timelines updated for policy amendments.", "Operations", "IN_PROGRESS", 24),
                seed(4, "RBI", "India", "Payments", "UPI Risk Controls", "Additional monitoring controls introduced for UPI transaction anomalies.", "Risk", "IMPLEMENTED", 31),
                seed(5, "SEBI", "India", "Asset Management", "Mutual Fund Reporting", "Daily liquidity reporting mandated for select schemes.", "Reporting", "CLOSED", 37),
                seed(6, "FCA", "United Kingdom", "Conduct", "Consumer Duty Update", "New customer outcome reviews required for high-risk products.", "Compliance", "NEW", 42),
                seed(7, "MAS", "Singapore", "Banking", "AML Transaction Thresholds", "Threshold-based alerts tightened for suspicious transaction monitoring.", "AML", "UNDER_REVIEW", 48),
                seed(8, "APRA", "Australia", "Prudential", "Capital Adequacy Review", "Internal stress-testing assumptions updated for lending portfolios.", "Capital", "IN_PROGRESS", 53),
                seed(9, "EBA", "European Union", "Banking", "Operational Resilience Notice", "Incident classification criteria aligned with new resilience guidance.", "Resilience", "IMPLEMENTED", 59),
                seed(10, "OCC", "United States", "Banking", "Third-Party Risk Guidance", "Vendor due-diligence documentation expanded for critical suppliers.", "Vendor Risk", "CLOSED", 65),
                seed(11, "RBI", "India", "Lending", "Digital Lending Disclosure", "Lending apps must disclose processing and recovery fees upfront.", "Disclosure", "NEW", 71),
                seed(12, "SEBI", "India", "Markets", "Algo Trading Controls", "Pre-trade checks required for retail-facing algorithmic platforms.", "Trading", "UNDER_REVIEW", 77),
                seed(13, "FINMA", "Switzerland", "Banking", "Outsourcing Register Update", "Material outsourcing arrangements need quarterly register validation.", "Operations", "IN_PROGRESS", 83),
                seed(14, "PRA", "United Kingdom", "Insurance", "Claims Handling Timeframes", "Claims escalation timelines shortened for vulnerable customers.", "Customer Care", "IMPLEMENTED", 88),
                seed(15, "MAS", "Singapore", "Payments", "e-Payments Security", "Enhanced authentication required for high-value payment approvals.", "Security", "CLOSED", 92),
                seed(16, "RBI", "India", "Banking", "Branch Audit Revisions", "Audit sampling rules widened to include digital onboarding exceptions.", "Audit", "NEW", 15),
                seed(17, "SEBI", "India", "Capital Markets", "Holding Period Alerts", "Portfolio holding period exceptions must be tracked daily.", "Monitoring", "UNDER_REVIEW", 22),
                seed(18, "IRDAI", "India", "Insurance", "Underwriting Review", "Medical underwriting workflows need additional approval checkpoints.", "Underwriting", "IN_PROGRESS", 28),
                seed(19, "FCA", "United Kingdom", "Conduct", "Complaint Response SLA", "Complaint acknowledgement SLAs reduced for retail customers.", "Service", "IMPLEMENTED", 34),
                seed(20, "OCC", "United States", "Banking", "Deposit Review Enhancements", "Deposit exception queues require weekly remediation evidence.", "Operations", "CLOSED", 39),
                seed(21, "APRA", "Australia", "Risk", "Climate Risk Scenario", "Climate scenario testing expanded to medium-term lending books.", "Risk", "NEW", 45),
                seed(22, "EBA", "European Union", "Banking", "Data Retention Policy", "Record retention windows standardized across supervisory reports.", "Records", "UNDER_REVIEW", 51),
                seed(23, "FINMA", "Switzerland", "Compliance", "Sanctions Screening", "Name screening rules updated for cross-border counterparties.", "AML", "IN_PROGRESS", 57),
                seed(24, "PRA", "United Kingdom", "Capital", "Liquidity Buffer Guidance", "Liquidity buffer thresholds recalibrated for seasonal stress.", "Capital", "IMPLEMENTED", 63),
                seed(25, "MAS", "Singapore", "Technology", "Cyber Incident Logging", "Security logs must be retained for extended forensic review.", "Cyber", "CLOSED", 69),
                seed(26, "RBI", "India", "Banking", "Customer Escalation Matrix", "Escalation owners mapped for priority complaints across channels.", "Service", "NEW", 74),
                seed(27, "SEBI", "India", "Markets", "Investor Communication Review", "Mass communication templates updated for retail investor notices.", "Communication", "UNDER_REVIEW", 79),
                seed(28, "IRDAI", "India", "Insurance", "Renewal Notice Rules", "Policy renewal notices must be issued with longer lead time.", "Renewals", "IN_PROGRESS", 86),
                seed(29, "FCA", "United Kingdom", "Conduct", "Vulnerable Customer Policy", "Staff training expanded for vulnerable customer identification.", "Training", "IMPLEMENTED", 94),
                seed(30, "OCC", "United States", "Banking", "Reg Change Closure Review", "Completed review of prior regulatory action items and evidence packs.", "Governance", "CLOSED", 100)
        );
    }

    private RegulatoryChange seed(
            int index,
            String source,
            String jurisdiction,
            String category,
            String title,
            String description,
            String priority,
            String status,
            int score
    ) {
        RegulatoryChange change = new RegulatoryChange();
        change.setTitle(title);
        change.setDescription(description);
        change.setSource(source);
        change.setJurisdiction(jurisdiction);
        change.setCategory(category);
        change.setPriority(priority);
        change.setStatus(status);
        change.setScore(score);
        change.setEffectiveDate(LocalDate.now().plusDays(index * 3L));
        change.setPublishedDate(LocalDate.now().minusDays(index));
        return change;
    }
}