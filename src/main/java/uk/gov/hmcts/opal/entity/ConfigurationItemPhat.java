package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitCore;

import java.util.List;

@Entity
@Table(name = "configuration_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "configurationItemId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigurationItemPhat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "configuration_item_id_seq_generator")
    @SequenceGenerator(name = "configuration_item_id_seq_generator", sequenceName = "configuration_item_id_seq",
        allocationSize = 1)
    @Column(name = "configuration_item_id", nullable = false)
    private Long configurationItemId;

    @Column(name = "item_name", length = 50, nullable = false)
    private String itemName;

    @ManyToOne
    @JoinColumn(name = "business_unit_id")
    private BusinessUnitCore businessUnit;

    @Column(name = "item_value")
    private String itemValue;

    @Column(name = "item_values", length = 500)
    private List<String> itemValues;

}
