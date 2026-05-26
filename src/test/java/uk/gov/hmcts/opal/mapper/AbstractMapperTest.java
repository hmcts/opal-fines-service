package uk.gov.hmcts.opal.mapper;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest.TestConfig;

@SpringJUnitConfig(classes = TestConfig.class)
public abstract class AbstractMapperTest {
    @Configuration
    @ComponentScan(basePackageClasses = AbstractMapperTest.class)
    static class TestConfig {

    }
}
