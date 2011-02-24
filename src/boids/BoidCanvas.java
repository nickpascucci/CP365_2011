import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JComponent;
/**
 * Provides a drawing surface for the boid flock.
 * @author Matthew Polk
 * @author Nick Pascucci
 */

class BoidCanvas extends JComponent{
	final static int MAX_SPEED = 10;
	final static int NEIGHBOR_DISTANCE = 100;
	int SIZE = 10;
	int WIDTH = 500;
	int HEIGHT = 400;
    ArrayList<Boid> boids;
    
    
    /**
     * Creates a new BoidCanvas object with defaults.
     */
    public BoidCanvas(){
    	boids = new ArrayList<Boid>();
    }
    
    /**
     * Searches the boids ArrayList for nearby boids to this one.
     */
    private ArrayList<Boid> get_nearby_boids(Boid b){
    	ArrayList<Boid> neighbors = new ArrayList<Boid>();
    	
    	for(Boid d: boids){
    		if(d.x - b.x + (d.y-b.y) < NEIGHBOR_DISTANCE)
    			neighbors.add(d);
    	}
    	
    	return neighbors;
    }
    
    /**
     * 
     */
    public void getVectors(){
    	ArrayList<Boid> newBoids = new ArrayList<Boid>();
    	for(Boid b:boids){
    		newBoids.add(b);
    	}
    	
    	for(Boid b: newBoids){
    		int[] cv = getCenterVec(b);
    		int[] av = getAwayVec(b);
    		int[] ms = getMatchSpeedVec(b);
    		
    		b.vec[0] += cv[0] + av[0] + ms[0];
    		b.vec[1] += cv[1] + av[1] + ms[1];
    		
    		if(b.vec[0] > MAX_SPEED)
    			b.vec[0] = MAX_SPEED;
    		if(b.vec[1] > MAX_SPEED)
    			b.vec[1] = MAX_SPEED;
    	}
    	boids = newBoids;
    }
    
    public int[] getCenterVec(Boid boid){
    	int[] center = new int[2];
    	center[0] = 0;
    	center[1] = 0;
    	 for(Boid b: boids){
    		 center[0] += b.x;
    		 center[1] += b.y;
    	 }
    	center[0] /= boids.size();
    	center[1] /= boids.size();
    	System.out.println("center at "+center[0]+" "+center[1]); 
    	int[] centerVec = new int[2];
    	
    	centerVec[0] = center[0]-boid.x;
    	centerVec[1] = center[1]-boid.y;
    	
    	return centerVec;
    }
    
    public int[] getAwayVec(Boid b){
    	int[] awayVec = new int[2];
    	
    	ArrayList<Boid> Neighborhood = get_nearby_boids(b);
    	int Neighborhood_x = 0;
    	int Neighborhood_y = 0;
    	for(Boid d: Neighborhood){
    		Neighborhood_x -= (d.x - b.x);
    	    Neighborhood_y -= (d.y - b.y);
    	}

		Neighborhood_x /= Neighborhood.size();
		Neighborhood_y /= Neighborhood.size();    	
    	
    	awayVec[0] = Neighborhood_x;
    	awayVec[1] = Neighborhood_y;
    	
    	return awayVec;
    }
    
    public int[] getMatchSpeedVec(Boid b){
    	int[] matchVec = new int[2];
    	ArrayList<Boid> Neighborhood = get_nearby_boids(b);
    	
    	int neighborhoodDX = 0;
    	int neighborhoodDY = 0;
    	for(Boid d: Neighborhood){
    		neighborhoodDX += d.vec[0];
    		neighborhoodDY += d.vec[1];
    	}

		neighborhoodDX /= Neighborhood.size();
		neighborhoodDY /= Neighborhood.size();
		
		matchVec[0] = neighborhoodDX - b.vec[0];
		matchVec[1] = neighborhoodDY - b.vec[1];
    	
    	return matchVec;
    }
    
    public void run(int num_boids) throws InterruptedException{
    	Random rand = new Random();
    	for(int i = 0; i< num_boids; i++){
    		boids.add(new Boid(rand.nextInt(WIDTH), rand.nextInt(HEIGHT), SIZE));
    	}
    	
    	while(true){
    		getVectors();
    		
    		for(Boid b: boids){
    			b.x += b.vec[0];
    			b.y += b.vec[1];
    		}
    		
    		System.out.println(boids.get(0).x+" "+boids.get(0).y);
    		repaint();
    		Thread.sleep(100);
    	}
    }
    
    public void paintComponent(Graphics g) {
	for (Boid b : boids) {
            b.draw(g);
        }
    }
}