package uk.gov.hmcts.opal.entity.majorcreditor;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.SequenceGenerator;
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
import org.hibernate.annotations.JoinFormula;
import uk.gov.hmcts.opal.entity.AddressEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "major_creditors")
@EqualsAndHashCode(callSuper = true, exclude = "creditorAccountEntity")
@ToString(callSuper = true, exclude = "creditorAccountEntity")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "majorCreditorId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedEntityGraph(name = MajorCreditorEntity.ENTITY_GRAPH_LITE)
@NamedEntityGraph(
    name = MajorCreditorEntity.ENTITY_GRAPH_FULL,
    attributeNodes = {
        @NamedAttributeNode("creditorAccountEntity")
    }
)
public class MajorCreditorEntity extends AddressEntity {

    public static final String ENTITY_GRAPH_LITE = "MajorCreditorEntity.lite";
    public static final String ENTITY_GRAPH_FULL = "MajorCreditorEntity.full";

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinFormula(
        value = "(select ca.creditor_account_id from creditor_accounts ca "
            + "where ca.major_creditor_id = major_creditor_id)",
        referencedColumnName = "creditor_account_id"
    )
    private CreditorAccountEntity creditorAccountEntity;
}
