package uk.gov.hmcts.opal.entity.alternatepaymentreference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Entity
@Data
@Table(name = "alternate_payment_references")
@Getter
@Builder
public class AlternatePaymentReferenceEntity {

    @Id
    @Column
    @NotNull
    private long alternatePaymentReference;

    @Column
    @NotNull
    private long defendantAccountId;

    @Column
    @NotNull
    private Relationship relationship;

    @Column
    @NotNull
    private Category category;

    @Column
    @NotNull
    private String businessUnitCode;

    @Column
    @NotNull
    private String aprText;
}
