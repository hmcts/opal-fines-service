package uk.gov.hmcts.opal.entity;

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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "prisons")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrisonEntity extends AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prison_id_seq_generator")
    @SequenceGenerator(name = "prison_id_seq_generator", sequenceName = "prison_id_seq", allocationSize = 1)
    @Column(name = "prison_id", nullable = false)
    private Long prisonId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", referencedColumnName = "business_unit_id", nullable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "prison_code", length = 4, nullable = false)
    private String prisonCode;

}
