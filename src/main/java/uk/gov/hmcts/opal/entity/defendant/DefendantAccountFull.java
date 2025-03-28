package uk.gov.hmcts.opal.entity.defendant;

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
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitCore;
import uk.gov.hmcts.opal.entity.court.CourtEntity;

@Entity
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "defendant_accounts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "defendantAccountId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DefendantAccountFull extends DefendantAccount {

    @ManyToOne
    @JoinColumn(name = "business_unit_id", insertable = false, updatable = false)
    private BusinessUnitCore businessUnit;

    @ManyToOne
    @JoinColumn(name = "enforcing_court_id", insertable = false, updatable = false)
    private CourtEntity.Lite enforcingCourt;

    @ManyToOne
    @JoinColumn(name = "last_hearing_court_id", insertable = false, updatable = false)
    private CourtEntity.Lite lastHearingCourt;

    // @OneToMany(mappedBy = "defendantAccount", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    // private List<DefendantAccountPartiesEntity> parties;
}
