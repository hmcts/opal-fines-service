package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.CourtFeesResponse;
import uk.gov.hmcts.opal.mapper.CourtFeeMapper;
import uk.gov.hmcts.opal.repository.CourtFeeRepository;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;

@ExtendWith(MockitoExtension.class)
class CourtFeesServiceTest {

    @Mock
    private UserStateService userStateService;

    @Mock
    private BusinessUnitService businessUnitService;

    @Mock
    private CourtFeeRepository courtFeeRepository;

    @Mock
    private CourtFeeMapper courtFeeMapper;

    @Mock
    private UserStateV2 userState;

    @Mock
    private DomainBusinessUnitUsers domainBusinessUnitUsers;

    @Mock
    private BusinessUnitUser businessUnitUser;

    @InjectMocks
    private CourtFeesService courtFeesService;

    @Test
    void getCourtFeesForBusinessUnit_whenUserIsAuthorised_returnsMappedResponse() {
        Short businessUnitId = 77;
        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder().build();
        List<CourtFeeEntity> courtFees = List.of(CourtFeeEntity.builder().build());
        CourtFeesResponse expectedResponse = CourtFeesResponse.builder().build();

        when(businessUnitService.getBusinessUnit(businessUnitId)).thenReturn(businessUnit);
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
        when(userState.getDomainBusinessUnitUsers(Domain.FINES)).thenReturn(domainBusinessUnitUsers);
        when(domainBusinessUnitUsers.getBusinessUnitUserForBusinessUnit(businessUnitId))
            .thenReturn(Optional.of(businessUnitUser));
        when(courtFeeRepository.findAllByBusinessUnit(businessUnit)).thenReturn(courtFees);
        when(courtFeeMapper.toCourtFeesResponse(courtFees)).thenReturn(expectedResponse);

        CourtFeesResponse response = courtFeesService.getCourtFeesForBusinessUnit(businessUnitId);

        assertSame(expectedResponse, response);
        verify(domainBusinessUnitUsers).getBusinessUnitUserForBusinessUnit(businessUnitId);
        verify(courtFeeMapper).toCourtFeesResponse(courtFees);
    }

    @Test
    void getCourtFeesForBusinessUnit_whenUserIsNotAuthorised_throwsPermissionNotAllowedException() {
        Short businessUnitId = 77;
        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder().build();

        when(businessUnitService.getBusinessUnit(businessUnitId)).thenReturn(businessUnit);
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
        when(userState.getDomainBusinessUnitUsers(Domain.FINES)).thenReturn(domainBusinessUnitUsers);
        when(domainBusinessUnitUsers.getBusinessUnitUserForBusinessUnit(businessUnitId))
            .thenReturn(Optional.empty());

        assertThrows(PermissionNotAllowedException.class,
            () -> courtFeesService.getCourtFeesForBusinessUnit(businessUnitId));
        verify(domainBusinessUnitUsers).getBusinessUnitUserForBusinessUnit(businessUnitId);
    }
}
