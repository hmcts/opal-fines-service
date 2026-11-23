package uk.gov.hmcts.opal.support;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserAuthenticationService;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;

@Service
public class TestSystemUserAuthenticationService extends SystemUserAuthenticationService {

    private UserStateStub userStateStub;

    public TestSystemUserAuthenticationService() {
        super(null, null, null);
    }

    public TestSystemUserAuthenticationService(UserStateStub userStateStub) {
        super(null, null, null);
        this.userStateStub = userStateStub;
    }

    public OpalJwtAuthenticationToken testSetupAsSystemUser(UserStateStub userStateStub) {
        this.userStateStub = userStateStub;
        return setupAsSystemUser(SystemUserEnum.OPAL_SYSTEM_USER);
    }

    @Override
    public OpalJwtAuthenticationToken setupAsSystemUser(SystemUserEnum systemUserEnum) {
        OpalJwtAuthenticationToken auth = userStateStub.getOpalJwtAuthenticationToken();
        SecurityContextHolder.getContext().setAuthentication(auth);
        return auth;
    }

}