package uk.gov.hmcts.opal.entity.court;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.AddressCyEntity;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class CourtEntity extends AddressCyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "court_id")
    private Long courtId;

    @Column(name = "business_unit_id", insertable = false, updatable = false)
    private Short businessUnitId;

    @Column(name = "court_code", nullable = false)
    private Short courtCode;

    @Column(name = "local_justice_area_id", insertable = false, updatable = false)
    private Short localJusticeAreaId;

    @Column(name = "court_type", length = 2)
    private String courtType;

    @Column(name = "division", length = 2)
    private String division;

    @Entity
    @Getter
    @EqualsAndHashCode(callSuper = true)
    @Table(name = "courts")
    @SuperBuilder
    @NoArgsConstructor
    public static class Lite extends CourtEntity {
    }
}
