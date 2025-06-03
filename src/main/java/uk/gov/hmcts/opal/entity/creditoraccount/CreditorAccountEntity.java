package uk.gov.hmcts.opal.entity.creditoraccount;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;

@Entity
@Table(name = "creditor_accounts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder  // Change from @Builder to @SuperBuilder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "creditorAccountId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CreditorAccountEntity extends AbstractCreditorAccountEntity {

    @ManyToOne
    @JoinColumn(name = "business_unit_id", updatable = false)
    private BusinessUnitEntity businessUnit;
}
