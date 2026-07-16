package uk.gov.hmcts.opal.entity.enforcement;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
import org.hibernate.annotations.ColumnTransformer;
import uk.gov.hmcts.opal.entity.LowHighValue;
import uk.gov.hmcts.opal.entity.converter.AccountTypeConverter;
import uk.gov.hmcts.opal.entity.converter.EnforcementAccountTypeConverter;
import uk.gov.hmcts.opal.entity.converter.LowHighValueConverter;
import uk.gov.hmcts.opal.util.Versioned;

@Entity
@Table(name = "enforcement_account_types")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Setter
public class EnforcementAccountTypeEntity implements Versioned {

    @Id
    @Column(nullable = false)
    private long enforcementAccountTypeId;

    @Convert(converter = EnforcementAccountTypeConverter.class)
    @Column(nullable = false)
    @NonNull
    @ColumnTransformer(write = "?::t_enforcement_account_type_enum")
    private EnforcementAccountType enforcementAccountType;

    @Convert(converter = AccountTypeConverter.class)
    @Column(nullable = false)
    @NonNull
    @ColumnTransformer(write = "?::t_account_type_enum")
    private AccountType accountType;

    @Convert(converter = LowHighValueConverter.class)
    @Column
    @NonNull
    @ColumnTransformer(write = "?::t_low_high_value_enum")
    private LowHighValue accountTypePath;

    @Column
    private BigDecimal minimumBalance;

    @Column
    @Version
    private Long versionNumber;

    @Override
    public BigInteger getVersion() {
        return Optional.ofNullable(versionNumber).map(BigInteger::valueOf).orElse(null);
    }
}
