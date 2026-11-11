package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
    private Long suspenseItemNumber;

    @Column(name = "business_unit_id")
    private Short businessUnitId;

    @Column(name = "business_unit_name")
    private String businessUnitName;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "suspense_item_type")
    private String suspenseItemType;

    @Column(name = "balance")
    private Double balance;

}
