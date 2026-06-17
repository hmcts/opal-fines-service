package uk.gov.hmcts.opal.repository;

public interface CentralFundProjection {

    Long getCreditorAccountId();

    String getAccountNumber();

    String getName();

    Short getBusinessUnitId();

    String getBusinessUnitName();

    Boolean getWelshLanguage();

    Long getVersionNumber();
}
