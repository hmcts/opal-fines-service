package uk.gov.hmcts.opal.entity.enforcement;

import jakarta.persistence.*;
import lombok.*;
import uk.gov.hmcts.opal.dto.EnforcementAccountType;
import uk.gov.hmcts.opal.entity.LowHighValue;
import uk.gov.hmcts.opal.entity.converter.AccountTypeConverter;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class EnforcementAccountTypeEntity {

    @Id
    @Column(nullable = false)
    private Long enforcementAccountTypeId;

    //TODO this enum shouldn't live in the DTO folder
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NonNull
    private EnforcementAccountType enforcementAccountType;

    @Column(nullable = false)
//    @Convert(converter = AccountTypeConverter.class) // TODO need to create a converter and deal with naming conflict
    @NonNull
    private AccountType accountType;

    @Enumerated(EnumType.STRING) // TODO does this need a transformer/converter
    @Column
    @NonNull
    private LowHighValue accountTypePath;

    @Column
    private BigDecimal minimumBalance; //TODO db needs to be changed (there is a db ticket)

    @Column
    private Long versionNumber;
}
