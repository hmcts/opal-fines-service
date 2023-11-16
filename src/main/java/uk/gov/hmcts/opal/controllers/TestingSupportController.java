package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.service.DynamicConfigService;

@RestController
@RequestMapping("/api/testing-support")
@RequiredArgsConstructor
public class TestingSupportController {

    private final DynamicConfigService dynamicConfigService;

    @GetMapping("/app-mode")
    @Operation(summary = "Retrieves the value for app mode.")
    public ResponseEntity<AppMode> getAppMode() {
        return ResponseEntity.ok(dynamicConfigService.getAppMode());
    }

    @PutMapping("/app-mode")
    @Operation(summary = "Updates the value for app mode.")
    public ResponseEntity<AppMode> updateMode(@RequestBody AppMode mode) {
        return ResponseEntity.accepted().body(this.dynamicConfigService.updateAppMode(mode));
    }
}
