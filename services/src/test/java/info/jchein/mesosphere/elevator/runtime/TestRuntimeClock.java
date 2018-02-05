package info.jchein.mesosphere.elevator.runtime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@TestPropertySource
@ContextConfiguration(classes= {RuntimeFrameworkConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestRuntimeIT
{
   @Test
   public void test() { }
}
