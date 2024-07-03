package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.ChequeSearchDto;
import uk.gov.hmcts.opal.entity.ChequeEntity;

import java.util.List;

public interface ChequeServiceInterface {

    ChequeEntity getCheque(long chequeId);

    List<ChequeEntity> searchCheques(ChequeSearchDto criteria);
}
