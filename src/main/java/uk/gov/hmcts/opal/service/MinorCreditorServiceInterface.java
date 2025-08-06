package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;

import java.util.List;

public interface MinorCreditorServiceInterface {

    MinorCreditorEntity searchMinorCreditors(MinorCreditorEntity minorCreditorSearchDto);

}
