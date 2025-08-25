package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.authorisation.model.UserState.UserBusinessUnits;
import uk.gov.hmcts.opal.dto.reference.BusinessUnitReferenceDataResults;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.dto.reference.BusinessUnitReferenceData;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;
import uk.gov.hmcts.opal.service.UserStateService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void testSearchBusinessUnits_Success() {
        // Arrange
        BusinessUnitEntity entity = BusinessUnitEntity.builder().build();
        List<BusinessUnitEntity> businessUnitList = List.of(entity);

        when(businessUnitService.searchBusinessUnits(any())).thenReturn(businessUnitList);

        // Act
        BusinessUnitSearchDto searchDto = BusinessUnitSearchDto.builder().build();
        ResponseEntity<List<BusinessUnitEntity>> response = businessUnitController.postBusinessUnitsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(businessUnitList, response.getBody());
        verify(businessUnitService, times(1)).searchBusinessUnits(any());
    }

    @Test
    void testGetBusinessUnitsRefData_Success() {
        // Arrange
        BusinessUnitReferenceData entity = createBusinessUnitReferenceData();
        List<BusinessUnitReferenceData> businessUnitList = List.of(entity);

        when(businessUnitService.getReferenceData(any())).thenReturn(businessUnitList);

        // Act
        Optional<String> filter = Optional.empty();
        Optional<Permissions> permission = Optional.empty();
        String headerToken = "Bearer token";
        ResponseEntity<BusinessUnitReferenceDataResults> response = businessUnitController
            .getBusinessUnitRefData(filter, permission, headerToken);

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
        UserState userState = Mockito.mock(UserState.class);
        BusinessUnitReferenceData entity = createBusinessUnitReferenceData();
        List<BusinessUnitReferenceData> businessUnitList = List.of(entity);

        when(businessUnitService.getReferenceData(any())).thenReturn(businessUnitList);
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(userState);
        when(userState.allBusinessUnitUsersWithPermission(any())).thenReturn(new TestUserBusinessUnits(true));

        // Act
        Optional<String> filter = Optional.empty();
        Optional<Permissions> permission = Optional.of(Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);
        String headerToken = "Bearer token";
        ResponseEntity<BusinessUnitReferenceDataResults> response = businessUnitController
            .getBusinessUnitRefData(filter, permission, headerToken);

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
        UserState userState = Mockito.mock(UserState.class);
        BusinessUnitReferenceData entity = createBusinessUnitReferenceData();
        List<BusinessUnitReferenceData> businessUnitList = List.of(entity);

        when(businessUnitService.getReferenceData(any())).thenReturn(businessUnitList);
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(userState);
        when(userState.allBusinessUnitUsersWithPermission(any())).thenReturn(new TestUserBusinessUnits(false));

        // Act
        Optional<String> filter = Optional.empty();
        Optional<Permissions> permission = Optional.of(Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);
        String headerToken = "Bearer token";
        ResponseEntity<BusinessUnitReferenceDataResults> response = businessUnitController
            .getBusinessUnitRefData(filter, permission, headerToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        BusinessUnitReferenceDataResults refDataResults = response.getBody();
        assertEquals(0, refDataResults.getCount());
        verify(businessUnitService, times(1)).getReferenceData(any());
    }

    private BusinessUnitReferenceData createBusinessUnitReferenceData() {
        return new BusinessUnitReferenceData(
            (short)1, "Main BU", "MNBU", "Big",
            "Prefix", "Domain", null, List.of(
                new BusinessUnitReferenceData.ConfigItemRefData("Item Name", "Item Value",
                                                                List.of("value 1", "value 2"))
        ));

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
