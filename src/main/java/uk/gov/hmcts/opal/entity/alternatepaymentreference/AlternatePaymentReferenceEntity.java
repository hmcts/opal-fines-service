package uk.gov.hmcts.opal.entity.alternatepaymentreference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;
import uk.gov.hmcts.opal.util.VersionedLong;

@Entity
@Table(name = "alternate_payment_references")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AlternatePaymentReferenceEntity implements VersionedLong {

    @Id
    @Column
    @NotNull
    private long alternatePaymentReferenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defendant_account_id", nullable = false)
    private DefendantAccountEntity defendantAccount;

    @Column
    @NotNull
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Relationship relationship;

    @Column
    @NotNull
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", referencedColumnName = "business_unit_id", nullable = false)
    private BusinessUnitEntity businessUnit;

    @Column
    @NotNull
    @Size(max = 18)
    private String aprText;

    @Column
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime createdDatetime;

    @Column
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime updatedDatetime;

    @Column(name = "version_number")
    @Version
    private Long versionNumber;
}
