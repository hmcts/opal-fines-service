package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.util.KeepAsJsonDeserializer;

import java.time.LocalDateTime;

@Entity
@Table(name = "draft_accounts")
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

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "submitted_by", length = 20, nullable = false)
    private String submittedBy;

    @Column(name = "validated_date")
    private LocalDateTime validatedDate;

    @Column(name = "validated_by", length = 20)
    private String validatedBy;

    @Column(name = "account", columnDefinition = "json", nullable = false)
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String account;

    @Column(name = "account_type", length = 30, nullable = false)
    private String accountType;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "account_snapshot", columnDefinition = "json", nullable = false)
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String accountSnapshot;

    @Column(name = "account_status", length = 30, nullable = false)
    private String accountStatus;

    @Column(name = "status_reason", columnDefinition = "json")
    @JsonDeserialize(using = KeepAsJsonDeserializer.class)
    @JsonRawValue
    private String timelineData;

    @Column(name = "account_number", length = 25)
    private String accountNumber;
}
