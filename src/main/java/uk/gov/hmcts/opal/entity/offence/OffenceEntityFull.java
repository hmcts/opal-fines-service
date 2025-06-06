package uk.gov.hmcts.opal.entity.offence;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "offences")
@ToString(callSuper = true)
@SuperBuilder
@XmlType(name = "Offence")
public class OffenceEntityFull extends AbstractOffenceEntity {

    @ManyToOne
    @JoinColumn(name = "business_unit_id")
    private BusinessUnitEntity businessUnit;
}
