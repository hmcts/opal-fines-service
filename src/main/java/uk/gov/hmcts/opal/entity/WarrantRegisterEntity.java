package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;

@Entity
@Table(name = "warrant_registers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "warrantRegisterId")
public class WarrantRegisterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "warrant_register_id_seq_generator")
    @SequenceGenerator(name = "warrant_register_id_seq_generator", sequenceName = "warrant_register_id_seq",
        allocationSize = 1)
    @Column(name = "warrant_register_id", nullable = false)
    private Long warrantRegisterId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", nullable = false)
    private BusinessUnitFullEntity businessUnit;

    @Column(name = "enforcer_id", nullable = false)
    private Long enforcerId;

    @Column(name = "enforcement_id")
    private Long enforcementId;
}
