package uk.gov.hmcts.opal.entity.enforcement;

import jakarta.persistence.*;
import lombok.*;
import uk.gov.hmcts.opal.dto.EnforcementAccountType;
import uk.gov.hmcts.opal.entity.LowHighValue;

import java.math.BigDecimal;

@Entity
@Table(name = "enforcement_account_types")
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NonNull
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column
    @NonNull
    private LowHighValue accountTypePath;

    @Column
    private BigDecimal minimumBalance; //TODO db needs to be changed (there is a db ticket)

    @Column
    private Long versionNumber;
}
