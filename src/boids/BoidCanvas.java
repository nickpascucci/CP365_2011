package boids;

import java.awt.Color;
import java.awt.Graphics;
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
class BoidCanvas extends JComponent {
	final static int MAX_SPEED = 10;
	final static int NEIGHBOR_DISTANCE = 100;
	int SIZE = 5;
	ArrayList<Boid> boids;

	/**
	 * Creates a new BoidCanvas object with defaults.
	 */
	public BoidCanvas() {
		boids = new ArrayList<Boid>();
	}

	/**
	 * Searches the boids ArrayList for nearby boids to this one.
	 */
	private ArrayList<Boid> getNearbyBoids(Boid b) {
		ArrayList<Boid> neighbors = new ArrayList<Boid>();

		for (Boid d : boids) {
			if (d.x - b.x + (d.y - b.y) < NEIGHBOR_DISTANCE)
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
			int[] cv = getCenterVector(newBoid);
			int[] av = getAwayVector(newBoid);
			int[] ms = getMatchSpeedVector(newBoid);
			
			// Combined vector is the sum of the contributing vectors.
			newBoid.movementVector[0] = cv[0] + av[0] + ms[0];
			newBoid.movementVector[1] = cv[1] + av[1] + ms[1];

			/*
			 * But, the vector may be very large. We should scale it down a bit.
			 * Remember to scale uniformly!
			 */
			newBoid.movementVector[0] /= 10;
			newBoid.movementVector[1] /= 10;
		}
		boids = newBoids;
	}

	/*
	 * Converts a vector to a unit vector.
	 */
	private float[] toUnitVector(int[] vector){
		float x = (float) vector[0];
		float y = (float) vector[1];
		
		//Loses some precision here.
		float magnitude = (float) Math.sqrt(x*x + y*y);
		float newX = (float) x/magnitude;
		float newY = (float) y/magnitude;
		
		float[] newVector = {};
		return newVector;
	}
	
	/**
	 * Generates the vector to the center of the flock.
	 * 
	 * @param currentBoid
	 *            The boid who is the origin of the vector.
	 * @return An int array representing the x,y unit vectors.
	 */
	public int[] getCenterVector(Boid currentBoid) {
		
		int centerX = getSwarmCenter()[0];
		int centerY = getSwarmCenter()[1];
		int[] centerVec = {centerX - currentBoid.x, centerY - currentBoid.y};
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
		
		System.out.println("Swarm center at " + centerX + " " + centerY);		
		int[] center = {centerX, centerY};
		return center;
	}
	
	/**
	 * Generates a boid-specific vector to avoid neighbors.
	 * 
	 * @param b
	 * @return
	 */
	public int[] getAwayVector(Boid b) {
		int[] awayVec = new int[2];

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
	public int[] getMatchSpeedVector(Boid b) {
		int[] matchVec = new int[2];
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

	/**
	 * Runs the simulation.
	 * 
	 * @param num_boids
	 * @throws InterruptedException
	 */
	public void run(int num_boids) throws InterruptedException {
		Random rand = new Random();
		for (int i = 0; i < num_boids; i++) {
			boids.add(new Boid(rand.nextInt(this.getWidth()), rand.nextInt(this.getWidth()), SIZE));
		}

		while (true) {
			getVectors();

			for (Boid b : boids) {
				b.x += b.movementVector[0];
				b.y += b.movementVector[1];
				
				if(b.x < 1)
					b.x = 1;
				else if(b.x > this.getWidth()-1)
					b.x = this.getWidth()-1;
				if(b.y < 1)
					b.y = 1;
				else if(b.y > this.getHeight()-1)
					b.y = this.getHeight()-1;
				
			}

			System.out.println(boids.get(0).x + " " + boids.get(0).y);
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
}
