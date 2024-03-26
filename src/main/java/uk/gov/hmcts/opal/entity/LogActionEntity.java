package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "log_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LogActionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "log_action_id_seq_generator")
    @SequenceGenerator(name = "log_action_id_seq_generator", sequenceName = "log_action_id_seq", allocationSize = 1)
    @Column(name = "log_action_id", nullable = false)
    private Short logActionId;

    @Column(name = "log_action_name", length = 200, nullable = false)
    private String logActionName;

}
