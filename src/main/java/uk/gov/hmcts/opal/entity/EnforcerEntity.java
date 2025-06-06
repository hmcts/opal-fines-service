package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "enforcers")
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "enforcerId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class EnforcerEntity extends AddressCyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enforcer_id", nullable = false)
    private Long enforcerId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", updatable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "enforcer_code", nullable = false)
    private Short enforcerCode;

    @Column(name = "warrant_reference_sequence", length = 20)
    private String warrantReferenceSequence;

    @Column(name = "warrant_register_sequence")
    private Integer warrantRegisterSequence;
}
