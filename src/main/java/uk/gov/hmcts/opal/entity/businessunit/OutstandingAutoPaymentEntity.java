package uk.gov.hmcts.opal.entity.businessunit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "v_outstanding_auto_payment_count")
@Immutable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutstandingAutoPaymentEntity {

    @Id
    @Column(name = "business_unit_id")
    private Short businessUnitId;

    @Column(name = "business_unit_name")
    private String businessUnitName;

    @Column(name = "files_to_process_count")
    private Long filesToProcessCount;

    @Column(name = "tills_to_allocate_count")
    private Long tillsToAllocateCount;
}
