package fixtures.elevator.runtime.temporal

/** 
 * A handful of contents repeated for reuse in test code as derived from values set in the FrameworkRuntimeTestConfiguration.properties file that is imported for properties
 * when using the @EnableTestVirtualRuntime annotation to pre-configure sensible test-time defaults for the "info.mesosphere.elevator.runtime" module layer.
 * When writing tests for that layer, use the fixture annotation to bootstrap your Context Loaded, and de-refrence any constants you need to assert about 
 * correct configuration handling using values found here.
 * @author jheinnic
 */
final class TestSomeFixture {
	private new() {
	}

	public static final int NUM_FLOORS = 25
	public static final int NUM_ELEVATORS = 6
	public static final int FLOOR_INDEX_ONE = 4
	public static final int FLOOR_INDEX_TWO = 7
	public static final int FLOOR_INDEX_THREE = 10
	public static final int FLOOR_INDEX_FOUR = 16
	public static final int FLOOR_INDEX_FIVE = 19
	public static final int FLOOR_INDEX_TWO_FAST_DOWN = 3
	public static final int FLOOR_INDEX_TWO_FAST_UP = 11
}
