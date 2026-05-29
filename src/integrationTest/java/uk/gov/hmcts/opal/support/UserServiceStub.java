package uk.gov.hmcts.opal.support;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.client.dto.BusinessUnitUserDto;
import uk.gov.hmcts.opal.common.user.authorisation.client.dto.PermissionDto;
import uk.gov.hmcts.opal.common.user.authorisation.client.dto.UserStateDto;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStatus;

public final class UserServiceStub {

    private static final short DEFAULT_BUSINESS_UNIT_ID = 70;
    private static final int DEFAULT_USER_ID = 1;
    private static final String DEFAULT_USERNAME = "opal-test@HMCTS.NET";
    private static final String DEFAULT_NAME = "Pablo";
    private static final String NORMAL_USER = "normal@users.com";
    private static final String NORMAL_BUSINESS_UNIT_USER_ID = "USER01";
    private static final String DEVELOPER_USER = "Developer_User";
    private static final FinesPermission[] ALL_PERMISSIONS = FinesPermission.values();
    private static final List<FinesPermission> ALL_PERMISSIONS_LIST = Arrays.asList(ALL_PERMISSIONS);
    private static final String USER_STATE_BY_ID_PATH_TEMPLATE = "/users/%d/state";

    public static final String USER_STATE_PATH = "/v2/users/0/state";
    public static final String V2_USER_STATE = TestUtil.toJsonString(createDefaultUserState());

    private UserServiceStub() {
    }

    public static void stubAuthorisedUser() {
        stubUserWithAllPermissions(DEFAULT_BUSINESS_UNIT_ID);
    }

    public static void stubAuthorisedUser(String userStateJson) {
        stubFor(get(USER_STATE_PATH)
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(userStateJson)));
    }

    public static void stubAuthorisedUser(UserStateV2 userState) {
        stubAuthorisedUser(TestUtil.toJsonString(userState));
    }

    public static void stubUserStateById(long userId, String userStateJson) {
        stubFor(get(USER_STATE_BY_ID_PATH_TEMPLATE.formatted(userId))
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(userStateJson)));
    }

    public static void stubUserStateById(long userId, UserStateDto userState) {
        stubUserStateById(userId, TestUtil.toJsonString(userState));
    }

    public static void stubUserStateByIdNotFound(long userId) {
        stubFor(get(USER_STATE_BY_ID_PATH_TEMPLATE.formatted(userId))
            .willReturn(aResponse().withStatus(404)));
    }

    public static void stubUserStateByIdWithPermission(long userId, int businessUnitId, FinesPermission permission) {
        stubUserStateById(
            userId,
            userStateByIdForBusinessUnit(userId, (short) businessUnitId, List.of(permission))
        );
    }

    public static void stubUserWithAllPermissions(int businessUnitId) {
        stubUserWithAllPermissions((short) businessUnitId);
    }

    public static void stubUserWithAllPermissions(short businessUnitId) {
        stubUserWithPermissions(businessUnitId, ALL_PERMISSIONS);
    }

    public static void stubUserWithAllPermissions(short... businessUnitIds) {
        stubUserWithPermissionsForBusinessUnits(businessUnitIds, ALL_PERMISSIONS);
    }

    public static void stubUserWithCreateManageDraftAccountsPermission(int businessUnitId) {
        stubUserWithPermissions((short) businessUnitId, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);
    }

    public static void stubUserWithPermission(int businessUnitId, FinesPermission permission) {
        stubUserWithPermissions((short) businessUnitId, permission);
    }

    public static void stubUserWithPermission(short businessUnitId, FinesPermission permission) {
        stubUserWithPermissions(businessUnitId, permission);
    }

    public static void stubUserWithPermissions(int businessUnitId, FinesPermission... permissions) {
        stubUserWithPermissions((short) businessUnitId, permissions);
    }

    public static void stubUserWithPermissions(short businessUnitId, FinesPermission... permissions) {
        stubAuthorisedUser(createUserStateWithPermissionsForBusinessUnit(businessUnitId, permissions));
    }

    public static void stubUserWithPermissions(int businessUnitId, String businessUnitUserId,
                                               FinesPermission... permissions) {
        stubAuthorisedUser(buildUserStateForBusinessUnits(
            DEFAULT_USER_ID,
            NORMAL_USER,
            NORMAL_USER,
            Map.of((short) businessUnitId, businessUnitUser(businessUnitId, businessUnitUserId, permissions))
        ));
    }

    public static void stubUserWithPermissions(int businessUnitId, long userId, String username, String name,
                                               String businessUnitUserId, FinesPermission... permissions) {
        stubAuthorisedUser(buildUserStateForBusinessUnits(
            userId,
            username,
            name,
            Map.of((short) businessUnitId, businessUnitUser(businessUnitId, businessUnitUserId, permissions))
        ));
    }

    public static void stubNormalUserWithPermissions(int businessUnitId, FinesPermission... permissions) {
        stubAuthorisedUser(buildUserStateForBusinessUnits(
            DEFAULT_USER_ID,
            NORMAL_USER,
            NORMAL_USER,
            Map.of((short) businessUnitId, businessUnitUser(businessUnitId, NORMAL_BUSINESS_UNIT_USER_ID, permissions))
        ));
    }

    public static void stubNormalUserWithAllPermissions(int businessUnitId) {
        stubNormalUserWithPermissions(businessUnitId, ALL_PERMISSIONS);
    }

    public static void stubNormalUserWithPermissionsForBusinessUnits(int[] businessUnitIds,
                                                                     FinesPermission... permissions) {
        Map<Short, BusinessUnitUser> businessUnitUsers = Arrays.stream(businessUnitIds)
            .boxed()
            .collect(Collectors.toMap(
                Integer::shortValue,
                businessUnitId -> businessUnitUser(businessUnitId, NORMAL_BUSINESS_UNIT_USER_ID, permissions)
            ));
        stubAuthorisedUser(buildUserStateForBusinessUnits(
            DEFAULT_USER_ID,
            NORMAL_USER,
            NORMAL_USER,
            businessUnitUsers
        ));
    }

    public static void stubDeveloperUserWithAllPermissions(int... businessUnitIds) {
        Map<Short, BusinessUnitUser> businessUnitUsers = Arrays.stream(businessUnitIds)
            .boxed()
            .collect(Collectors.toMap(
                Integer::shortValue,
                businessUnitId -> businessUnitUser(businessUnitId, "", ALL_PERMISSIONS)
            ));
        stubAuthorisedUser(buildUserStateForBusinessUnits(0L, DEVELOPER_USER, DEVELOPER_USER, businessUnitUsers));
    }

    public static void stubUserWithNoPermissions(int businessUnitId) {
        stubUserWithNoPermissions((short) businessUnitId);
    }

    public static void stubUserWithNoPermissions(short businessUnitId) {
        stubUserWithPermissions(businessUnitId);
    }

    public static void stubUserWithNoPermissions(short... businessUnitIds) {
        stubUserWithPermissionsForBusinessUnits(businessUnitIds);
    }

    public static void stubUserWithPermissionsForBusinessUnits(short[] businessUnitIds,
                                                              FinesPermission... permissions) {
        stubAuthorisedUser(createUserStateWithPermissionsForBusinessUnits(businessUnitIds, permissions));
    }

    public static UserStateV2 createUserStateWithPermissionsForBusinessUnits(short[] businessUnitIds,
                                                                             FinesPermission... permissions) {
        Map<Short, List<FinesPermission>> businessUnitIdToPermissions = new HashMap<>();
        for (short businessUnitId : businessUnitIds) {
            businessUnitIdToPermissions.put(businessUnitId, List.of(permissions));
        }
        return createUserStateForBusinessUnit(businessUnitIdToPermissions);
    }

    public static UserStateV2 createUserStateWithPermissionsForBusinessUnit(short businessUnitId,
                                                                            FinesPermission... permissions) {
        return createUserStateForBusinessUnit(Map.of(businessUnitId, List.of(permissions)));
    }

    public static UserStateV2 createUserStateForBusinessUnit(
        Map<Short, List<FinesPermission>> businessUnitIdToPermissions) {

        Map<Short, BusinessUnitUser> businessUnitUsers = businessUnitIdToPermissions.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> createBusinessUnitUser(entry.getKey(), "L%03dJG".formatted(entry.getKey()), entry.getValue())
            ));
        return buildUserStateForBusinessUnits(
            DEFAULT_USER_ID,
            DEFAULT_USERNAME,
            DEFAULT_NAME,
            businessUnitUsers
        );
    }

    public static UserStateV2 createDefaultUserState() {
        return createDefaultUserState(DEFAULT_USER_ID);
    }

    public static UserStateV2 createDefaultUserState(int userId) {
        return createDefaultUserState(userId, List.of(
            createBusinessUnitUser((short) 70, "L065JG", ALL_PERMISSIONS_LIST),
            createBusinessUnitUser((short) 68, "L066JG", List.of()),
            createBusinessUnitUser((short) 73, "L067JG", List.of()),
            createBusinessUnitUser((short) 71, "L073JG", List.of()),
            createBusinessUnitUser((short) 67, "L077JG", List.of()),
            createBusinessUnitUser((short) 69, "L078JG", List.of()),
            createBusinessUnitUser((short) 61, "L080JG", List.of())
        ));
    }

    public static UserStateV2 createDefaultUserState(int userId, short businessUnitId) {
        return createDefaultUserState(userId, List.of(
            createBusinessUnitUser(businessUnitId, "USER" + businessUnitId, ALL_PERMISSIONS_LIST)
        ));
    }

    public static UserStateV2 createDefaultUserState(int userId, List<BusinessUnitUser> businessUnitUsers) {
        Map<Domain, DomainBusinessUnitUsers> domains = new EnumMap<>(Domain.class);
        domains.put(
            Domain.FINES,
            DomainBusinessUnitUsers.builder().businessUnitUsers(businessUnitUsers).build()
        );

        return UserStateV2.builder()
            .userId((long) userId)
            .username(DEFAULT_USERNAME)
            .name(DEFAULT_NAME)
            .status(UserStatus.ACTIVE)
            .version(1L)
            .domains(domains)
            .build();
    }

    public static BusinessUnitUser createBusinessUnitUser(short businessUnitId, String businessUnitUserId,
                                                          List<FinesPermission> permissions) {
        return BusinessUnitUser.builder()
            .businessUnitUserId(businessUnitUserId)
            .businessUnitId(businessUnitId)
            .permissions(createPermissions(permissions))
            .build();
    }

    public static Set<Permission> createPermissions(List<FinesPermission> permissions) {
        return permissions.stream()
            .map(UserServiceStub::getPermission)
            .collect(Collectors.toSet());
    }

    public static Permission getPermission(FinesPermission permission) {
        return Permission.builder()
            .permissionId(permission.getId())
            .permissionName(permission.getDescription())
            .build();
    }

    public static void stubUserNotLinkedToAnyBusinessUnit() {
        stubAuthorisedUser(createDefaultUserState(500000000, List.of()));
    }

    public static void stubUserServiceUnauthorized() {
        stubUserServiceStatus(401);
    }

    public static void stubUserServiceForbidden() {
        stubUserServiceStatus(403);
    }

    public static void stubUserServiceStatus(int status) {
        stubFor(get(USER_STATE_PATH)
            .willReturn(aResponse().withStatus(status)));
    }

    private static UserStateV2 buildUserStateForBusinessUnits(long userId, String username, String name,
                                                              Map<Short, BusinessUnitUser> businessUnitUsers) {
        Map<Domain, DomainBusinessUnitUsers> domains = new EnumMap<>(Domain.class);
        domains.put(
            Domain.FINES,
            DomainBusinessUnitUsers.builder().businessUnitUsers(List.copyOf(businessUnitUsers.values())).build()
        );
        return UserStateV2.builder()
            .userId(userId)
            .username(username)
            .name(name)
            .status(UserStatus.ACTIVE)
            .version(1L)
            .domains(domains)
            .build();
    }

    private static BusinessUnitUser businessUnitUser(int businessUnitId, String businessUnitUserId,
                                                     FinesPermission... permissions) {
        return createBusinessUnitUser((short) businessUnitId, businessUnitUserId, List.of(permissions));
    }

    private static UserStateDto userStateByIdForBusinessUnit(long userId, short businessUnitId,
                                                             List<FinesPermission> permissions) {
        return UserStateDto.builder()
            .userId(userId)
            .username(NORMAL_USER)
            .name(NORMAL_USER)
            .status(UserStatus.ACTIVE.name())
            .version(0L)
            .businessUnitUsers(List.of(createBusinessUnitUserDto(businessUnitId, permissions)))
            .build();
    }

    private static BusinessUnitUserDto createBusinessUnitUserDto(short businessUnitId,
                                                                 List<FinesPermission> permissions) {
        return new BusinessUnitUserDto(
            NORMAL_BUSINESS_UNIT_USER_ID,
            businessUnitId,
            permissions.stream()
                .map(UserServiceStub::createPermissionDto)
                .toList()
        );
    }

    private static PermissionDto createPermissionDto(FinesPermission permission) {
        return new PermissionDto(permission.getId(), permission.getDescription());
    }
}
