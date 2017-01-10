package DPM_TEAM04.geometry;

import lejos.robotics.navigation.Pose;
/**
 * Represent both a 2D point and a direction. Useful for odometry and positioning
 * @author Kareem Halabi
 *
 */

public class DirectedCoordinate extends Coordinate {

	// All headings end up being normalized to radians
	double headingRad;

	public DirectedCoordinate(CoordinateSystem c, double c1, double c2,
			double direction, CoordinateSystem directionUnit) {
		super(c, c1, c2);
		setDirection(direction, directionUnit);
	}

	/**
	 * Gets the current heading of this in @param unit units.
	 * 
	 * @param unit
	 *            Desired angular unit
	 * @return Current heading in @param unit units. If POLAR_RAD or CARTESIAN
	 *         selected: returned value is confined to 0 <= theta < 2pi with 0
	 *         rad as x axis If POLAR_DEG selected: returned value is confined
	 *         to 0 <= theta < 360 with 0 deg as x axis If HEADING_DEG selected:
	 *         returned value is confined to 0 <= theta < 360 with 0 deg as y
	 *         axis
	 */
	public synchronized double getDirection(CoordinateSystem unit) {

		switch (unit) {
		case POLAR_RAD:
			return headingRad;
		case POLAR_DEG:
			return headingRad * 180 / Math.PI;
		case HEADING_DEG:
			double thetaDeg = headingRad * 180 / Math.PI;
			if (0 <= thetaDeg && thetaDeg <= 90)
				return 90 - thetaDeg;
			else
				return 450 - thetaDeg;
		default: // Default to rad if CARTESIAN selected
			return headingRad;
		}
	}

	/**
	 * Return coordinates and direction of this DirectedCoordinate
	 * 
	 * @param positionUnit
	 *            Units of spacial coordinates
	 * @param directionUnit
	 *            Unit of direction angle
	 * @return An array containing [c1, c2, direction]
	 */
	public synchronized double[] getCoordinates(CoordinateSystem positionUnit,
			CoordinateSystem directionUnit) {
		double[] positionAndDirection = new double[3];
		// get spacial coordinates
		System.arraycopy(super.getCoordinates(positionUnit), 0,
				positionAndDirection, 0, 2);

		// set direction
		positionAndDirection[2] = getDirection(directionUnit);

		return positionAndDirection;
	}

	public synchronized void setDirection(double heading,
			CoordinateSystem headingUnits) {

		switch (headingUnits) {
		case POLAR_RAD:
			headingRad = heading;
			break;

		case POLAR_DEG:
			headingRad = heading * Math.PI / 180; // convert to rad
			break;
		case HEADING_DEG:
			if (0 <= heading && heading <= 90) // convert to polar deg then rad
				headingRad = (90 - heading) * Math.PI / 180;
			else
				headingRad = (450 - heading) * Math.PI / 180;
			break;
		default:
			headingRad = heading; // Default to rad if CARTESIAN selected
		}

		// Map heading to be between 0 and 2pi
		while (headingRad > 2 * Math.PI)
			headingRad -= 2 * Math.PI;
		while (headingRad < 0)
			headingRad += 2 * Math.PI;
	}

	@SuppressWarnings("incomplete-switch")
	public synchronized void incrementDirection(double dHeading,
			CoordinateSystem headingUnits) {

		switch (headingUnits) {
		case POLAR_RAD:
			headingRad += dHeading;
			break;
		case POLAR_DEG:
			headingRad += dHeading * Math.PI / 180; // convert to rad
			break;
		case HEADING_DEG:
			headingRad -= dHeading * Math.PI / 180; // convert to rad
			break;
		// Do nothing if CARTESIAN selected
		}

		// Map heading to be between 0 and 2pi
		while (headingRad > 2 * Math.PI)
			headingRad -= 2 * Math.PI;
		while (headingRad < 0)
			headingRad += 2 * Math.PI;
	}

	public synchronized void setCoordinate(CoordinateSystem c, double c1,
			double c2, double direction, CoordinateSystem directionUnits) {
		super.setCoordinate(c, c1, c2);
		setDirection(direction, directionUnits);
	}

	public synchronized void incrementCoordinate(CoordinateSystem c,
			double dC1, double dC2, double dDirection,
			CoordinateSystem directionUnits) {
		super.incrementCoordinate(c, dC1, dC2);
		incrementDirection(dDirection, directionUnits);
	}

	/**
	 * Returns shortest angular displacement between this DirectedCoordinate and another Coordinate, @param
	 * dest
	 * 
	 * @param dest
	 *            Coordiante to compare to
	 * @param angleUnit
	 *            Unit of returned angle
	 * @return shortest angular displacement between this and dest. If the two
	 *         points are the same, 0 is returned
	 */
	public synchronized double angleTo(Coordinate dest,
			CoordinateSystem angleUnit) {
		return super.angleTo(dest, headingRad, CoordinateSystem.POLAR_RAD,
				angleUnit);
	}

	/**
	 * Returns shortest angular displacement between this DirectedCoordinate and an angle @param
	 * angle
	 * 
	 * @param angle
	 *            The angle to compare to
	 * @param angleUnit
	 *            Unit of @param angle as well as the returned angle
	 * @return shortest angular displacement between this and angle. If the two
	 *         directions are the same, 0 is returned
	 */
	public synchronized double directionTo(double angle,
			CoordinateSystem angleUnit) {

		double changeInAngle = 0;
		switch (angleUnit) {
		case CARTESIAN:
		case POLAR_RAD:
			changeInAngle = angle - this.getDirection(angleUnit);

			if (changeInAngle > Math.PI) {
				changeInAngle -= 2 * Math.PI;
			} else if (changeInAngle < -Math.PI) {
				changeInAngle += 2 * Math.PI;
			}
			break;
		case POLAR_DEG:
		case HEADING_DEG:
			changeInAngle = angle - this.getDirection(angleUnit);

			if (changeInAngle > 180) {
				changeInAngle -= 360;
			} else if (changeInAngle < -180) {
				changeInAngle += 360;
			}
		}

		return changeInAngle;
	}

	/**
	 * For use with the LeJOS geometry classes
	 * @return this DirectedCoordinate, represented as a Pose
	 */
	public synchronized Pose asPose() {
		return new Pose((float) this.x, (float) this.y,
				(float) getTheta(CoordinateSystem.HEADING_DEG));
	}
}
