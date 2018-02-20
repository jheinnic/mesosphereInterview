package info.jchein.mesosphere.test.config.runtime.temporal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@ContextConfiguration(classes= {EmulatorBootstrapTestConfiguration.class}, loader=AnnotationConfigContextLoader.class)
@TestPropertySource("TestEmulatorBootstrap.properties")
@ActiveProfiles("elevator.runtime.virtual")
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableTestEmulatorInstance
{

}
