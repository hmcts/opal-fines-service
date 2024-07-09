package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "configuration_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigurationItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "configuration_item_id_seq_generator")
    @SequenceGenerator(name = "configuration_item_id_seq_generator", sequenceName = "configuration_item_id_seq",
        allocationSize = 1)
    @Column(name = "configuration_item_id", nullable = false)
    private Long configurationItemId;

    @Column(name = "item_name", length = 50, nullable = false)
    private String itemName;

    @Column(name = "business_unit_id")
    private Short businessUnitId;

    @Column(name = "item_value")
    private String itemValue;

    @Column(name = "item_values", length = 500)
    private List<String> itemValues;

}
