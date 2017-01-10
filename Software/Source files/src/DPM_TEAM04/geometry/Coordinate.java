package DPM_TEAM04.geometry;

import lejos.robotics.geometry.Point;

/**
 * Class to represent 2D points in various coordinate systems. Can also convert
 * between coordinate systems as computationally and spacially efficient as
 * possible.
 * 
 * @author KareemHalabi
 *
 */
public class Coordinate {
	protected double x;
	protected double y;

	/**
	 * Creates a new Coordinate in the desired coordinate system
	 * 
	 * @param c
	 *            desired coordinate system
	 * @param c1
	 *            x coordinate if Cartesian, r if Polar
	 * @param c2
	 *            y coordinate if Cartesian, theta if Polar or Compass Heading
	 */
	public Coordinate(CoordinateSystem c, double c1, double c2) {
		setCoordinate(c, c1, c2);
	}

	/**
	 * Reposition both dimensions of coordinate simultaneously
	 * 
	 * @param c
	 *            desired coordinate system
	 * @param c1
	 *            new x coordinate if Cartesian, new r if Polar
	 * @param c2
	 *            new y coordinate if Cartesian, new theta if Polar or Compass
	 *            Heading
	 */
	public synchronized void setCoordinate(CoordinateSystem c, double c1,
			double c2) {
		switch (c) {
		case CARTESIAN:
			this.x = c1;
			this.y = c2;
			break;
		case POLAR_RAD:
			this.x = c1 * Math.cos(c2);
			this.y = c1 * Math.sin(c2);
			break;
		case POLAR_DEG:
			this.x = c1 * Math.cos(c2 * Math.PI / 180);
			this.y = c1 * Math.sin(c2 * Math.PI / 180);
			break;
		case HEADING_DEG:
			this.x = c1 * Math.sin(c2 * Math.PI / 180);
			this.y = c2 * Math.cos(c2 * Math.PI / 180);
			break;
		}
	}

	// -------------------Getters-------------------

	
	public synchronized double getX() {
		return this.x;
	}

	public synchronized double getY() {
		return this.y;
	}

	/**
	 * Returns radial distance from origin. Always non-negative
	 * 
	 * @return radial distance from origin
	 */
	public synchronized double getR() {
		return Math.sqrt(x * x + y * y);
	}

	/**
	 * Gets the current angle of a non-origin coordinate in polar coordinate
	 * systems.
	 * 
	 * @param unit
	 *            Desired angular unit
	 * @return Angle in @param unit units. If POLAR_RAD or CARTESIAN selected:
	 *         returned value is confined to 0 <= theta < 2pi with 0 rad as x
	 *         axis If POLAR_DEG selected: returned value is confined to 0 <=
	 *         theta < 360 with 0 deg as x axis If HEADING_DEG selected:
	 *         returned value is confined to 0 <= theta < 360 with 0 deg as y
	 *         axis returns NaN if point is at origin
	 */
	public double getTheta(CoordinateSystem unit) {
		double thetaRad;

		// Since -pi <= Math.atan2(y,x) <= pi
		// We need to add a correction depending on
		// where the coordinate is

		synchronized (this) {
			thetaRad = Math.atan2(y, x);
		}
		if (thetaRad < 0) // normalize thetaRad to be between 0 and 2pi
			thetaRad += 2 * Math.PI;

		switch (unit) {
		case POLAR_RAD:
			return thetaRad;
		case POLAR_DEG:
			return thetaRad * 180 / Math.PI;
		case HEADING_DEG:
			// convert to deg, then perform transformation to heading
			double thetaDeg = thetaRad * 180 / Math.PI;
			if (0 <= thetaDeg && thetaDeg <= 90)
				return 90 - thetaDeg;
			else
				return 450 - thetaDeg;
		default: // Default to rad if CARTESIAN selected
			return thetaRad;
		}
	}

	public synchronized double[] getCoordinates(
			CoordinateSystem coordinateSystem) {
		switch (coordinateSystem) {
		case CARTESIAN:
			return new double[] { x, y };
		default: // All other angle-based coordinate systems
			return new double[] { getR(), getTheta(coordinateSystem) };
		}
	}

	// -------------------Setters-------------------

	public synchronized void setX(double newX) {
		this.x = newX;
	}

	public synchronized void setY(double newY) {
		this.y = newY;
	}

	public synchronized void setR(double newR) {
		this.x = newR * Math.cos(getTheta(CoordinateSystem.POLAR_RAD));
		this.y = newR * Math.sin(getTheta(CoordinateSystem.POLAR_RAD));
	}

	/**
	 * Sets new angle in desired CoordinateSystem. Does nothing if CARTESIAN is
	 * selected
	 * 
	 * @param newTheta
	 *            new angle in @param dThetaUnit units
	 * @param dThetaUnit
	 *            desired unit for @param newTheta
	 */
	@SuppressWarnings("incomplete-switch")
	public synchronized void setTheta(double newTheta,
			CoordinateSystem dThetaUnit) {
		double r = getR();
		double newThetaRad = newTheta * Math.PI / 180; // in case newTheta is
														// not in rads

		switch (dThetaUnit) {
		case POLAR_RAD:
			this.x = r * Math.cos(newTheta);
			this.y = r * Math.sin(newTheta);
			break;
		case POLAR_DEG:
			this.x = r * Math.cos(newThetaRad);
			this.y = r * Math.sin(newThetaRad);
			break;
		case HEADING_DEG:
			this.x = r * Math.sin(newThetaRad);
			this.y = r * Math.cos(newThetaRad);
			break;
		// Do nothing if CARTESIAN selected
		}
	}

	// -------------------Incrementers-------------------

	public synchronized void incrementX(double dx) {
		this.x += dx;
	}

	public synchronized void incrementY(double dy) {
		this.y += dy;
	}

	public synchronized void incrementR(double dr) {
		this.x += dr * Math.cos(getTheta(CoordinateSystem.POLAR_RAD));
		this.y += dr * Math.sin(getTheta(CoordinateSystem.POLAR_RAD));
	}

	/**
	 * Increments angle in desired CoordinateSystem. Does nothing if CARTESIAN
	 * is selected
	 * 
	 * @param dTheta
	 *            increment to apply in @param dThetaUnit units
	 * @param dThetaUnit
	 *            desired unit for @param dtheta
	 */
	@SuppressWarnings("incomplete-switch")
	public synchronized void incrementTheta(double dtheta,
			CoordinateSystem dThetaUnit) {
		double r = getR();
		double thetaRad = getTheta(CoordinateSystem.POLAR_RAD);
		double dthetaRad = dtheta * Math.PI / 180; // in case dtheta is in deg
													// or heading deg, otherwise
													// it won't get used

		switch (dThetaUnit) {
		case POLAR_RAD:
			this.x = r * Math.cos(dtheta + thetaRad);
			this.y = r * Math.sin(dtheta + thetaRad);
			break;
		case POLAR_DEG:
			this.x = r * Math.cos(dthetaRad + thetaRad);
			this.y = r * Math.sin(dthetaRad + thetaRad);
			break;
		case HEADING_DEG:
			this.x = r * Math.cos(dthetaRad - thetaRad);
			this.y = r * Math.sin(dthetaRad - thetaRad);
			break;
		// Do nothing if CARTESIAN selected
		}
	}

	/**
	 * Increment both coordinates
	 * 
	 * @param c
	 *            Coordinate system of dimensions
	 * @param dC1
	 *            First dimension to increment
	 * @param dC2
	 *            Second dimension to increment
	 */
	public synchronized void incrementCoordinate(CoordinateSystem c,
			double dC1, double dC2) {

		switch (c) {
		case CARTESIAN:
			incrementX(dC1);
			incrementY(dC2);
			break;
		default: // all other angle-based coordinate systems
			incrementR(dC1);
			incrementTheta(dC2, c);
			break;
		}
	}

	// -------------------Comparators-------------------

	/**
	 * Returns shortest distance between this and @param dest
	 * 
	 * @param dest
	 *            Coordiante to compare to
	 * @return Shortest distance between this and @param dest
	 */
	public double distanceTo(Coordinate dest) {

		double xDist;
		double yDist;

		synchronized (this) {
			synchronized (dest) {
				xDist = dest.x - this.x;
				yDist = dest.y - this.y;
			}
		}

		return Math.sqrt(xDist * xDist + yDist * yDist);
	}

	/**
	 * Returns shortest angular displacement between currentHeading and @param
	 * dest
	 * 
	 * @param dest
	 *            Coordiante to compare to
	 * @param currentHeading
	 *            the current heading in @param currentHeadingUnit. In deg
	 *            assuming input is 0 <= @param currentHeadingUnit < 360 or in
	 *            rad 0 <= @param currentHeadingUnit < 2pi
	 * @param currentHeadingUnit
	 *            CoordinateSystem of @param currentHeading
	 * @param returnedUnit
	 *            Unit of returned angle
	 * @return shortest angular displacement between this and dest. If the two
	 *         points are the same, 0 is returned
	 */
	public double angleTo(Coordinate dest, double currentHeading,
			CoordinateSystem currentHeadingUnit, CoordinateSystem returnedUnit) {

		double xDist;
		double yDist;

		synchronized (this) {
			synchronized (dest) {
				xDist = dest.x - this.x;
				yDist = dest.y - this.y;
			}
		}

		double destThetaRad = Math.atan2(yDist, xDist);

		if (destThetaRad < 0) // normalize angle to be between 0 and 2pi
			destThetaRad += 2 * Math.PI;

		// Normalize currentHeading to rads
		double currentHeadingRad;
		switch (currentHeadingUnit) {
		case POLAR_RAD:
			currentHeadingRad = currentHeading;
			break;
		case POLAR_DEG:
			currentHeadingRad = currentHeading * Math.PI / 180;
			break;
		case HEADING_DEG:
			if (0 <= currentHeading && currentHeading <= 90)
				currentHeadingRad = (90 - currentHeading) * Math.PI / 180;
			else
				currentHeadingRad = (450 - currentHeading) * Math.PI / 180;
			break;
		default:
			currentHeadingRad = currentHeading;
			break;
		}

		double changeInAngleRad = destThetaRad - currentHeadingRad;

		// Pick the shortest traversal
		if (changeInAngleRad > Math.PI) {
			changeInAngleRad -= 2 * Math.PI;
		} else if (changeInAngleRad < -Math.PI) {
			changeInAngleRad += 2 * Math.PI;
		}

		// Convert to desired unit
		switch (returnedUnit) {
		case POLAR_RAD:
			return changeInAngleRad;
		case POLAR_DEG:
			return changeInAngleRad * 180 / Math.PI;
		case HEADING_DEG:
			return -changeInAngleRad * 180 / Math.PI;
		default: // Default to rad if CARTESIAN selected
			return changeInAngleRad;
		}
	}

	/**
	 * Returns this Coordinate as a Point object for use in LeJOS geometry
	 * classes
	 * 
	 * @return this Coordinate as a Point object
	 */
	public synchronized Point asPoint() {
		return new Point((float) this.x, (float) this.y);
	}
}
