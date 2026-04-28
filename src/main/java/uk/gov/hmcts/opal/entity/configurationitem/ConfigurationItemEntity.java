package uk.gov.hmcts.opal.entity.configurationitem;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "configuration_items")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "configurationItemId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedEntityGraph(name = ConfigurationItemEntity.ENTITY_GRAPH_LITE)
@NamedEntityGraph(
    name = ConfigurationItemEntity.ENTITY_GRAPH_FULL,
    attributeNodes = {
        @NamedAttributeNode("businessUnit")
    }
)
public class ConfigurationItemEntity {

    public static final String ENTITY_GRAPH_LITE = "ConfigurationItemEntity.lite";
    public static final String ENTITY_GRAPH_FULL = "ConfigurationItemEntity.full";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "configuration_item_id_seq_generator")
    @SequenceGenerator(name = "configuration_item_id_seq_generator", sequenceName = "configuration_item_id_seq",
        allocationSize = 1)
    @Column(name = "configuration_item_id", nullable = false)
    private Long configurationItemId;

    @Column(name = "business_unit_id", insertable = false, updatable = false)
    private Short businessUnitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_unit_id", insertable = false, updatable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "item_name", length = 50, nullable = false)
    private String itemName;

    @Column(name = "item_value")
    private String itemValue;

    @Column(name = "item_values", length = 500)
    private List<String> itemValues;
}
