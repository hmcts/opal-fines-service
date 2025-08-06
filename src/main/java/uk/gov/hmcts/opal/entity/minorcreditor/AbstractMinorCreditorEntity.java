package uk.gov.hmcts.opal.entity.minorcreditor;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.AddressEntity;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "minorCreditorId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractMinorCreditorEntity extends AddressEntity {

    @Column(name = "business_unit_id", insertable = false, updatable = false)
    private String businessUnitId;




}
