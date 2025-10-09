package uk.gov.hmcts.opal.entity.majorcreditor;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.AddressEntity;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "majorCreditorId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class MajorCreditorEntity extends AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "major_creditor_id_seq_generator")
    @SequenceGenerator(name = "major_creditor_id_seq_generator", sequenceName = "major_creditor_id_seq",
        allocationSize = 1)
    @Column(name = "major_creditor_id", nullable = false)
    private Long majorCreditorId;

    @Column(name = "business_unit_id", insertable = false, updatable = false)
    private Short businessUnitId;

    @Column(name = "major_creditor_code", length = 4)
    private String majorCreditorCode;

    @Column(name = "contact_name", length = 35)
    private String contactName;

    @Column(name = "contact_telephone", length = 35)
    private String contactTelephone;

    @Column(name = "contact_email", length = 80)
    private String contactEmail;

    @Entity
    @Getter
    @EqualsAndHashCode(callSuper = true)
    @Table(name = "offences")
    @SuperBuilder
    @NoArgsConstructor
    public static class Lite extends MajorCreditorEntity {
    }
}
