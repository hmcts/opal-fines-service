package uk.gov.hmcts.opal.entity.creditoraccount;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "creditor_accounts")
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "creditorAccountId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CreditorAccountEntityLite extends AbstractCreditorAccountEntity {

    @Column(name = "business_unit_id", insertable = false, updatable = false)
    private Short businessUnitId;
}
