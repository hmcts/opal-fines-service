package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment_terms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PaymentTermsEntity {

    @Id
    @Column(name = "payment_terms_id")
    private Long paymentTermsId;

    @ManyToOne
    @JoinColumn(name = "defendant_account_id", referencedColumnName = "defendant_account_id", nullable = false)
    private DefendantAccountEntity defendantAccount;


    @Column(name = "posted_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate postedDate;

    @Column(name = "posted_by")
    private String postedBy;

    @Column(name = "terms_type_code", nullable = false)
    private String termsTypeCode;

    @Column(name = "effective_date")
    @Temporal(TemporalType.DATE)
    private LocalDate effectiveDate;

    @Column(name = "instalment_period")
    private String instalmentPeriod;

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

    @Column(name = "posted_by_aad", length = 100)
    private String postedByAad;

}
