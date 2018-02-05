package info.jchein.mesosphere.elevator.control.physics;

import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

public class TestConstantVelocityPathLeg
{
   @ClassRule
   public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

   @Rule
   public final SpringMethodRule springMethodRule = new SpringMethodRule();
}
