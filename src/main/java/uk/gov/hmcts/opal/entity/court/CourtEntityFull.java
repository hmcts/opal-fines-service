package uk.gov.hmcts.opal.entity.court;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "courts")
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "courtId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CourtEntityFull extends CourtEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "business_unit_id")
    private BusinessUnit.Lite businessUnit;

    @ManyToOne
    @JoinColumn(name = "parent_court_id")
    private Lite parentCourt;

    @ManyToOne
    @JoinColumn(name = "local_justice_area_id")
    private LocalJusticeAreaEntity localJusticeArea;

}
