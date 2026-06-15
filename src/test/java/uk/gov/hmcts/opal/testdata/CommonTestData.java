package uk.gov.hmcts.opal.testdata;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;

public class CommonTestData {

    private CommonTestData() {
        // Utility class, prevent instantiation
    }

    public static BusinessUnitUser businessUnitUserWithPermission(String businessUnitId, FinesPermission permission) {
        return new BusinessUnitUser(
            "buUserId-1",
            Short.parseShort(businessUnitId),
            Set.of(permission.toUserPermission())
        );
    }

    public static List<BusinessUnitUser> businessUnitUsersWithPermission(
        FinesPermission permission,
        String... businessUnitIds) {

        return Arrays.stream(businessUnitIds)
            .map(businessUnitId -> businessUnitUserWithPermission(businessUnitId, permission))
            .toList();
    }

}
