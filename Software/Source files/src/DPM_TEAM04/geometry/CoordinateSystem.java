package DPM_TEAM04.geometry;

/*
 * Choose a coordinate system
 * 
 * CARTESIAN:			POLAR_RAD & DEG		  HEADING
 * 
 * +y					^					  North  
 * ^					|     ^ +r			  ^     ^ +r
 * |    				|    /				  |-   /
 * |   					|   /				  | \ /  +theta (deg only)
 * | 					|  /--				  |  /
 * | 					| /   \				  | /
 * |					|/     | +theta	  	  |/
 * +-------------> +x	+------------->		  +-------------> East
 * 						(0 angle is x axis)	  (0 angle is y axis)
 * 
 */


/** Choose a coordinate System between : CARTESIAN, POLAR_RAD, POLAR_DEG, HEADING_DEG.
 * 
 * 
  * @author KareemHalabi
  */


public enum CoordinateSystem {
	CARTESIAN("Cartesian", "x", "y"), POLAR_RAD("Polar radians", "r", "T"), POLAR_DEG(
			"Polar degrees", "r", "T"), HEADING_DEG("Heading degrees", "r", "T");

	private final String name;
	private final String c1;
	private final String c2;

	private CoordinateSystem(String name, String c1, String c2) {
		this.name = name;
		this.c1 = c1;
		this.c2 = c2;
	}

	public String getName() {
		return this.name();
	}

	public String getC1() {
		return this.c1;
	}

	public String getC2() {
		return this.c2;
	}

	@Override
	public String toString() {
		return String.format("%s: (%s, %s)", name, c1, c2);
	}
}