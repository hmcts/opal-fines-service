package uk.gov.hmcts.opal.entity.creditoraccount;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorEntity;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;
import uk.gov.hmcts.opal.util.Versioned;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"businessUnit", "majorCreditor"})
@ToString(exclude = {"businessUnit", "majorCreditor"})
@Entity
@Table(name = "creditor_accounts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "creditorAccountId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedEntityGraph(name = CreditorAccountEntity.ENTITY_GRAPH_LITE)
@NamedEntityGraph(
    name = CreditorAccountEntity.ENTITY_GRAPH_FULL,
    attributeNodes = {
        @NamedAttributeNode("businessUnit"),
        @NamedAttributeNode("majorCreditor")
    }
)
public class CreditorAccountEntity implements Versioned {

    public static final String ENTITY_GRAPH_LITE = "CreditorAccountEntity.lite";
    public static final String ENTITY_GRAPH_FULL = "CreditorAccountEntity.full";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "creditor_account_id_seq_generator")
    @SequenceGenerator(name = "creditor_account_id_seq_generator", sequenceName = "creditor_account_id_seq",
        allocationSize = 1)
    @Column(name = "creditor_account_id", nullable = false)
    private Long creditorAccountId;

    @Column(name = "business_unit_id", insertable = false, updatable = false)
    private Short businessUnitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_unit_id", insertable = false, updatable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "account_number", length = 20, nullable = false)
    private String accountNumber;

    @Column(name = "creditor_account_type", length = 2, nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CreditorAccountType creditorAccountType;

    @Column(name = "prosecution_service", nullable = false)
    private boolean prosecutionService;

    @Column(name = "major_creditor_id", updatable = false)
    private Long majorCreditorId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_creditor_id", insertable = false, updatable = false)
    private MajorCreditorEntity majorCreditor;

    @Column(name = "minor_creditor_party_id")
    private Long minorCreditorPartyId;

    @Column(name = "from_suspense", nullable = false)
    private boolean fromSuspense;

    @Column(name = "hold_payout", nullable = false)
    private boolean holdPayout;

    @Column(name = "pay_by_bacs", nullable = false)
    private boolean payByBacs;

    @Column(name = "bank_sort_code", length = 6)
    private String bankSortCode;

    @Column(name = "bank_account_number", length = 10)
    private String bankAccountNumber;

    @Column(name = "bank_account_name", length = 18)
    private String bankAccountName;

    @Column(name = "bank_account_reference", length = 18)
    private String bankAccountReference;

    @Column(name = "bank_account_type", length = 1)
    private String bankAccountType;

    @Column(name = "last_changed_date")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime lastChangedDate;

    @Column(name = "version_number")
    @Version
    private Long versionNumber;

    @Override
    public BigInteger getVersion() {
        return Optional.ofNullable(versionNumber).map(BigInteger::valueOf).orElse(null);
    }
}
