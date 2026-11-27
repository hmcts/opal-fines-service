package uk.gov.hmcts.opal.service.iface;

import java.util.List;
import org.jspecify.annotations.Nullable;
import uk.gov.hmcts.opal.generated.model.LocalJusticeArea;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaType;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreasResponse;

public interface LocalJusticeAreaServiceInterface {

    LocalJusticeArea getLocalJusticeAreaById(Short localJusticeAreaId);

    LocalJusticeAreasResponse getLocalJusticeAreaRefData(
        @Nullable String q, @Nullable List<LocalJusticeAreaType> ljaTypes);
}
