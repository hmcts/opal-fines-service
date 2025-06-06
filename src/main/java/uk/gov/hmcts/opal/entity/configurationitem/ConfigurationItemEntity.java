package uk.gov.hmcts.opal.entity.configurationitem;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

@Entity
@Table(name = "configuration_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "configurationItemId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigurationItemEntity extends AbstractConfigurationItemEntity {


    @ManyToOne
    @JoinColumn(name = "business_unit_id")
    private BusinessUnitEntity businessUnit;


}
