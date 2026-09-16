package uk.gov.hmcts.opal.disco.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.dto.reference.BusinessUnitReferenceData;
import uk.gov.hmcts.opal.dto.reference.BusinessUnitReferenceData.ConfigItemRefData;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitType;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;
import uk.gov.hmcts.opal.repository.BusinessUnitLiteRepository;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;

@ExtendWith(MockitoExtension.class)
class BusinessUnitServiceTest {

    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @Mock
    private BusinessUnitLiteRepository businessUnitLiteRepository;

    @InjectMocks
    private BusinessUnitService businessUnitService;

    @Test
    void testGetBusinessUnit() {
        // Arrange
        BusinessUnitEntity businessUnitEntity = BusinessUnitEntity.builder().build();
        when(businessUnitRepository.findById(any())).thenReturn(Optional.of(businessUnitEntity));

        // Act
        BusinessUnitEntity result = businessUnitService.getBusinessUnit((short)1);

        // Assert
        assertNotNull(result);
    }

    @Test
    void getBusinessUnitUserIdForBusinessUnit_returnsMatchingUserId() {
        // Arrange
        DomainBusinessUnitUsers businessUnitUsers = businessUnitUsers(
            businessUnitUser((short) 78, "L078JG"),
            businessUnitUser((short) 77, "L077JG")
        );

        // Act
        String result = businessUnitService.getBusinessUnitUserIdForBusinessUnit(
            businessUnitUsers, (short) 78, FinesPermission.AMEND_PAYMENT_TERMS);

        // Assert
        assertEquals("L078JG", result);
    }

    @Test
    void getBusinessUnitUserIdForBusinessUnit_whenBusinessUnitMissing_throwsPermissionNotAllowed() {
        // Arrange
        DomainBusinessUnitUsers businessUnitUsers = businessUnitUsers(
            businessUnitUser((short) 77, "L077JG")
        );

        // Act
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> businessUnitService.getBusinessUnitUserIdForBusinessUnit(
                businessUnitUsers, (short) 78, FinesPermission.AMEND_PAYMENT_TERMS)
        );

        // Assert
        assertEquals((short) 78, ex.getBusinessUnitId());
        assertThat(ex.getPermission()).containsExactly(FinesPermission.AMEND_PAYMENT_TERMS);
    }

    @Test
    void getBusinessUnitUserIdForBusinessUnit_whenBusinessUnitUserIdBlank_throwsPermissionNotAllowed() {
        DomainBusinessUnitUsers businessUnitUsers = businessUnitUsers(
            businessUnitUser((short) 78, " ")
        );

        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> businessUnitService.getBusinessUnitUserIdForBusinessUnit(
                businessUnitUsers, (short) 78, FinesPermission.AMEND_PAYMENT_TERMS)
        );

        assertEquals((short) 78, ex.getBusinessUnitId());
        assertThat(ex.getPermission()).containsExactly(FinesPermission.AMEND_PAYMENT_TERMS);
    }

    @Test
    void hasBusinessUnitUserWithPermission_whenUserHasPermission_returnsTrue() {
        // Arrange
        DomainBusinessUnitUsers businessUnitUsers = businessUnitUsers(
            businessUnitUser((short) 78, "L078JG", FinesPermission.AMEND_PAYMENT_TERMS)
        );

        // Act
        boolean result = businessUnitService.hasBusinessUnitUserWithPermission(
            businessUnitUsers, (short) 78, FinesPermission.AMEND_PAYMENT_TERMS);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void hasBusinessUnitUserWithPermission_whenBusinessUnitUsersIsNull_returnsFalse() {
        // Act
        boolean result = businessUnitService.hasBusinessUnitUserWithPermission(
            null, (short) 78, FinesPermission.AMEND_PAYMENT_TERMS);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void hasBusinessUnitUserWithPermission_whenBusinessUnitUserListIsNull_returnsFalse() {
        // Arrange
        DomainBusinessUnitUsers businessUnitUsers = DomainBusinessUnitUsers.builder().build();

        // Act
        boolean result = businessUnitService.hasBusinessUnitUserWithPermission(
            businessUnitUsers, (short) 78, FinesPermission.AMEND_PAYMENT_TERMS);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void hasBusinessUnitUserWithPermission_whenPermissionMissing_returnsFalse() {
        // Arrange
        DomainBusinessUnitUsers businessUnitUsers = businessUnitUsers(
            businessUnitUser((short) 78, "L078JG")
        );

        // Act
        boolean result = businessUnitService.hasBusinessUnitUserWithPermission(
            businessUnitUsers, (short) 78, FinesPermission.AMEND_PAYMENT_TERMS);

        // Assert
        assertThat(result).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchBusinessUnits() {
        // Arrange
        SpecificationFluentQuery sfq = mock(SpecificationFluentQuery.class);

        BusinessUnitEntity businessUnitEntity = BusinessUnitEntity.builder().build();
        Page<BusinessUnitEntity> mockPage = new PageImpl<>(List.of(businessUnitEntity), Pageable.unpaged(), 999L);
        when(businessUnitRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<BusinessUnitEntity> result = businessUnitService
            .searchBusinessUnits(BusinessUnitSearchDto.builder().build());

        // Assert
        assertEquals(List.of(businessUnitEntity), result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testBusinessUnitsReferenceData() {
        // Arrange
        SpecificationFluentQuery sfq = mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        BusinessUnitEntity businessUnitEntityLite = BusinessUnitEntity.builder()
            .businessUnitId((short)3)
            .businessUnitName("Big Business Unit")
            .businessUnitType(BusinessUnitType.AREA)
            .welshLanguage(true)
            .configurationItems(List.of(
                ConfigurationItemEntity.builder()
                    .itemName("A Config Item")
                    .itemValue("A value")
                    .itemValues(configItemValues())
                    .build()))
            .build();

        Page<BusinessUnitEntity> mockPage = new PageImpl<>(List.of(businessUnitEntityLite), Pageable.unpaged(), 999L);

        // Mock the lite repository instead of the full repository
        when(businessUnitLiteRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<BusinessUnitReferenceData> result = businessUnitService.getReferenceData(Optional.empty());

        // Assert
        Map<String, String> expectedItemValues = Map.of("Key1", "Item Values One", "Key2", "Item Values Two");
        ConfigItemRefData expectedConfigItem = new ConfigItemRefData("A Config Item", "A value", expectedItemValues);
        assertEquals(List.of(new BusinessUnitReferenceData(
            (short)3, "Big Business Unit", null,
            BusinessUnitType.AREA.getLabel(), null,
            null, Boolean.TRUE, List.of(expectedConfigItem))), result);
    }

    private static JsonNode configItemValues() {
        return JsonNodeFactory.instance.objectNode()
            .put("Key1", "Item Values One")
            .put("Key2", "Item Values Two");
    }

    private static DomainBusinessUnitUsers businessUnitUsers(BusinessUnitUser... businessUnitUsers) {
        return DomainBusinessUnitUsers.builder()
            .businessUnitUsers(List.of(businessUnitUsers))
            .build();
    }

    private static BusinessUnitUser businessUnitUser(
        short businessUnitId, String businessUnitUserId, FinesPermission... permissions) {

        return BusinessUnitUser.builder()
            .businessUnitId(businessUnitId)
            .businessUnitUserId(businessUnitUserId)
            .permissions(permissionsFor(permissions))
            .build();
    }

    private static Set<Permission> permissionsFor(FinesPermission... permissions) {
        return Arrays.stream(permissions)
            .map(permission -> Permission.builder()
                .permissionId(permission.getId())
                .permissionName(permission.getDescription())
                .build())
            .collect(Collectors.toSet());
    }
}
