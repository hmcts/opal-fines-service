package uk.gov.hmcts.opal.entity.draft;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedStoredProcedureQuery;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.StoredProcedureParameter;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.util.Versioned;

import java.time.LocalDateTime;

import static uk.gov.hmcts.opal.entity.draft.StoredProcedureNames.BUSINESS_UNIT_ID;
import static uk.gov.hmcts.opal.entity.draft.StoredProcedureNames.DB_PROC_NAME;
import static uk.gov.hmcts.opal.entity.draft.StoredProcedureNames.DEF_ACC_ID;
import static uk.gov.hmcts.opal.entity.draft.StoredProcedureNames.DEF_ACC_NO;
import static uk.gov.hmcts.opal.entity.draft.StoredProcedureNames.DRAFT_ACC_ID;
import static uk.gov.hmcts.opal.entity.draft.StoredProcedureNames.JPA_PROC_NAME;
import static uk.gov.hmcts.opal.entity.draft.StoredProcedureNames.POSTED_BY;
import static uk.gov.hmcts.opal.entity.draft.StoredProcedureNames.POSTED_BY_NAME;

@Entity
@Table(name = "draft_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "draftAccountId")
@NamedStoredProcedureQuery(name = JPA_PROC_NAME, procedureName = DB_PROC_NAME, parameters = {
    @StoredProcedureParameter(mode = ParameterMode.IN, name = DRAFT_ACC_ID, type = Long.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = BUSINESS_UNIT_ID, type = Short.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = POSTED_BY, type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = POSTED_BY_NAME, type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = DEF_ACC_NO, type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = DEF_ACC_ID, type = Long.class)
})
public class DraftAccountEntity implements Versioned {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "draft_account_id_seq_generator")
    @SequenceGenerator(name = "draft_account_id_seq_generator", sequenceName = "draft_account_id_seq",
        allocationSize = 1)
    @Column(name = "draft_account_id", nullable = false)
    private Long draftAccountId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", nullable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "submitted_by", length = 20, nullable = false)
    private String submittedBy;

    @Column(name = "submitted_by_name", length = 100, nullable = false)
    private String submittedByName;

    @Column(name = "validated_date")
    private LocalDateTime validatedDate;

    @Column(name = "validated_by", length = 20)
    private String validatedBy;

    @Column(name = "validated_by_name", length = 100)
    private String validatedByName;

    @Column(name = "account", columnDefinition = "json", nullable = false)
    @ColumnTransformer(write = "?::jsonb")
    @JsonRawValue
    private String account;

    @Column(name = "account_type", length = 30, nullable = false)
    private String accountType;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "account_snapshot", columnDefinition = "json", nullable = false)
    @ColumnTransformer(write = "?::jsonb")
    @JsonRawValue
    private String accountSnapshot;

    @Column(name = "account_status", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private DraftAccountStatus accountStatus;

    @Column(name = "account_status_date", nullable = false)
    private LocalDateTime accountStatusDate;

    @Column(name = "status_message")
    private String statusMessage;

    @Column(name = "timeline_data", columnDefinition = "json")
    @ColumnTransformer(write = "?::jsonb")
    @JsonRawValue
    private String timelineData;

    @Column(name = "account_number", length = 25)
    private String accountNumber;

    @Column(name = "version_number")
    @Version
    private Long version;

}
