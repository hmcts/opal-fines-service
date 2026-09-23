package uk.gov.hmcts.opal.entity.alternatepaymentreference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

@Entity
@Table(name = "alternate_payment_references")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AlternatePaymentReferenceEntity {

    @Id
    @Column
    @NotNull
    private long alternatePaymentReferenceId;

    @Column
    @NotNull
    private long defendantAccountId;

    @Column
    @NotNull
    @Enumerated(EnumType.STRING)
    @ColumnTransformer(read = "relationship::text", write = "?::t_apr_relationship_enum")
    private Relationship relationship;

    @Column
    @NotNull
    @Enumerated(EnumType.STRING)
    @ColumnTransformer(read = "category::text", write = "?::t_apr_category_enum")
    private Category category;

    @Column
    @NotNull
    private String businessUnitCode;

    @Column
    @NotNull
    private String aprText;

    @Column
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime createdDatetime;

    @Column
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime updatedDatetime;
}
