package uk.gov.hmcts.opal.entity.majorcreditor;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.CreditorAccountEntity;

@Getter
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "major_creditors")
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "majorCreditorId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MajorCreditorEntity extends AbstractMajorCreditorEntity {

    @OneToOne(mappedBy = "majorCreditor")
    private CreditorAccountEntity creditorAccountEntity;
}
