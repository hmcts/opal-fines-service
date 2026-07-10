package uk.gov.hmcts.opal.dto.print;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.print.PrintStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrintJobDto {

    private Long printJobId;

    private UUID batchId;

    private UUID jobId;

    private String xmlData;

    private String docType;

    private String docVersion;

    private PrintStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
