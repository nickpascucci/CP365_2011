package boids;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Provides an implementation of a basic boid.
 * @author Matthew Polk
 * @author Nick Pascucci
 */

public class Boid {
	int x, y, size;
	float[] movementVector;
	Color color;
	
	/**
	 * Create a new boid at the give x,y coordinates.
	 * @param _x
	 * @param _y
	 * @param _size
	 */
	public Boid(int _x, int _y, int _size){
		x = _x;
		y = _y;
		size = _size;
		movementVector = new float[2];
		movementVector[0] = 0;
		movementVector[1] = 1;
	}
	
	/**
	 * Renders the given boid.
	 * @param g
	 */
	public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, size, size);
    }
}
