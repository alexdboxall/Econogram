
public class Coordinate {
	double x;
	double y;
	
	public Coordinate(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Coordinate() {
		x = 0;
		y = 0;
	}
	
	public Coordinate(Coordinate old, Coordinate offset) {
		x = offset.x + old.x;
		y = offset.y + old.y;
	}
}
