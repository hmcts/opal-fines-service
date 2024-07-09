package uk.gov.hmcts.opal.entity.projection;

import uk.gov.hmcts.opal.entity.BusinessUnitRef;

public interface BusinessUnitReferenceData extends BusinessUnitRef {

    Short getBusinessUnitId();

    String getBusinessUnitName();

    String getBusinessUnitCode();

    String getBusinessUnitType();

    String getAccountNumberPrefix();

    String getOpalDomain();

    Boolean getWelshLanguage();
}
