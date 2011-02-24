import java.awt.Color;
import java.awt.Graphics;


public class Boid {
	int x, y, size;
	int[] vec;
	Color color;
	
	public Boid(int _x, int _y, int _size){
		x = _x;
		y = _y;
		size = _size;
		vec = new int[2];
		vec[0] = 0;
		vec[1] = 1;
	}
	
	public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, size, size);
    }
}
