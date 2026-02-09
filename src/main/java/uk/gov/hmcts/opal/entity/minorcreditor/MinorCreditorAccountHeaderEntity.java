package uk.gov.hmcts.opal.entity.minorcreditor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

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
    private String creditorAccountType;

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

    @Column(name = "welsh_language")
    private boolean welshLanguage;

    @Column(name = "awarded")
    private BigDecimal awarded;

    @Column(name = "paid_out")
    private BigDecimal paidOut;

    @Column(name = "awaiting_payment")
    private BigDecimal awaitingPayment;

    @Column(name = "outstanding")
    private BigDecimal outstanding;
}
