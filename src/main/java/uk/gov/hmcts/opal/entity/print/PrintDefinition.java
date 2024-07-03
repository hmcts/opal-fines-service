package uk.gov.hmcts.opal.entity.print;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "print_definition")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrintDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "print_definition_id_seq")
    @SequenceGenerator(name = "print_definition_id_seq", sequenceName = "print_definition_id_seq", allocationSize = 1)
    @Column(name = "print_definition_id")
    private Long printDefinitionId;

    private String docType;

    private String docDescription;

    private String destMain;

    private String destSec1;

    private String destSec2;

    private String format;

    private String autoMode;

    private Long expiryDuration;

    private String system;

    private String templateId;

    private String addressValElement;

    private Long docDocId;

    private String xslt;

    private String linkedAreas;

    private String templateFile;
}
