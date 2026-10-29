package uk.gov.hmcts.opal.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.VersionedResponse;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.AlternatePaymentReferencesItem;
import uk.gov.hmcts.opal.mapper.AlternatePaymentReferenceMapper;
import uk.gov.hmcts.opal.repository.AlternatePaymentReferenceRepository;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.util.PermissionUtil;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.AlternatePaymentReferenceService")
public class AlternatePaymentReferenceService {

    private final DefendantAccountRepositoryService defendantAccountRepositoryService;
    private final AlternatePaymentReferenceRepository repository;
    private final AlternatePaymentReferenceMapper alternatePaymentReferenceMapper;


    DefendantAccountEntity verifyDefendantAccountEntityExistAndUserHasCorrectPermission(
        Long defendantAccountId) {
        DefendantAccountEntity defendantAccountEntity = defendantAccountRepositoryService.findById(defendantAccountId);
        PermissionUtil.checkPermissionInBusinessUnit(FinesPermission.MANAGE_ALTERNATE_PAYMENT_REFERENCES,
            defendantAccountEntity.getBusinessUnit().getBusinessUnitId());
        return defendantAccountEntity;
    }

    List<AlternatePaymentReferenceEntity> getAlternativePaymentReferenceEntities(Long defendantAccountId) {
        return repository.findByDefendantAccount_defendantAccountId(defendantAccountId);
    }

    public VersionedResponse<List<AlternatePaymentReferencesItem>> getAlternatePaymentReferences(
        Long defendantAccountId) {
        DefendantAccountEntity defendantAccountEntity =
            verifyDefendantAccountEntityExistAndUserHasCorrectPermission(defendantAccountId);
        List<AlternatePaymentReferenceEntity> alternatePaymentReferenceEntities =
            getAlternativePaymentReferenceEntities(defendantAccountId);

        List<AlternatePaymentReferencesItem> data = alternatePaymentReferenceEntities
            .stream()
            .map(alternatePaymentReferenceMapper::toAlternatePaymentReferencesItem)
            .toList();
        return new VersionedResponse<>(data, defendantAccountEntity.getVersionNumber());
    }
}