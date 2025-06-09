package uk.gov.hmcts.opal.entity.configurationitem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
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
public abstract class AbstractConfigurationItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "configuration_item_id_seq_generator")
    @SequenceGenerator(name = "configuration_item_id_seq_generator", sequenceName = "configuration_item_id_seq",
        allocationSize = 1)
    @Column(name = "configuration_item_id", nullable = false)
    private Long configurationItemId;

    @Column(name = "item_name", length = 50, nullable = false)
    private String itemName;

    @Column(name = "item_value")
    private String itemValue;

    @Column(name = "item_values", length = 500)
    private List<String> itemValues;
}
