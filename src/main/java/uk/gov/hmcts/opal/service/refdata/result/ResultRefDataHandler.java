package uk.gov.hmcts.opal.service.refdata.result;

import static uk.gov.hmcts.opal.service.refdata.common.ValidationUtils.invalid;
import static uk.gov.hmcts.opal.service.refdata.common.ValidationUtils.validateLength;
import static uk.gov.hmcts.opal.service.refdata.common.ValidationUtils.validateRequired;
import static uk.gov.hmcts.opal.service.refdata.common.ValidationUtils.validateValue;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.result.ResultType;
import uk.gov.hmcts.opal.refdata.RefDataUpdateHandler;
import uk.gov.hmcts.opal.repository.ResultRepository;

@Component
@RequiredArgsConstructor
public class ResultRefDataHandler implements RefDataUpdateHandler<ResultsRecord, ResultEntity> {
    private final ResultRepository repository;
    private final ResultRefDataMapper mapper;

    @Override
    public String refDataType() {
        return "Result";
    }

    @Override
    public Class<ResultsRecord> payloadType() {
        return ResultsRecord.class;
    }

    @Override
    public void validateDto(ResultsRecord dto) {
        validateRequired(dto.getResultCode(), "result_code");
        validateRequired(dto.getResultTitle(), "result_title");
        validateRequired(dto.getResultType(), "result_type");
        validateRequired(dto.getPublishingStatus(), "publishing_status");
        validateRequired(dto.getImposition(), "imposition");
        validateRequired(dto.getImpositionAccruing(), "imposition_accruing");
        validateRequired(dto.getEnforcement(), "enforcement");
        validateRequired(dto.getEnforcementOverride(), "enforcement_override");
        validateRequired(dto.getFurtherEnforcementWarningRequired(), "further_enforcement_warning_required");
        validateRequired(dto.getFurtherEnforcementDisallowed(), "further_enforcement_disallowed");
        validateRequired(dto.getEnforcementOnHold(), "enforcement_on_hold");
        validateRequired(dto.getRequiresEnforcer(), "requires_enforcer");
        validateRequired(dto.getGeneratesAHearing(), "generates_a_hearing");
        validateRequired(dto.getGeneratesAWarrant(), "generates_a_warrant");
        validateRequired(dto.getCollectionOrder(), "collection_order");
        validateRequired(dto.getDisallowTtpExtension(), "disallow_ttp_extension");
        validateRequired(dto.getPreserveLastEnforcementOnTtpExtension(), "preserve_last_enforcement_on_ttp_extension");
        validateRequired(dto.getPreventPaymentCard(), "prevent_payment_card");
        validateRequired(dto.getListsMonies(), "lists_monies");
        validateLength(dto.getResultCode(), "result_code", 6);
        validateLength(dto.getResultTitle(), "result_title", 60);
        validateLength(dto.getResultTitleWelsh(), "result_title_welsh", 60);
        validateValue(dto.getResultType(), "result_type", ResultType::valueOf);
        validateValue(dto.getImpositionCategory(), "imposition_category", mapper::category);
        validateValue(dto.getImpositionCreditor(), "imposition_creditor", mapper::creditor);
        BigInteger priority = dto.getImpositionAllocationPriority();
        if (priority != null && (priority.compareTo(BigInteger.valueOf(Short.MIN_VALUE)) < 0
            || priority.compareTo(BigInteger.valueOf(Short.MAX_VALUE)) > 0)) {
            throw invalid("imposition_allocation_priority", "exceeds database range");
        }
        if (mapper.actions(dto).length() > 100) {
            throw invalid("result_next_permitted_action_mappings", "exceed database length");
        }
    }

    @Override
    public Optional<ResultEntity> findEntity(ResultsRecord dto) {
        return repository.findById(getBusinessKey(dto));
    }

    @Override
    public ResultEntity createEntityFromDto(ResultsRecord dto) {
        ResultEntity entity = new ResultEntity();
        entity.setResultId(getBusinessKey(dto));
        updateEntityFromDto(dto, entity);
        return entity;
    }

    @Override
    public ResultEntity saveEntity(ResultEntity entity) {
        return repository.save(entity);
    }

    @Override
    public void deleteEntity(String businessKey) {
        repository.deleteById(businessKey);
    }

    @Override
    public String getBusinessKey(ResultsRecord dto) {
        return dto.getResultCode();
    }

    @Override
    public List<String> cachesToClear() {
        return List.of("resultsCache", "resultReferenceDataCache");
    }

    @Override
    public void updateEntityFromDto(ResultsRecord dto, ResultEntity entity) {
        mapper.updateEntityFromDto(dto, entity);
    }
}
