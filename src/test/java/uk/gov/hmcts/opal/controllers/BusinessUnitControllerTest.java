package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers.UserBusinessUnits;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.dto.reference.BusinessUnitReferenceData;
import uk.gov.hmcts.opal.dto.reference.BusinessUnitReferenceData.ConfigItemRefData;
import uk.gov.hmcts.opal.dto.reference.BusinessUnitReferenceDataResults;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;

@ExtendWith(MockitoExtension.class)
class BusinessUnitControllerTest {

    @Mock
    private BusinessUnitService businessUnitService;

    @Mock
    private UserStateService userStateService;

    @InjectMocks
    private BusinessUnitController businessUnitController;

    @Test
    void testGetBusinessUnit_Success() {
        // Arrange
        BusinessUnitEntity entity = BusinessUnitEntity.builder().build();

        when(businessUnitService.getBusinessUnit(any(Short.class))).thenReturn(entity);

        // Act
        ResponseEntity<BusinessUnitEntity> response = businessUnitController.getBusinessUnitById((short)1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(businessUnitService, times(1)).getBusinessUnit(any(Short.class));
    }

    @Test
    void testGetBusinessUnitsRefData_Success() {
        // Arrange
        BusinessUnitReferenceData entity = createBusinessUnitReferenceData();
        List<BusinessUnitReferenceData> businessUnitList = List.of(entity);

        when(businessUnitService.getReferenceData(any())).thenReturn(businessUnitList);

        // Act
        Optional<String> filter = Optional.empty();
        Optional<FinesPermission> permission = Optional.empty();
        ResponseEntity<BusinessUnitReferenceDataResults> response = businessUnitController
            .getBusinessUnitRefData(filter, permission);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        BusinessUnitReferenceDataResults refDataResults = response.getBody();
        assertEquals(1, refDataResults.getCount());
        assertEquals(businessUnitList, refDataResults.getRefData());
        verify(businessUnitService, times(1)).getReferenceData(any());
    }


    @Test
    void testGetBusinessUnitsRefData_Permission_Success() {
        // Arrange
        UserStateV2 userState = mock(UserStateV2.class);
        DomainBusinessUnitUsers domainBusinessUnitUsers = mock(DomainBusinessUnitUsers.class);
        BusinessUnitReferenceData entity = createBusinessUnitReferenceData();
        List<BusinessUnitReferenceData> businessUnitList = List.of(entity);

        when(businessUnitService.getReferenceData(any())).thenReturn(businessUnitList);
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
        when(userState.getDomainBusinessUnitUsers(Domain.FINES)).thenReturn(domainBusinessUnitUsers);
        when(domainBusinessUnitUsers.allBusinessUnitUsersWithPermission(any()))
            .thenReturn(new TestUserBusinessUnits(true));

        // Act
        Optional<String> filter = Optional.empty();
        Optional<FinesPermission> permission = Optional.of(FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);
        ResponseEntity<BusinessUnitReferenceDataResults> response = businessUnitController
            .getBusinessUnitRefData(filter, permission);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        BusinessUnitReferenceDataResults refDataResults = response.getBody();
        assertEquals(1, refDataResults.getCount());
        assertEquals(businessUnitList, refDataResults.getRefData());
        verify(businessUnitService, times(1)).getReferenceData(any());
    }

    @Test
    void testGetBusinessUnitsRefData_Permission_Empty() {
        // Arrange
        UserStateV2 userState = mock(UserStateV2.class);
        DomainBusinessUnitUsers domainBusinessUnitUsers = mock(DomainBusinessUnitUsers.class);
        BusinessUnitReferenceData entity = createBusinessUnitReferenceData();
        List<BusinessUnitReferenceData> businessUnitList = List.of(entity);

        when(businessUnitService.getReferenceData(any())).thenReturn(businessUnitList);
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
        when(userState.getDomainBusinessUnitUsers(Domain.FINES)).thenReturn(domainBusinessUnitUsers);
        when(domainBusinessUnitUsers.allBusinessUnitUsersWithPermission(any()))
            .thenReturn(new TestUserBusinessUnits(false));

        // Act
        Optional<String> filter = Optional.empty();
        Optional<FinesPermission> permission = Optional.of(FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);
        ResponseEntity<BusinessUnitReferenceDataResults> response = businessUnitController
            .getBusinessUnitRefData(filter, permission);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        BusinessUnitReferenceDataResults refDataResults = response.getBody();
        assertEquals(0, refDataResults.getCount());
        verify(businessUnitService, times(1)).getReferenceData(any());
    }

    private BusinessUnitReferenceData createBusinessUnitReferenceData() {
        ConfigItemRefData configItem = new ConfigItemRefData("Item Name", "Item Value", Map.of("value 1", "value 2"));

        return new BusinessUnitReferenceData(
            (short)1, "Main BU", "MNBU", "Big",
            "Prefix", "Domain", null, List.of(configItem));
    }

    private class TestUserBusinessUnits implements UserBusinessUnits {
        private final boolean contains;

        public TestUserBusinessUnits(boolean contains) {
            this.contains = contains;
        }

        @Override
        public boolean containsBusinessUnit(Short businessUnitId) {
            return contains;
        }
    }
}
