package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "business_units")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "businessUnitId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BusinessUnitEntity {

    @Id
    @Column(name = "business_unit_id")
    private Short businessUnitId;

    @Column(name = "business_unit_name", length = 200, nullable = false)
    private String businessUnitName;

    @Column(name = "business_unit_code", length = 4)
    private String businessUnitCode;

    @Column(name = "business_unit_type", length = 20, nullable = false)
    private String businessUnitType;

    @Column(name = "account_number_prefix", length = 2)
    private String accountNumberPrefix;

    @ManyToOne
    @JoinColumn(name = "parent_business_unit_id")
    private BusinessUnitEntity parentBusinessUnit;

    @Column(name = "opal_domain", length = 30)
    private String opalDomain;

    @Column(name = "welsh_language")
    private Boolean welshLanguage;

    @OneToMany(mappedBy = "businessUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConfigurationItemEntity> configurationItems;
}
