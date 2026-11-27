package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;

@Getter
@Entity
@Table(name = "v_suspense_item_summary")
@Immutable
@SuperBuilder
@NoArgsConstructor
public class SuspenseItemSummaryEntity {

    @Id
    @Column(name = "suspense_item_id")
    private Long id;

    @Column(name = "suspense_item_number")
    private Short suspenseItemNumber;

    @Column(name = "business_unit_id")
    private Long businessUnitId;

    @Column(name = "business_unit_name")
    private String businessUnitName;

    @Column(name = "created_date")
    private String createdDate;

    @Column(name = "suspense_item_type")
    private String suspenseItemType;

    @Column(name = "balance")
    private Double balance;

}
