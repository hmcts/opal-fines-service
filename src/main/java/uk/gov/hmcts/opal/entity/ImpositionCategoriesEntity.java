package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "imposition_categories",
    uniqueConstraints = {
        @UniqueConstraint(name = "ic_imposition_category_uk", columnNames = "imposition_category")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ImpositionCategoriesEntity {

    @Id
    @Column(name = "imposition_category_id", length = 10, nullable = false)
    private String impositionCategoryId;

    @Column(name = "imposition_category", length = 40, nullable = false)
    private String impositionCategory;

    @Column(name = "item_number", nullable = false)
    private Short itemNumber;
}
