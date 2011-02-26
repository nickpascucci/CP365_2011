/**
 * GA.java:
 * A simple program to use a genetic algorithm to analyze
 * and try to reproduce images. It works ok, but could use 
 * some tweaks. Contains multiple classes.
 */

package genetic;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * Provides a representation of polygons storing both shape and color.
 * Quite a bit of this code was written by our instructor, Matthew
 * Whitehead. We adapted it to solve the problem at hand and implemented
 * some missing methods.
 * 
 * @author Matthew Whitehead
 * @author Nick Pascucci
 * @author Robert Florance
 * 
 */
// Each MyPolygon has a color and a Polygon object
class MyPolygon {

	Polygon polygon;
	Color color;

	public MyPolygon(Polygon _p, Color _c) {
		polygon = _p;
		color = _c;
	}

	public void setColor(Color c) {
		this.color = c;
	}

	public Color getColor() {
		return color;
	}

	public Polygon getPolygon() {
		return polygon;
	}

	public String toString() {
		String output = "Color: " + color + "Vertices: ";
		String verts = "";
		for (int i = 0; i < polygon.npoints; i++) {
			verts = verts + "(" + polygon.xpoints[i] + "," + polygon.ypoints[i]
					+ ") ";
		}
		output = output + verts;
		return output;
	}
}

/**
 * Stores a generated solution to the problem, 
 * also known as a chromosome.
 */
class GASolution {

	ArrayList<MyPolygon> shapes;

	// width and height are for the full resulting image
	int width, height;

	public float fitness;

	public GASolution(int _width, int _height) {
		shapes = new ArrayList<MyPolygon>();
		width = _width;
		height = _height;
	}

	public void addPolygon(MyPolygon p) {
		shapes.add(p);
	}

	public ArrayList<MyPolygon> getShapes() {
		return shapes;
	}

	public int size() {
		return shapes.size();
	}

	// Create a BufferedImage of this solution
	// Use this to compare an evolved solution with
	// a BufferedImage of the target image
	//
	// This is almost surely NOT the fastest way to do this...
	public BufferedImage getImage() {
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		for (MyPolygon p : shapes) {
			Graphics g2 = image.getGraphics();
			g2.setColor(p.getColor());
			Polygon poly = p.getPolygon();
			if (poly.npoints > 0) {
				g2.fillPolygon(poly);
			}
		}
		return image;
	}

	public String toString() {
		return "" + shapes;
	}
}

/**
 * Provides drawing capabilities to display solutions.
 */
class GACanvas extends JComponent {

	int width, height;
	GASolution solution;

	public GACanvas(int WINDOW_WIDTH, int WINDOW_HEIGHT) {
		width = WINDOW_WIDTH;
		height = WINDOW_HEIGHT;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setImage(GASolution sol) {
		solution = sol;
	}

	public void paintComponent(Graphics g) {
		if (solution == null)
			return;
		BufferedImage image = solution.getImage();
		g.drawImage(image, 0, 0, null);
	}
}

/**
 * Implements the actual genetic algorithm.
 */
public class GA extends JComponent {

	GACanvas canvas;
	int width, height;
	BufferedImage realPicture;
	ArrayList<GASolution> population;
	Random rand = new Random();

	// Adjust these parameters as necessary for your simulation
	double MUTATION_RATE = 0.01;
	double CROSSOVER_RATE = 0.4;
	int MAX_POLYGON_POINTS = 4;
	int MAX_POLYGONS = 10;
	int MAX_SAMPLES = 1000;
	int COLOR_MOBILITY = 40; // Be sure only to use even numbers here
	int VERTEX_MOBILITY = 40; // Otherwise, the polys/colors will shift to one side
	int ORGANISMS = 100;

	/*
	 * Some notes on parameter values: Increasing VERTEX_MOBILITY will make
	 * mutations have larger movements. This means that the solution might find
	 * a good medium faster, but won't stay there.
	 * 
	 * Increasing COLOR_MOBILITY has a similar effect Increasing MAX_SAMPLES
	 * improves the granularity of the fitness function.
	 */

	/**
	 * Attaches a new genetic algorithm instance to a canvas and target
	 * image.
	 */
	public GA(GACanvas _canvas, BufferedImage _realPicture) {

		canvas = _canvas;
		realPicture = _realPicture;
		width = realPicture.getWidth();
		height = realPicture.getHeight();
		population = new ArrayList<GASolution>();

		createPopulation(ORGANISMS);
	}

	/**
	 * Performs the grunt work in running the simulation.
	 * @param gens
	 */
	private void evolve(int gens) {
		for (int generation = 0; generation < gens; generation++) {
			// Evaluate fitness
			// Start off with our population from above -> fitness -> store in
			// GASolution
			for (GASolution s : population) {
				evaluateFitness(s);
			}
			// Get it on with the fittest polygons
			ArrayList<GASolution> newPop = new ArrayList<GASolution>();
			for (int i = 0; i < (population.size() * CROSSOVER_RATE); i++) {
				GASolution dad = selectPartner(population);
				GASolution mom = selectPartner(population);
				GASolution kid = mate(mom, dad); // Naughty
				newPop.add(kid);
			}
			for (int i = 0; i < (population.size() * (1 - CROSSOVER_RATE)); i++) {
				newPop.add(selectPartner(population));
			}
			// Mutate
			for (GASolution s : newPop) {
				mutate(s);
			}
			population = newPop;
			if (generation % 10 == 0) {
				canvas.setImage(mostFit);
				canvas.repaint();
				double percentFitness = ((double) mostFit.fitness)
						/ ((double) MAX_SAMPLES * 3 * 255) * 100;
				System.out.println("Generation " + generation + " Fitness "
						+ percentFitness + "%");
			}
			mostFit = null;
		}
	}

	/**
	 * Picks a weighted random Chromosome from the population.
	 */
	private GASolution selectPartner(ArrayList<GASolution> pop) {
		// Sum the fitnesses, create a random number between 0 and that sum
		int totalFitness = 0;
		for (GASolution s : pop) {
			totalFitness += s.fitness;
		}
		int avgFitness = totalFitness / pop.size();
		// Go through our population, keeping a running sum of fitnesses and see
		// which one tips it over the rand
		int target = 0;
		while (target < avgFitness)
			target = rand.nextInt(totalFitness); // Only select organisms better than avg

		int i = 0, runningSum = 0;
		while (runningSum < target) { 
			runningSum += pop.get(i++).fitness;
		}
		return pop.get(i - 1);
	}

	/**
	 * Evaluates a given solution and sets its internal fitness value
	 */
	private GASolution mostFit;

	private void evaluateFitness(GASolution g) {
		BufferedImage solutionImage = g.getImage();
		g.fitness = 0;
		for (int i = 0; i < MAX_SAMPLES; i++) {
			int x = rand.nextInt(width);
			int y = rand.nextInt(height);
			int color = solutionImage.getRGB(x, y);
			int realColor = realPicture.getRGB(x, y);
			Color gColor = new Color(color);
			Color rColor = new Color(realColor);
			/*
			 * Here we set the fitness value to be the maximum distance possible
			 * minus the actual distance; so the amount of distance left over.
			 * This gives us positive fitness values.
			 */
			g.fitness += (255 - Math.abs(gColor.getGreen() - rColor.getGreen()));
			g.fitness += (255 - Math.abs(gColor.getBlue() - rColor.getBlue()));
			g.fitness += (255 - Math.abs(gColor.getRed() - rColor.getRed()));
		}

		if (mostFit == null || g.fitness > mostFit.fitness)
			mostFit = g;
	}

	/**
	 * Mutates a GASolution in place.
	 */
	private void mutate(GASolution mutant) {

		ArrayList<MyPolygon> polys = mutant.getShapes();
		for (MyPolygon p : polys) {
			Polygon shape = p.getPolygon();
			for (int i = 0; i < shape.npoints; i++) {
				double mutationScore = rand.nextDouble();
				// Check to see if we're mutating this point's coordinates
				if (mutationScore < MUTATION_RATE) {
					shape.xpoints[i] = shape.xpoints[i]
							+ rand.nextInt(VERTEX_MOBILITY + 1)
							- (VERTEX_MOBILITY / 2);
					shape.ypoints[i] = shape.ypoints[i]
							+ rand.nextInt(VERTEX_MOBILITY + 1)
							- (VERTEX_MOBILITY / 2);
				}
			}
			double mutationScore = rand.nextDouble();
			// Check to see if we're mutating this poly's color
			if (mutationScore < MUTATION_RATE) {
				int r = p.getColor().getRed()
						+ rand.nextInt(COLOR_MOBILITY + 1)
						- (COLOR_MOBILITY / 2);
				int g = p.getColor().getGreen()
						+ rand.nextInt(COLOR_MOBILITY + 1)
						- (COLOR_MOBILITY / 2);
				int b = p.getColor().getBlue()
						+ rand.nextInt(COLOR_MOBILITY + 1)
						- (COLOR_MOBILITY / 2);
				if (r > 255)
					r = 255;
				if (g > 255)
					g = 255;
				if (b > 255)
					b = 255;
				if (r < 0)
					r = 0;
				if (g < 0)
					g = 0;
				if (b < 0)
					b = 0;
				p.setColor(new Color(r, g, b));
			}
		}
	}

	/**
	 * Combines two GASolutions into a child, using a random crossover.
	 */
	private GASolution mate(GASolution father, GASolution mother) {
		GASolution child = new GASolution(width, height);
		ArrayList<MyPolygon> fatherShapes = father.getShapes();
		ArrayList<MyPolygon> motherShapes = mother.getShapes();
		// Get some from dad
		for (int i = 0; i < fatherShapes.size(); i++) {
			Polygon newPoly = new Polygon();
			Polygon momPoly = motherShapes.get(i).getPolygon();
			Polygon dadPoly = fatherShapes.get(i).getPolygon();
			// We're going to go through and merge the points of each polygon
			// by taking a random divisor somewhere between 0 and npoints
			int divisor = rand.nextInt(momPoly.npoints);
			for (int f = 0; f < divisor; f++) {
				newPoly.addPoint(dadPoly.xpoints[f], dadPoly.ypoints[f]);
			}
			for (int m = divisor; m < momPoly.npoints; m++) {
				newPoly.addPoint(momPoly.xpoints[m], momPoly.ypoints[m]);
			}
			// Picking the color for the new MyPolygon is done by picking
			// whoever
			// got the least vertices.
			Color newColor = (divisor > (dadPoly.npoints) ? fatherShapes.get(i)
					.getColor() : motherShapes.get(i).getColor());
			MyPolygon childPoly = new MyPolygon(newPoly, newColor);
			child.addPolygon(childPoly);
		}
		return child;
	}

	/**
	 * Creates a random population of GASolutions.
	 */
	private void createPopulation(int members) {
		// Create 'members' random chromosomes, and add them to the pop
		for (int i = 0; i < members; i++) {
			population.add(createRandomChromosome());
		}
	}

	/**
	 * Generates a random chromosome.
	 * @return
	 */
	private GASolution createRandomChromosome() {
		// Create random polygons
		GASolution solution = new GASolution(width, height);
		for (int p = 0; p < MAX_POLYGONS; p++) {
			Polygon tmp = new Polygon();
			for (int i = 0; i < MAX_POLYGON_POINTS; i++) {
				int x = rand.nextInt(width);
				int y = rand.nextInt(height);
				tmp.addPoint(x, y);
			}
			// Match with random colors
			Color tmpColor = new Color(rand.nextInt(255), rand.nextInt(255),
					rand.nextInt(255));
			// Create MyPolygon objects with those polys and colors
			MyPolygon poly = new MyPolygon(tmp, tmpColor);
			// Add them to a GASolution object
			solution.addPolygon(poly);
		}
		return solution;
		// Return that
	}

	/**
	 * Runs the simulation.
	 */
	public void runSimulation() {
		evolve(1000000); // Run for a million epochs
	}

	/**
	 * Main method.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		String realPictureFilename = "test.jpg";

		BufferedImage realPicture = ImageIO.read(new File(realPictureFilename));

		JFrame frame = new JFrame();
		frame.setSize(realPicture.getWidth(), realPicture.getHeight());
		frame.setTitle("GA Simulation of Art");

		GACanvas theCanvas = new GACanvas(realPicture.getWidth(),
				realPicture.getHeight());
		frame.add(theCanvas);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		GA pt = new GA(theCanvas, realPicture);
		pt.runSimulation();
	}
}
