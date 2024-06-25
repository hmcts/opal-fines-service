package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "draft-accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "draftAccountId")
public class DraftAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "draft_account_id_seq_generator")
    @SequenceGenerator(name = "draft_account_id_seq_generator", sequenceName = "draft_account_id_seq",
        allocationSize = 1)
    @Column(name = "draft_account_id", nullable = false)
    private Long draftAccountId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", nullable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "created_date")
    @Temporal(TemporalType.DATE)
    private LocalDate createdDate;

    @Column(name = "created_by", length = 20)
    private String createdBy;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private UserEntity createdByUser;

    @Column(name = "validated_date")
    @Temporal(TemporalType.DATE)
    private LocalDate validatedDate;

    @Column(name = "validated_by", length = 20)
    private String validatedBy;

    @ManyToOne
    @JoinColumn(name = "validated_by_user_id")
    private UserEntity validatedByUser;

    @Column(name = "account", columnDefinition = "json")
    @JsonRawValue
    private String account;

    @Column(name = "account_summary_data", columnDefinition = "json")
    @JsonRawValue
    private String accountSummaryData;

    @Column(name = "account_type", length = 30)
    private String accountType;

    @Column(name = "account_status")
    private String accountStatus;

    @Column(name = "status_reason", columnDefinition = "json")
    @JsonRawValue
    private String statusReason;

    @Column(name = "account_id")
    private Long accountId;
}
