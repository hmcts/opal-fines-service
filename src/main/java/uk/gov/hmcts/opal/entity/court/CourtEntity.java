package uk.gov.hmcts.opal.entity.court;

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
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.AddressCyEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"businessUnit", "localJusticeArea", "parentCourt"})
@ToString(callSuper = true, exclude = {"businessUnit", "localJusticeArea", "parentCourt"})
@Table(name = "courts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "courtId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedEntityGraph(name = CourtEntity.ENTITY_GRAPH_LITE)
@NamedEntityGraph(
    name = CourtEntity.ENTITY_GRAPH_FULL,
    attributeNodes = {
        @NamedAttributeNode("businessUnit"),
        @NamedAttributeNode("localJusticeArea"),
        @NamedAttributeNode("parentCourt")
    }
)
public class CourtEntity extends AddressCyEntity {

    public static final String ENTITY_GRAPH_LITE = "CourtEntity.lite";
    public static final String ENTITY_GRAPH_FULL = "CourtEntity.full";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "court_id")
    private Long courtId;

    @Column(name = "business_unit_id", insertable = false, updatable = false)
    private Short businessUnitId;

    @Column(name = "court_code", nullable = false)
    private Short courtCode;

    @Column(name = "court_name", length = 100)
    private String name;

    @Column(name = "local_justice_area_id", insertable = false, updatable = false)
    private Short localJusticeAreaId;

    @Column(name = "parent_court_id", insertable = false, updatable = false)
    private Long parentCourtId;

    @Column(name = "national_court_code")
    private Short nationalCourtCode;

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

    @Column(name = "start_time", length = 10)
    private String startTime;

    @Column(name = "max_load")
    private Integer maxLoad;

    @Column(name = "record_session_times", length = 1)
    private String recordSessionTimes;

    @Column(name = "max_court_duration")
    private Integer maxCourtDuration;

    @Column(name = "group_code", length = 20)
    private String groupCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_unit_id", insertable = false, updatable = false)
    private BusinessUnitEntity businessUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_justice_area_id", insertable = false, updatable = false)
    private LocalJusticeAreaEntity localJusticeArea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_court_id", insertable = false, updatable = false)
    private CourtEntity parentCourt;
}
