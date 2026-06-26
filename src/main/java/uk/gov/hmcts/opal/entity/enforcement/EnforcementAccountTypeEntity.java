package uk.gov.hmcts.opal.entity.enforcement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.opal.entity.LowHighValue;
import uk.gov.hmcts.opal.util.Versioned;

@Entity
@Table(name = "enforcement_account_types")
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Setter
public class EnforcementAccountTypeEntity implements Versioned {

    @Id
    @Column(nullable = false)
    private Long enforcementAccountTypeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NonNull
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private EnforcementAccountType enforcementAccountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NonNull
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column
    @NonNull
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private LowHighValue accountTypePath;

    @Column
    private BigDecimal minimumBalance;

    @Column
    private Long versionNumber;

    @Override
    public BigInteger getVersion() {
        return Optional.ofNullable(versionNumber).map(BigInteger::valueOf).orElse(null);
    }
}
