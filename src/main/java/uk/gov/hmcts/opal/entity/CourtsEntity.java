package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "courts")
public class CourtsEntity extends EnforcersCourtsBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "court_id")
    private Long courtId;

    @Column(name = "court_code", nullable = false)
    private Short courtCode;

    @Column(name = "parent_court_id")
    private Long parentCourtId;

    @Column(name = "local_justice_area_id", nullable = false)
    private Short localJusticeAreaId;

    @Column(name = "national_court_code", length = 7)
    private String nationalCourtCode;

}
