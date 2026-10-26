package uk.gov.hmcts.opal.service.opal.till;

import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS;
import static uk.gov.hmcts.opal.common.user.authorisation.model.Domain.FINES;
import static uk.gov.hmcts.opal.entity.TillStatusEnum.Created;
import static uk.gov.hmcts.opal.entity.TillStatusEnum.Failed;
import static uk.gov.hmcts.opal.entity.TillStatusEnum.Processing;
import static uk.gov.hmcts.opal.util.PermissionUtil.checkBusinessUnitUserHasPermission;
import static uk.gov.hmcts.opal.util.PermissionUtil.getRequiredBusinessUnitUser;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.generated.model.TillsAllocateItem;
import uk.gov.hmcts.opal.generated.model.TillsAllocateRequest;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.messaging.TillQueuePublisher;

@Service
@RequiredArgsConstructor
public class TillAllocationService {

    private final TillRepository tillRepository;
    private final UserStateService userStateService;
    private final TillQueuePublisher tillQueuePublisher;

    /**
     * Submits tills using separate database and broker transactions, as in interface-job processing.
     * A publication failure rolls back database changes, but a successful broker commit cannot be
     * undone if the subsequent database commit fails. This is not a distributed transaction.
     */
    @Transactional
    public void allocate(TillsAllocateRequest request) {
        List<TillsAllocateItem> requestedTills = request.getTills();
        if (requestedTills.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Tills must not contain null entries");
        }
        authorise(requestedTills);

        List<Long> tillIds = requestedTills.stream().map(TillsAllocateItem::getTillId).distinct().toList();
        Map<Long, TillEntity> tillsById = lockTills(tillIds);
        validateTills(requestedTills, tillsById);
        markProcessing(tillsById.values());
        tillQueuePublisher.publish(tillIds);
    }

    /**
     * Uses the shared permission utilities to authorise each distinct requested business unit before loading tills.
     */
    private void authorise(List<TillsAllocateItem> requestedTills) {
        DomainBusinessUnitUsers businessUnitUsers = userStateService.getUserStateFromSecurityContext()
            .getDomainBusinessUnitUsers(FINES);
        requestedTills.stream().map(TillsAllocateItem::getBusinessUnitId).distinct()
            .forEach(id -> checkBusinessUnitUserHasPermission(
                getRequiredBusinessUnitUser(businessUnitUsers, id), PROCESS_AND_ALLOCATE_PAYMENTS));
    }

    /**
     * Acquires write locks in till ID order, held until the enclosing allocation transaction completes.
     */
    private Map<Long, TillEntity> lockTills(List<Long> tillIds) {
        return tillRepository.findAllByTillIdInOrderByTillId(tillIds).stream()
            .collect(Collectors.toMap(TillEntity::getTillId, Function.identity()));
    }

    /**
     * Checks existence, business-unit ownership and eligibility for every item before changing any status.
     */
    private static void validateTills(List<TillsAllocateItem> requestedTills, Map<Long, TillEntity> tillsById) {
        for (TillsAllocateItem requestedTill : requestedTills) {
            TillEntity till = tillsById.get(requestedTill.getTillId());
            if (till == null) {
                throw new EntityNotFoundException("Till not found with id: " + requestedTill.getTillId());
            }
            checkBusinessUnit(requestedTill, till);
            checkStatus(till);
        }
    }

    /**
     * Updates the managed tills and flushes writes before publication, without committing the database transaction.
     */
    private void markProcessing(Collection<TillEntity> tills) {
        tills.forEach(till -> till.setStatus(Processing));
        tillRepository.flush();
    }

    /**
     * Prevents a caller from selecting a till under a different business unit from the one authorised.
     */
    private static void checkBusinessUnit(TillsAllocateItem requestedTill, TillEntity till) {
        if (!Objects.equals(till.getBusinessUnit().getBusinessUnitId(), requestedTill.getBusinessUnitId())) {
            throw new IllegalArgumentException("Business unit does not match till " + till.getTillId());
        }
    }

    /**
     * Accepts newly created tills and retries of failed allocations, rejecting processing or allocated tills.
     */
    private static void checkStatus(TillEntity till) {
        if (till.getStatus() != Created && till.getStatus() != Failed) {
            throw new ResourceConflictException("Till", till.getTillId(), "Till must be Created or Failed", null);
        }
    }
}
