package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "enforcers")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnforcersEntity extends EnforcersCourtsBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enforcer_id", nullable = false)
    private Long enforcerId;

    @Column(name = "enforcer_code", nullable = false)
    private Short enforcerCode;

    @Column(name = "warrant_reference_sequence", length = 20)
    private String warrantReferenceSequence;

    @Column(name = "warrant_register_sequence")
    private Integer warrantRegisterSequence;
}
