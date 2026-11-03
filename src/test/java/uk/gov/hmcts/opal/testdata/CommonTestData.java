package uk.gov.hmcts.opal.testdata;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2;

public class CommonTestData {

    private CommonTestData() {
        // Utility class, prevent instantiation
    }

    public static BusinessUnitUserV2 businessUnitUserWithPermission(String businessUnitId, FinesPermission permission) {
        return new BusinessUnitUserV2(
            "buUserId-1",
            Short.parseShort(businessUnitId),
            Set.of(permission.toCommonPermission())
        );
    }

    public static List<BusinessUnitUserV2> businessUnitUsersWithPermission(
        FinesPermission permission,
        String... businessUnitIds) {

        return Arrays.stream(businessUnitIds)
            .map(businessUnitId -> businessUnitUserWithPermission(businessUnitId, permission))
            .toList();
    }

}
