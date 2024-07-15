package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.WarrantRegisterSearchDto;
import uk.gov.hmcts.opal.entity.WarrantRegisterEntity;

import java.util.List;

public interface WarrantRegisterServiceInterface {

    WarrantRegisterEntity getWarrantRegister(long warrantRegisterId);

    List<WarrantRegisterEntity> searchWarrantRegisters(WarrantRegisterSearchDto criteria);
}
