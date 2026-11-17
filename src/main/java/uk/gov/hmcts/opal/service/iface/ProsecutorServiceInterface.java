package uk.gov.hmcts.opal.service.iface;

import org.jspecify.annotations.Nullable;
import uk.gov.hmcts.opal.generated.model.ProsecutorsObject;
import uk.gov.hmcts.opal.generated.model.ProsecutorsReferenceDataResponse;

public interface ProsecutorServiceInterface {

    ProsecutorsObject getProsecutorByKey(Long prosecutorKey);

    ProsecutorsReferenceDataResponse getProsecutorsRefData(@Nullable String q);
}
