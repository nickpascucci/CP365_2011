package boids;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JComponent;

/**
 * Provides a drawing surface for the boid flock.
 * 
 * @author Matthew Polk
 * @author Nick Pascucci
 */

@SuppressWarnings("serial")
class BoidCanvas extends JComponent implements MouseMotionListener{
	final static float MAX_SPEED = 10;
	final static int NEIGHBOR_DISTANCE = 300;
	int CENTER_WEIGHT = 6;
	int AVOID_WEIGHT = 9;
	int MATCH_SPEED_WEIGHT = 2;
	int MOUSE_WEIGHT = 3;
	int mouseX;
	int mouseY;
	final static int RANDOMOSITY = 50;
	int SIZE = 5;
	ArrayList<Boid> boids;
	Random rand = new Random();

	/**
	 * Creates a new BoidCanvas object with defaults.
	 */
	public BoidCanvas() {
		boids = new ArrayList<Boid>();
		mouseX = this.getWidth()/2;
		mouseY = this.getHeight()/2;
		addMouseMotionListener(this);
	}

	/**
	 * Searches the boids ArrayList for nearby boids to this one.
	 */
	private ArrayList<Boid> getNearbyBoids(Boid b) {
		ArrayList<Boid> neighbors = new ArrayList<Boid>();

		for (Boid d : boids) {
			if (Math.sqrt((d.x - b.x)*(d.x - b.x) + (d.y - b.y)*(d.y - b.y)) < NEIGHBOR_DISTANCE)
				neighbors.add(d);
		}

		return neighbors;
	}

	/**
	 * Calculates the combined vectors for each boid.
	 */
	public void getVectors() {
		ArrayList<Boid> newBoids = new ArrayList<Boid>();
		for (Boid b : boids) {
			// Build a copy of the boids list so we can
			// modify the new situation without touching the original.
			// Touching the original can mess up future calculations.
			newBoids.add(b);
		}

		for (Boid newBoid : newBoids) {
			float[] cv = getCenterVector(newBoid);
			float[] av = getAwayVector(newBoid);
			float[] ms = getMatchSpeedVector(newBoid);
			float[] rv = {(rand.nextFloat()*RANDOMOSITY)+1-RANDOMOSITY/2, (rand.nextFloat()*RANDOMOSITY)+1-RANDOMOSITY/2};
			float[] fm = getMouseVector(newBoid);

			newBoid.movementVector[0] = CENTER_WEIGHT * cv[0] + AVOID_WEIGHT * av[0] + 
				MATCH_SPEED_WEIGHT * ms[0] + MOUSE_WEIGHT * fm[0] + rv[0];
			newBoid.movementVector[1] = CENTER_WEIGHT * cv[1] + AVOID_WEIGHT * av[1] + 
				MATCH_SPEED_WEIGHT * ms[1] + MOUSE_WEIGHT * fm[1] + rv[0];
			/*
			 * But, the vector may be very large. We should scale it down a bit.
			 * Remember to scale uniformly!
			 */
			newBoid.movementVector = toUnitVector(newBoid.movementVector);

			newBoid.movementVector[0] *= MAX_SPEED;
			newBoid.movementVector[1] *= MAX_SPEED;
		}
		boids = newBoids;
	}

	/*
	 * Converts a vector to a unit vector.
	 */
	private float[] toUnitVector(float[] vector){
		float x = (float) vector[0];
		float y = (float) vector[1];
		
		//Loses some precision here.
		float magnitude = (float) Math.sqrt(x*x + y*y);
		float newX = (float) x/magnitude;
		float newY = (float) y/magnitude;
		
		float[] newVector = {newX, newY};
		return newVector;
	}
	
	/**
	 * Generates the vector to the center of the flock.
	 * 
	 * @param currentBoid
	 *            The boid who is the origin of the vector.
	 * @return An int array representing the x,y unit vectors.
	 */
	public float[] getCenterVector(Boid currentBoid) {
		
		int centerX = getSwarmCenter()[0];
		int centerY = getSwarmCenter()[1];
		float[] centerVec = {centerX - currentBoid.x, centerY - currentBoid.y};
		return centerVec;
	}

	/**
	 * Calculates the weighted center of the swarm.
	 * @return
	 */
	public int[] getSwarmCenter(){
		int centerX = 0;
		int centerY = 0;
		
		//We calculate the center of the swarm by summing all of
		//the x and y coordinates, and then dividing by the number
		//of boids.
		for (Boid b : boids) {
			centerX += b.x;
			centerY += b.y;
		}
		centerX /= boids.size();
		centerY /= boids.size();
		
		//System.out.println("Swarm center at " + centerX + " " + centerY);		
		int[] center = {centerX, centerY};
		return center;
	}
	
	/**
	 * Generates a boid-specific vector to avoid neighbors.
	 * 
	 * @param b
	 * @return
	 */
	public float[] getAwayVector(Boid b) {
		float[] awayVec = new float[2];

		ArrayList<Boid> Neighborhood = getNearbyBoids(b);
		int Neighborhood_x = 0;
		int Neighborhood_y = 0;
		for (Boid d : Neighborhood) {
			Neighborhood_x -= (d.x - b.x);
			Neighborhood_y -= (d.y - b.y);
		}

		Neighborhood_x /= Neighborhood.size();
		Neighborhood_y /= Neighborhood.size();

		awayVec[0] = Neighborhood_x;
		awayVec[1] = Neighborhood_y;

		return awayVec;
	}

	/**
	 * Generates a vector to match the speed of a boid's neighbors.
	 * 
	 * @param b
	 * @return
	 */
	public float[] getMatchSpeedVector(Boid b) {
		float[] matchVec = new float[2];
		ArrayList<Boid> Neighborhood = getNearbyBoids(b);

		int neighborhoodDX = 0;
		int neighborhoodDY = 0;
		for (Boid d : Neighborhood) {
			neighborhoodDX += d.movementVector[0];
			neighborhoodDY += d.movementVector[1];
		}

		neighborhoodDX /= Neighborhood.size();
		neighborhoodDY /= Neighborhood.size();

		matchVec[0] = neighborhoodDX - b.movementVector[0];
		matchVec[1] = neighborhoodDY - b.movementVector[1];

		return matchVec;
	}
	
	public float[] getMouseVector(Boid b){
		float[] ans = {(mouseX - b.x), (mouseY - b.y)};
		return ans;
	}

	/**
	 * Runs the simulation.
	 * 
	 * @param num_boids
	 * @throws InterruptedException
	 */
	public void run(int num_boids) throws InterruptedException {
		for (int i = 0; i < num_boids; i++) {
			boids.add(new Boid(rand.nextInt(this.getWidth()), rand.nextInt(this.getWidth()), SIZE));
		}

		while (true) {
			getVectors();

			for (Boid b : boids) {
				b.x += b.movementVector[0];
				b.y += b.movementVector[1];
				
				if(b.x < 0)
					b.x = 0;
				else if(b.x > this.getWidth()-1)
					b.x = this.getWidth()-1;
				if(b.y < 0)
					b.y = 0;
				else if(b.y > this.getHeight()-1)
					b.y = this.getHeight()-1;
				
			}

			//System.out.println(boids.get(0).x + " " + boids.get(0).y);
			repaint();
			Thread.sleep(100);
		}
	}

	/**
	 * Drawing code.
	 */
	public void paintComponent(Graphics g) {
		super.repaint();
		for (Boid b : boids) {
			b.draw(g);
		}
		g.setColor(Color.RED);
        g.fillRect(getSwarmCenter()[0], getSwarmCenter()[1], 4, 4);
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		
	}
}
