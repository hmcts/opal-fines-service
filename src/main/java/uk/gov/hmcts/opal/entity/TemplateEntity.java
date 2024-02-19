package uk.gov.hmcts.opal.entity;

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
@Table(name = "templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "template_id_seq_generator")
    @SequenceGenerator(name = "template_id_seq_generator", sequenceName = "template_id_seq", allocationSize = 1)
    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "template_name", length = 100)
    private String templateName;

}
