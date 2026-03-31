package uk.gov.hmcts.opal.entity.paymentterms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.util.LocalDateAdapter;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

@Entity
@Table(name = "payment_terms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentTermsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_terms_id_seq_generator")
    @SequenceGenerator(name = "payment_terms_id_seq_generator",
        sequenceName = "payment_terms_id_seq",
        allocationSize = 1)
    @Column(name = "payment_terms_id", nullable = false)
    private Long paymentTermsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defendant_account_id", referencedColumnName = "defendant_account_id", nullable = false)
    private DefendantAccountEntity defendantAccount;


    @Column(name = "posted_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime postedDate;

    @Column(name = "posted_by")
    private String postedBy;

    @Column(name = "terms_type_code", nullable = false, columnDefinition = "t_terms_type_code_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TermsTypeCode termsTypeCode;

    @Column(name = "effective_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate effectiveDate;

    @Column(name = "instalment_period", columnDefinition = "t_instalment_period_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private InstalmentPeriod instalmentPeriod;

    @Column(name = "instalment_amount")
    private BigDecimal instalmentAmount;

    @Column(name = "instalment_lump_sum")
    private BigDecimal instalmentLumpSum;

    @Column(name = "jail_days")
    private Integer jailDays;

    @Column(name = "extension")
    private Boolean extension;

    @Column(name = "account_balance")
    private BigDecimal accountBalance;

    @Column(name = "posted_by_name", length = 100)
    private String postedByUsername;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "reason_for_extension")
    private String reasonForExtension;

    /**
     * This avoids potential infinite recursion issues when
     * printing entities with bidirectional relationships.
     */
    @Override
    public String toString() {
        return "PaymentTermsEntity{"
            + "id=" + paymentTermsId
            + ", termsType=" + termsTypeCode
            + '}';
    }
}
