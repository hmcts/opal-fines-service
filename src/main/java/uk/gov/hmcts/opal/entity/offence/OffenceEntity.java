package uk.gov.hmcts.opal.entity.offence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "offences")
@ToString(callSuper = true)
@XmlType(name = "Offence")
public class OffenceEntity extends AbstractOffenceEntity {

    @Column(name = "business_unit_id", insertable = false, updatable = false)
    private Short businessUnitId;
}
