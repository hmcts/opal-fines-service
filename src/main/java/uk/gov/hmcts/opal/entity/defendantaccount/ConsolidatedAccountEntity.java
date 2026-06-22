package uk.gov.hmcts.opal.entity.defendantaccount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "v_consolidated_accounts")
@Immutable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsolidatedAccountEntity {

    @Column(name = "master_account_id")
    private Long masterAccountId;

    @Id
    @Column(name = "child_account_id")
    private Long childAccountId;

    @Column(name = "child_account_number")
    private String childAccountNumber;

    @Column(name = "child_first_name")
    private String childFirstName;

    @Column(name = "child_last_name")
    private String childLastName;

    @Column(name = "child_date_imposed")
    private LocalDate childDateImposed;

    @Column(name = "child_imposed_by")
    private String childImposedBy;

    @Column(name = "child_reference")
    private String childReference;
}
