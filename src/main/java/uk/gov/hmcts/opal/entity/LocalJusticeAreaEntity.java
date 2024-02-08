package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "local_justice_areas")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalJusticeAreaEntity extends AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "local_justice_area_id_seq_generator")
    @SequenceGenerator(name = "local_justice_area_id_seq_generator", sequenceName = "local_justice_area_id_seq",
        allocationSize = 1)
    @Column(name = "local_justice_area_id", nullable = false)
    private Short localJusticeAreaId;

}
