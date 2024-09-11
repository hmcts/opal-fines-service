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
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "courts")
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "courtId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CourtEntity extends AddressCyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "court_id")
    private Long courtId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", updatable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "court_code", nullable = false)
    private Short courtCode;

    @ManyToOne
    @JoinColumn(name = "parent_court_id")
    private CourtEntity parentCourt;

    @ManyToOne
    @JoinColumn(name = "local_justice_area_id", nullable = false)
    private LocalJusticeAreaEntity localJusticeArea;

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

}
