package uk.gov.hmcts.opal.entity.court;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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

    @Column(name = "court_code", nullable = false)
    private Short courtCode;

    @Column(name = "national_court_code", length = 7)
    private String nationalCourtCode;

    @Column(name = "gob_enforcing_court_code")
    private Short gobEnforcingCourtCode;

    @Column(name = "lja")
    private Short lja;

    @Column(name = "court_type", length = 2)
    private String courtType;

    @Column(name = "division", length = 2)
    private String division;

    @Column(name = "session", length = 2)
    private String session;

    @Column(name = "start_time", length = 8)
    private String startTime;

    @Column(name = "max_load")
    private Long maxLoad;

    @Column(name = "record_session_times", length = 1)
    private String recordSessionTimes;

    @Column(name = "max_court_duration")
    private Long maxCourtDuration;

    @Column(name = "group_code", length = 24)
    private String groupCode;

    @Column(name = "business_unit_id", insertable = false, updatable = false)
    private Short businessUnitId;

    @Column(name = "parent_court_id", insertable = false, updatable = false)
    private Long parentCourtId;

    @Column(name = "local_justice_area_id", insertable = false, updatable = false)
    private Short localJusticeAreaId;

    @Data
    @Entity
    @EqualsAndHashCode(callSuper = true)
    @Table(name = "courts")
    @SuperBuilder
    @ToString(callSuper = true)
    @AllArgsConstructor
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "courtId")
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "Court")
    public static class Lite extends CourtEntity {
    }
}
