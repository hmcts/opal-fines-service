package uk.gov.hmcts.opal.entity.businessunit;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.converter.BusinessUnitTypeConverter;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"parentBusinessUnit", "configurationItems"})
@Entity
@Table(name = "business_units")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "businessUnitId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedEntityGraph(
    name = BusinessUnitEntity.ENTITY_GRAPH_LITE,
    attributeNodes = {
        @NamedAttributeNode("configurationItems")
    }
)
@NamedEntityGraph(
    name = BusinessUnitEntity.ENTITY_GRAPH_FULL,
    attributeNodes = {
        @NamedAttributeNode("parentBusinessUnit"),
        @NamedAttributeNode("configurationItems")
    }
)
public class BusinessUnitEntity {

    public static final String ENTITY_GRAPH_LITE = "BusinessUnitEntity.lite";
    public static final String ENTITY_GRAPH_FULL = "BusinessUnitEntity.full";

    @Id
    @Column(name = "business_unit_id")
    private Short businessUnitId;

    @Column(name = "business_unit_name", length = 200, nullable = false)
    private String businessUnitName;

    @Column(name = "business_unit_code", length = 4)
    private String businessUnitCode;

    @Column(name = "business_unit_type", length = 20, nullable = false)
    @Convert(converter = BusinessUnitTypeConverter.class)
    private BusinessUnitType businessUnitType;

    @Column(name = "account_number_prefix", length = 2)
    private String accountNumberPrefix;

    @Column(name = "opal_domain", length = 30)
    private String opalDomain;

    @Column(name = "welsh_language")
    private Boolean welshLanguage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_business_unit_id")
    private BusinessUnitEntity parentBusinessUnit;

    @OneToMany(mappedBy = "businessUnit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ConfigurationItemEntity> configurationItems;
}
