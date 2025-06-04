package uk.gov.hmcts.opal.entity.offence;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;

@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "offences")
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlType(name = "Offence")
public class OffenceEntityFull extends AbstractOffenceEntity {

    @ManyToOne
    @JoinColumn(name = "business_unit_id")
    private BusinessUnitEntity businessUnit;
}
