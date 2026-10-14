package uk.gov.hmcts.opal.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.report.SupportedFileType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportInstanceFileEntityId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long reportInstanceId;

    private SupportedFileType fileType;
}
