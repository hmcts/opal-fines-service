package uk.gov.hmcts.opal.service.opal.till;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS;
import static uk.gov.hmcts.opal.common.user.authorisation.model.Domain.FINES;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissions;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionsFor;
import static uk.gov.hmcts.opal.entity.TillStatusEnum.Created;
import static uk.gov.hmcts.opal.entity.TillStatusEnum.Processing;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.TillStatusEnum;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.generated.model.TillsAllocateItem;
import uk.gov.hmcts.opal.generated.model.TillsAllocateRequest;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.messaging.TillQueuePublisher;

@ExtendWith(MockitoExtension.class)
class TillAllocationServiceTest {

    private static final short BU = 78;

    @Mock
    private TillRepository repository;
    @Mock
    private UserStateService userStateService;
    @Mock
    private TillQueuePublisher publisher;
    @InjectMocks
    private TillAllocationService service;

    @Test
    void rejectsNullItem() {
        TillsAllocateRequest request = TillsAllocateRequest.builder().tills(Collections.singletonList(null)).build();

        assertThatThrownBy(() -> service.allocate(request)).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Tills must not contain null entries");

        verifyNoInteractions(userStateService, repository, publisher);
    }

    @Test
    void rejectsBusinessUnitWithoutPermission() {
        userState(permissions(BU, permissionsFor()));

        assertThatThrownBy(() -> service.allocate(request(item(1L, BU))))
            .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(repository, publisher);
    }

    @Test
    void rejectsWrongBusinessUnit() {
        permit();
        TillEntity till = till(1L, Created);
        till.setBusinessUnit(BusinessUnitEntity.builder().businessUnitId((short) 77).build());
        when(repository.findAllByTillIdInOrderByTillId(List.of(1L))).thenReturn(List.of(till));

        assertThatThrownBy(() -> service.allocate(request(item(1L, BU))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Business unit does not match");

        assertUnchanged(till);
    }

    @Test
    void missingTillPreventsAnyUpdates() {
        permit();
        TillEntity till = till(1L, Created);
        when(repository.findAllByTillIdInOrderByTillId(List.of(1L, 2L))).thenReturn(List.of(till));

        assertThatThrownBy(() -> service.allocate(request(item(1L, BU), item(2L, BU))))
            .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("2");

        assertUnchanged(till);
    }

    @ParameterizedTest
    @EnumSource(value = TillStatusEnum.class, names = {"Processing", "Allocated"})
    void ineligibleTillPreventsAnyUpdates(TillStatusEnum status) {
        permit();
        TillEntity first = till(1L, Created);
        TillEntity second = till(2L, status);
        when(repository.findAllByTillIdInOrderByTillId(List.of(1L, 2L))).thenReturn(List.of(first, second));

        assertThatThrownBy(() -> service.allocate(request(item(1L, BU), item(2L, BU))))
            .isInstanceOf(ResourceConflictException.class);

        assertUnchanged(first);
        assertThat(second.getStatus()).isEqualTo(status);
    }

    @Test
    void publishesDuplicateOnlyOnceAfterFlushing() {
        permit();
        TillEntity till = till(1L, Created);
        when(repository.findAllByTillIdInOrderByTillId(List.of(1L))).thenReturn(List.of(till));

        service.allocate(request(item(1L, BU), item(1L, BU)));

        assertThat(till.getStatus()).isEqualTo(Processing);
        InOrder order = inOrder(repository, publisher);
        order.verify(repository).flush();
        order.verify(publisher).publish(List.of(1L));
        verify(publisher).publish(List.of(1L));
    }

    @Test
    void validatesBusinessUnitOnDuplicate() {
        userState(permissions(BU, permissionsFor(PROCESS_AND_ALLOCATE_PAYMENTS)),
            permissions((short) 77, permissionsFor(PROCESS_AND_ALLOCATE_PAYMENTS)));
        TillEntity till = till(1L, Created);
        when(repository.findAllByTillIdInOrderByTillId(List.of(1L))).thenReturn(List.of(till));

        assertThatThrownBy(() -> service.allocate(request(item(1L, BU), item(1L, (short) 77))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Business unit does not match");

        assertUnchanged(till);
    }

    @Test
    void propagatesUnexpectedPublisherFailure() {
        permit();
        when(repository.findAllByTillIdInOrderByTillId(List.of(1L))).thenReturn(List.of(till(1L, Created)));
        IllegalStateException failure = new IllegalStateException("Unexpected failure");
        doThrow(failure).when(publisher).publish(List.of(1L));

        assertThatThrownBy(() -> service.allocate(request(item(1L, BU)))).isSameAs(failure);
    }

    private void permit() {
        userState(permissions(BU, permissionsFor(PROCESS_AND_ALLOCATE_PAYMENTS)));
    }

    private void userState(BusinessUnitUser... users) {
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(UserStateV2.builder()
            .domains(Map.of(FINES, DomainBusinessUnitUsers.builder().businessUnitUsers(List.of(users)).build()))
            .build());
    }

    private void assertUnchanged(TillEntity till) {
        assertThat(till.getStatus()).isEqualTo(Created);
        verify(repository, never()).flush();
        verifyNoInteractions(publisher);
    }

    private static TillEntity till(long id, TillStatusEnum status) {
        return TillEntity.builder().tillId(id).status(status)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId(BU).build()).build();
    }

    private static TillsAllocateItem item(long id, short businessUnitId) {
        return TillsAllocateItem.builder().tillId(id).businessUnitId(businessUnitId).build();
    }

    private static TillsAllocateRequest request(TillsAllocateItem... items) {
        return TillsAllocateRequest.builder().tills(List.of(items)).build();
    }
}
