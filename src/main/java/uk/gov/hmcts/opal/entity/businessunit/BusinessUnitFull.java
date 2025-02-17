package uk.gov.hmcts.opal.entity.businessunit;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.ConfigurationItemLite;

import java.util.List;

@Entity
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "business_units")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "businessUnitId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BusinessUnitFull extends BusinessUnit {

    @ManyToOne
    @JoinColumn(name = "parent_business_unit_id", insertable = false, updatable = false)
    private BusinessUnitFull parentBusinessUnit;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConfigurationItemLite> configurationItems;
}
