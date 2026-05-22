package uk.gov.hmcts.opal.support;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import org.springframework.http.MediaType;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;

public final class UserServiceStub {

    public static final String USER_STATE_PATH = "/v2/users/0/state";
    public static final String V2_USER_STATE = """
        {
          "user_id" : 500000000,
          "username" : "opal-test@HMCTS.NET",
          "name" : "Pablo",
          "status" : "ACTIVE",
          "version" : 0,
          "cache_name" : "USER_STATE_k9LpT2xVqR8m",
          "domains" : {
            "fines" : {
              "business_unit_users" : [ {
                "business_unit_user_id" : "L065JG",
                "business_unit_id" : 70,
                "permissions" : [ {
                  "permission_id" : 1,
                  "permission_name" : "Create and Manage Draft Accounts"
                }, {
                  "permission_id" : 3,
                  "permission_name" : "Account Enquiry"
                }, {
                  "permission_id" : 4,
                  "permission_name" : "Collection Order"
                }, {
                  "permission_id" : 5,
                  "permission_name" : "Check and Validate Draft Accounts"
                }, {
                  "permission_id" : 6,
                  "permission_name" : "Search and View Accounts"
                } ]
              }, {
                "business_unit_user_id" : "L066JG",
                "business_unit_id" : 68,
                "permissions" : [ ]
              }, {
                "business_unit_user_id" : "L067JG",
                "business_unit_id" : 73,
                "permissions" : [ ]
              }, {
                "business_unit_user_id" : "L073JG",
                "business_unit_id" : 71,
                "permissions" : [ ]
              }, {
                "business_unit_user_id" : "L077JG",
                "business_unit_id" : 67,
                "permissions" : [ ]
              }, {
                "business_unit_user_id" : "L078JG",
                "business_unit_id" : 69,
                "permissions" : [ ]
              }, {
                "business_unit_user_id" : "L080JG",
                "business_unit_id" : 61,
                "permissions" : [ ]
              } ]
            }
          }
        }""";
    private static final int DEFAULT_BUSINESS_UNIT_ID = 70;
    private static final String USER_STATE_BY_ID_PATH_TEMPLATE = "/users/%d/state";
    private static final String CREATE_MANAGE_DRAFT_ACCOUNTS_PERMISSION = """
        [
          {
            "permission_id": 1,
            "permission_name": "Create and Manage Draft Accounts"
          }
        ]
        """;
    private static final String ALL_PERMISSIONS = """
        [
          {
            "permission_id": 1,
            "permission_name": "Create and Manage Draft Accounts"
          },
          {
            "permission_id": 2,
            "permission_name": "Account Enquiry - Account Notes"
          },
          {
            "permission_id": 3,
            "permission_name": "Account Enquiry"
          },
          {
            "permission_id": 4,
            "permission_name": "Collection Order"
          },
          {
            "permission_id": 5,
            "permission_name": "Check and Validate Draft Accounts"
          },
          {
            "permission_id": 6,
            "permission_name": "Search and View Accounts"
          },
          {
            "permission_id": 7,
            "permission_name": "Account Maintenance"
          },
          {
            "permission_id": 9,
            "permission_name": "Amend Payment Terms"
          },
          {
            "permission_id": 10,
            "permission_name": "Enter Enforcement"
          },
          {
            "permission_id": 13,
            "permission_name": "Consolidate"
          },
          {
            "permission_id": 14,
            "permission_name": "Add and Remove payment hold"
          }
        ]
        """;
    private static final String NO_PERMISSIONS = "[]";
    private static final String USER_STATE_BY_ID_TEMPLATE = """
        {
          "user_id": %d,
          "username": "%s",
          "name": "%s",
          "status": "ACTIVE",
          "version": 0,
          "business_unit_users": [
        %s
          ]
        }
        """;
    private static final String USER_STATE_TEMPLATE = """
        {
          "user_id": %d,
          "username": "%s",
          "name": "%s",
          "status": "ACTIVE",
          "version": 0,
          "cache_name": "USER_STATE_test_permissions",
          "domains": {
            "fines": {
              "business_unit_users": [
        %s
              ]
            }
          }
        }
        """;
    private static final String BUSINESS_UNIT_USER_TEMPLATE = """
                {
                  "business_unit_user_id": "%s",
                  "business_unit_id": %d,
                  "permissions": %s
                }""";
    private static final String USER_STATE_WITH_NO_BUSINESS_UNITS = """
        {
          "user_id": 500000000,
          "username": "opal-test@HMCTS.NET",
          "name": "Pablo",
          "status": "ACTIVE",
          "version": 0,
          "cache_name": "USER_STATE_no_business_units",
          "domains": {
            "fines": {
              "business_unit_users": []
            }
          }
        }
        """;

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

    public static void stubUserStateById(long userId, String userStateJson) {
        stubFor(get(USER_STATE_BY_ID_PATH_TEMPLATE.formatted(userId))
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(userStateJson)));
    }

    public static void stubUserStateByIdNotFound(long userId) {
        stubFor(get(USER_STATE_BY_ID_PATH_TEMPLATE.formatted(userId))
            .willReturn(aResponse().withStatus(404)));
    }

    public static void stubUserStateByIdWithPermission(long userId, int businessUnitId, FinesPermission permission) {
        stubUserStateById(userId, userStateByIdForBusinessUnit(userId, businessUnitId, permissionJson(permission)));
    }

    public static void stubUserWithAllPermissions(int businessUnitId) {
        stubAuthorisedUser(userStateForBusinessUnit(businessUnitId, ALL_PERMISSIONS));
    }

    public static void stubUserWithCreateManageDraftAccountsPermission(int businessUnitId) {
        stubAuthorisedUser(userStateForBusinessUnit(businessUnitId, CREATE_MANAGE_DRAFT_ACCOUNTS_PERMISSION));
    }

    public static void stubUserWithPermission(int businessUnitId, FinesPermission permission) {
        stubAuthorisedUser(userStateForBusinessUnit(businessUnitId, permissionJson(permission)));
    }

    public static void stubUserWithPermissions(int businessUnitId, FinesPermission... permissions) {
        stubAuthorisedUser(userStateForBusinessUnit(businessUnitId, permissionsJson(permissions)));
    }

    public static void stubUserWithPermissions(int businessUnitId, String businessUnitUserId,
                                               FinesPermission... permissions) {
        stubAuthorisedUser(userStateForBusinessUnit(
            businessUnitId, 1L, "normal@users.com", "normal@users.com", businessUnitUserId,
            permissionsJson(permissions)));
    }

    public static void stubUserWithPermissions(int businessUnitId, long userId, String username, String name,
                                               String businessUnitUserId, FinesPermission... permissions) {
        stubAuthorisedUser(userStateForBusinessUnit(
            businessUnitId, userId, username, name, businessUnitUserId, permissionsJson(permissions)));
    }

    public static void stubNormalUserWithPermissions(int businessUnitId, FinesPermission... permissions) {
        stubAuthorisedUser(userStateForBusinessUnit(
            businessUnitId, 1L, "normal@users.com", "normal@users.com", "USER01", permissionsJson(permissions)));
    }

    public static void stubNormalUserWithAllPermissions(int businessUnitId) {
        stubAuthorisedUser(userStateForBusinessUnit(businessUnitId, 1L, "normal@users.com", "normal@users.com",
                                                    "USER01", ALL_PERMISSIONS));
    }

    public static void stubNormalUserWithPermissionsForBusinessUnits(int[] businessUnitIds,
                                                                     FinesPermission... permissions) {
        stubAuthorisedUser(userStateForBusinessUnits(
            businessUnitIds, 1L, "normal@users.com", "normal@users.com", "USER01", permissionsJson(permissions)));
    }

    public static void stubDeveloperUserWithAllPermissions(int... businessUnitIds) {
        stubAuthorisedUser(userStateForBusinessUnits(businessUnitIds, 0L, "Developer_User", "Developer_User", "",
                                                    ALL_PERMISSIONS));
    }

    public static void stubUserWithNoPermissions(int businessUnitId) {
        stubAuthorisedUser(userStateForBusinessUnit(businessUnitId, NO_PERMISSIONS));
    }

    public static void stubUserNotLinkedToAnyBusinessUnit() {
        stubAuthorisedUser(USER_STATE_WITH_NO_BUSINESS_UNITS);
    }

    public static void stubUserServiceUnauthorized() {
        stubFor(get(USER_STATE_PATH)
            .willReturn(aResponse().withStatus(401)));
    }

    public static void stubUserServiceForbidden() {
        stubUserServiceStatus(403);
    }

    public static void stubUserServiceStatus(int status) {
        stubFor(get(USER_STATE_PATH)
            .willReturn(aResponse().withStatus(status)));
    }

    private static String userStateForBusinessUnit(int businessUnitId, String permissionsJson) {
        return userStateForBusinessUnit(
            businessUnitId, 500000000L, "opal-test@HMCTS.NET", "Pablo",
            "L%03dJG".formatted(businessUnitId), permissionsJson);
    }

    private static String userStateForBusinessUnit(int businessUnitId, long userId, String username, String name,
                                                   String businessUnitUserId, String permissionsJson) {
        return userState(userId, username, name, businessUnitUserJson(businessUnitId, businessUnitUserId,
                                                                      permissionsJson));
    }

    private static String userStateByIdForBusinessUnit(long userId, int businessUnitId, String permissionsJson) {
        return USER_STATE_BY_ID_TEMPLATE.formatted(
            userId,
            "normal@users.com",
            "normal@users.com",
            businessUnitUserJson(businessUnitId, "USER01", permissionsJson)
        );
    }

    private static String permissionJson(FinesPermission permission) {
        return permissionsJson(permission);
    }

    private static String permissionsJson(FinesPermission... permissions) {
        String permissionJson = java.util.Arrays.stream(permissions)
            .map(UserServiceStub::permissionObjectJson)
            .collect(java.util.stream.Collectors.joining(",\n"));
        return "[\n%s\n]".formatted(permissionJson);
    }

    private static String permissionObjectJson(FinesPermission permission) {
        return """
          {
            "permission_id": %d,
            "permission_name": "%s"
          }
        """.formatted(permission.getId(), permission.getDescription());
    }

    private static String userStateForBusinessUnits(int[] businessUnitIds, long userId, String username, String name,
                                                    String businessUnitUserId, String permissionsJson) {
        String businessUnitUsersJson = java.util.Arrays.stream(businessUnitIds)
            .mapToObj(businessUnitId -> businessUnitUserJson(businessUnitId, businessUnitUserId, permissionsJson))
            .collect(java.util.stream.Collectors.joining(",\n"));
        return userState(userId, username, name, businessUnitUsersJson);
    }

    private static String businessUnitUserJson(int businessUnitId, String businessUnitUserId, String permissionsJson) {
        return BUSINESS_UNIT_USER_TEMPLATE.formatted(businessUnitUserId, businessUnitId, permissionsJson);
    }

    private static String userState(long userId, String username, String name, String businessUnitUsersJson) {
        return USER_STATE_TEMPLATE.formatted(userId, username, name, businessUnitUsersJson);
    }
}
