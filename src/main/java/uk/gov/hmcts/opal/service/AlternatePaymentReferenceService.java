package uk.gov.hmcts.opal.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.common.exceptions.bespoke.BusinessRuleViolationException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteType;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.Category;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.Relationship;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.AlternatePaymentReferencesItem;
import uk.gov.hmcts.opal.generated.model.AlternatePaymentReferencesWriteRequest;
import uk.gov.hmcts.opal.mapper.AlternatePaymentReferenceMapper;
import uk.gov.hmcts.opal.repository.AlternatePaymentReferenceRepository;
import uk.gov.hmcts.opal.service.opal.OpalNotesService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.util.PermissionUtil;
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.AlternatePaymentReferenceService")
public class AlternatePaymentReferenceService {

    private final DefendantAccountRepositoryService defendantAccountRepositoryService;
    private final OpalNotesService notesService;
    private final AlternatePaymentReferenceRepository repository;
    private final AlternatePaymentReferenceMapper alternatePaymentReferenceMapper;
    private final Clock clock;


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

    public List<AlternatePaymentReferencesItem> getAlternatePaymentReferences(Long defendantAccountId) {
        verifyDefendantAccountEntityExistAndUserHasCorrectPermission(defendantAccountId);
        List<AlternatePaymentReferenceEntity> alternatePaymentReferenceEntities =
            getAlternativePaymentReferenceEntities(defendantAccountId);

        return alternatePaymentReferenceEntities
            .stream()
            .map(alternatePaymentReferenceMapper::toAlternatePaymentReferencesItem)
            .toList();
    }

    void verifyUniqueApr(BusinessUnitEntity businessUnit, String aprText) {
        if (repository.existsByAprTextAndBusinessUnit_businessUnitId(
            aprText,
            businessUnit.getBusinessUnitId()
        )) {
            throw new BusinessRuleViolationException(
                "Alternate Payment Reference text already exists in business unit",
                "Alternate Payment Reference text: " + aprText
                    + " already exists in business unit: " + businessUnit.getBusinessUnitId());
        }
    }

    @Transactional
    public AlternatePaymentReferencesItem createAlternatePaymentReferences(Long defendantAccountId,
        AlternatePaymentReferencesWriteRequest alternatePaymentReferencesWriteRequest, String expectedVersion) {

        DefendantAccountEntity defendantAccountEntity =
            verifyDefendantAccountEntityExistAndUserHasCorrectPermission(defendantAccountId);

        VersionUtils.verifyIfMatch(defendantAccountEntity, expectedVersion, defendantAccountId,
            "createAlternatePaymentReferences");

        if (repository.existsByDefendantAccount_defendantAccountIdAndRelationship(
            defendantAccountId, Relationship.APR)) {
            throw new BusinessRuleViolationException(
                "Alternate Payment Reference already exists for defendant account",
                "Alternate Payment Reference with relationship APR already exists for defendant account: "
                    + defendantAccountId);
        }

        String aprText = alternatePaymentReferencesWriteRequest.getAprText();

        verifyUniqueApr(defendantAccountEntity.getBusinessUnit(), aprText);

        AlternatePaymentReferenceEntity alternatePaymentReferenceEntity =
            AlternatePaymentReferenceEntity.builder()
                .defendantAccount(defendantAccountEntity)
                .relationship(Relationship.APR)
                .category(Category.APR)
                .businessUnit(defendantAccountEntity.getBusinessUnit())
                .aprText(aprText)
                .createdDatetime(LocalDateTime.now(clock))
                .updatedDatetime(LocalDateTime.now(clock))
                .build();
        alternatePaymentReferenceEntity = repository.save(alternatePaymentReferenceEntity);

        notesService.addNote(
            NoteType.AA,
            AssociatedRecordType.DEFENDANT_ACCOUNTS,
            defendantAccountId,
            defendantAccountEntity.getVersion(),
            "Alternate payment reference added | " + alternatePaymentReferenceEntity.getAprText(),
            defendantAccountEntity.getBusinessUnit().getBusinessUnitId()
        );
        return alternatePaymentReferenceMapper.toAlternatePaymentReferencesItem(alternatePaymentReferenceEntity);
    }
}
