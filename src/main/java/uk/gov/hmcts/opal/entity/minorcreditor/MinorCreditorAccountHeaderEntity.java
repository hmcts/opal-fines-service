package uk.gov.hmcts.opal.entity.minorcreditor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;

@Getter
@Entity
@Table(name = "v_minor_creditor_account_header")
@Immutable
@SuperBuilder
@NoArgsConstructor
public class MinorCreditorAccountHeaderEntity {

    @Id
    @Column(name = "creditor_account_id")
    private long creditorAccountId;

    @Column(name = "creditor_account_number")
    private String creditorAccountNumber;

    @Column(name = "creditor_account_type")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CreditorAccountType creditorAccountType;

    @Column(name = "version_number")
    private Long versionNumber;

    @Column(name = "party_id")
    private long partyId;

    @Column(name = "title")
    private String title;

    @Column(name = "forenames")
    private String forenames;

    @Column(name = "surname")
    private String surname;

    @Column(name = "organisation")
    private boolean organisation;

    @Column(name = "organisation_name")
    private String organisationName;

    @Column(name = "business_unit_id")
    private short businessUnitId;

    @Column(name = "business_unit_name")
    private String businessUnitName;

    @Column(name = "business_unit_code")
    private String businessUnitCode;

    @Column(name = "welsh_language")
    private boolean welshLanguage;

    @Column(name = "has_associated_defendant")
    private boolean hasAssociatedDefendant;

    @Column(name = "awarded")
    private BigDecimal awarded;

    @Column(name = "paid_out")
    private BigDecimal paidOut;

    @Column(name = "awaiting_payment")
    private BigDecimal awaitingPayment;

    @Column(name = "outstanding")
    private BigDecimal outstanding;
}
