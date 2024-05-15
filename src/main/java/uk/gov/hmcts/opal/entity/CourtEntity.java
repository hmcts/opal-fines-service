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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "courts")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "courtId")
public class CourtEntity extends EnforcerCourtBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "court_id")
    private Long courtId;

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

}
