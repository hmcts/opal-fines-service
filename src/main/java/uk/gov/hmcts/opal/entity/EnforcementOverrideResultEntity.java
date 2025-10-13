package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Immutable;

@Data
@Entity
@Table(name = "enforcement_override_results")
@Immutable // read-only lookup table
public class EnforcementOverrideResultEntity {

    @Id
    @Column(name = "enforcement_override_result_id", nullable = false)
    private String enforcementOverrideResultId;

    @Column(name = "enforcement_override_result_name", nullable = false)
    private String enforcementOverrideResultName;
}
