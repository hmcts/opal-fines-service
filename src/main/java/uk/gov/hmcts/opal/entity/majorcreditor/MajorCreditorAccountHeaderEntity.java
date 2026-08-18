package uk.gov.hmcts.opal.entity.majorcreditor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;

@Getter
@Entity
@Table(name = "v_major_creditor_account_header")
@Immutable
@SuperBuilder
@NoArgsConstructor
public class MajorCreditorAccountHeaderEntity {

    @Id
    @Column(name = "creditor_account_id")
    private Long creditorAccountId;

    @Column(name = "creditor_account_number")
    private String creditorAccountNumber;

    @Column(name = "creditor_account_type")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CreditorAccountType creditorAccountType;

    @Column(name = "version_number")
    private Long versionNumber;

    @Column(name = "business_unit_id")
    private Short businessUnitId;

    @Column(name = "business_unit_name")
    private String businessUnitName;

    @Column(name = "business_unit_code")
    private String businessUnitCode;

    @Column(name = "name")
    private String name;

    @Column(name = "awaiting_payout")
    private BigDecimal awaitingPayout;
}
