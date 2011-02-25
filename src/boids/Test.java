package boids;

import javax.swing.JFrame;

/**
 * Provides a driver class for the boids/flocking simulation.
 * @author Matthew Polk
 * @author Nick Pascucci
 */

public class Test {
	public static void main(String[] args) throws InterruptedException{
		System.out.println("start");
		BoidCanvas can = new BoidCanvas();
		JFrame frame = new JFrame();
		frame.setSize(500, 600);
		frame.add(can);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		can.run(75);
	}
}
