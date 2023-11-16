package uk.gov.hmcts.opal.service;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AppMode;

@Service
public class DynamicConfigService {

    private final DynamicStringProperty appMode;

    public DynamicConfigService(@Value("${app-mode}") String defaultMode) {
        this.appMode = DynamicPropertyFactory.getInstance()
            .getStringProperty("app-mode", defaultMode);
    }

    public AppMode getAppMode() {
        return AppMode.builder()
            .mode(this.appMode.get())
            .build();
    }

    public AppMode updateAppMode(AppMode newValue) {
        ConfigurationManager.getConfigInstance().setProperty("app-mode", newValue.getMode());
        return this.getAppMode();
    }
}
