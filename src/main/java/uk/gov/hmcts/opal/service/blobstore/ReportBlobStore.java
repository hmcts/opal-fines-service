package uk.gov.hmcts.opal.service.blobstore;

import java.io.InputStream;
import java.util.UUID;

public interface ReportBlobStore {

    UUID storeReport(InputStream jsonReport);

    byte[] getReport(UUID reportLocation);

    void deleteReport(UUID reportLocation);

}
