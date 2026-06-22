package uk.gov.hmcts.opal.entity.enforcement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
