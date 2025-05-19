package uk.gov.hmcts.opal.entity.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public abstract class AbstractResultEntity {

    @Id
    @Column(name = "result_id")
    private String resultId;

    @Column(name = "result_title", length = 50, nullable = false)
    private String resultTitle;

    @Column(name = "result_title_cy", length = 50, nullable = false)
    private String resultTitleCy;

    @Column(name = "result_type", length = 10, nullable = false)
    private String resultType;

    @Column(name = "active", nullable = false)
    private boolean active;

    // maps to imposition_allocation_order in the DTO
    @Column(name = "imposition_allocation_priority")
    private Short impositionAllocationPriority;

    @Column(name = "imposition_creditor", length = 10)
    private String impositionCreditor;
}
