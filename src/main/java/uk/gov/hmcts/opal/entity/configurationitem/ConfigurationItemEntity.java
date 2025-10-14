package uk.gov.hmcts.opal.entity.configurationitem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public abstract class ConfigurationItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "configuration_item_id_seq_generator")
    @SequenceGenerator(name = "configuration_item_id_seq_generator", sequenceName = "configuration_item_id_seq",
        allocationSize = 1)
    @Column(name = "configuration_item_id", nullable = false)
    private Long configurationItemId;

    @Column(name = "business_unit_id", insertable = false, updatable = false)
    private Short businessUnitId;

    @Column(name = "item_name", length = 50, nullable = false)
    private String itemName;

    @Column(name = "item_value")
    private String itemValue;

    @Column(name = "item_values", length = 500)
    private List<String> itemValues;

    @Entity
    @Getter
    @EqualsAndHashCode(callSuper = true)
    @Table(name = "configuration_items")
    @SuperBuilder
    @NoArgsConstructor
    public static class Lite extends ConfigurationItemEntity {
    }
}
