package uk.gov.hmcts.opal.entity.creditoraccount;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorEntity;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "creditorAccountId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractCreditorAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "creditor_account_id_seq_generator")
    @SequenceGenerator(name = "creditor_account_id_seq_generator", sequenceName = "creditor_account_id_seq",
        allocationSize = 1)
    @Column(name = "creditor_account_id", nullable = false)
    private Long creditorAccountId;

    @Column(name = "account_number", length = 20, nullable = false)
    private String accountNumber;

    @Column(name = "creditor_account_type", length = 2, nullable = false)
    private String creditorAccountType;

    @Column(name = "prosecution_service", nullable = false)
    private boolean prosecutionService;

    @OneToOne
    @JoinColumn(name = "major_creditor_id")
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
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime lastChangedDate;
}
