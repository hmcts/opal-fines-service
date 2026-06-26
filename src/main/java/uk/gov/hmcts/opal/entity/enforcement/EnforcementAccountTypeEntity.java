package uk.gov.hmcts.opal.entity.enforcement;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import uk.gov.hmcts.opal.entity.LowHighValue;
import uk.gov.hmcts.opal.entity.converter.AccountTypeConverter;
import uk.gov.hmcts.opal.entity.converter.EnforcementAccountTypeConverter;
import uk.gov.hmcts.opal.entity.converter.LowHighValueConverter;

@Entity
@Table(name = "enforcement_account_types")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class EnforcementAccountTypeEntity {

    @Id
    @Column(nullable = false)
    private long enforcementAccountTypeId;

    @Convert(converter = EnforcementAccountTypeConverter.class)
    @Column(nullable = false)
    @NonNull
    private EnforcementAccountType enforcementAccountType;

    @Convert(converter = AccountTypeConverter.class)
    @Column(nullable = false)
    @NonNull
    private AccountType accountType;

    @Convert(converter = LowHighValueConverter.class)
    @Column
    @NonNull
    private LowHighValue accountTypePath;

    @Column
    private BigDecimal minimumBalance;

    @Column
    private Long versionNumber;
}
