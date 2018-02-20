package info.jchein.mesosphere.test.config.runtime.temporal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import info.jchein.mesosphere.elevator.common.bootstrap.BootstrapConfiguration;

@ContextConfiguration(classes= {BootstrapConfiguration.class, VirtualRuntimeTestConfiguration.class}, loader=AnnotationConfigContextLoader.class)
@TestPropertySource("classpath:/info/jchein/mesosphere/configuration/tests/TestRuntimeBootstrap.properties")
@ActiveProfiles("elevator.runtime.virtual")
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableTestVirtualRuntime
{

}
