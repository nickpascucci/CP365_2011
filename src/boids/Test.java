import javax.swing.JFrame;


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
