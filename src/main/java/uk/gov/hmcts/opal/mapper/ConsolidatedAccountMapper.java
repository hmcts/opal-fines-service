package uk.gov.hmcts.opal.mapper;

import java.util.List;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.defendantaccount.ConsolidatedAccountEntity;
import uk.gov.hmcts.opal.generated.model.ConsolidatedAccountDefendantAccount;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ConsolidatedAccountMapper {

    List<ConsolidatedAccountDefendantAccount> toResponse(List<ConsolidatedAccountEntity> entities);

    @Mapping(target = "accountId", source = "childAccountId")
    @Mapping(target = "accountNumber", source = "childAccountNumber")
    @Mapping(target = "firstName", source = "childFirstName")
    @Mapping(target = "lastName", source = "childLastName")
    @Mapping(target = "dateImposed", source = "childDateImposed")
    @Mapping(target = "imposedBy", source = "childImposedBy")
    @Mapping(target = "reference", source = "childReference")
    ConsolidatedAccountDefendantAccount toResponse(ConsolidatedAccountEntity entity);
}
