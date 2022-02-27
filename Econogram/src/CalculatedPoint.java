import java.util.List;

public class CalculatedPoint extends Point {
	PrimaryLine line1;
	PrimaryLine line2;
	
	public CalculatedPoint(PrimaryLine l1, PrimaryLine l2) {
		super(new Coordinate(0.0, 0.0));
		
		canDrag = false;
		
		line1 = l1;
		line2 = l2;

		recalculateLocation();
	}
	
	void recalculateLocation() {
		if (line1 == null || line2 == null) {
			return;
		}
		
		Coordinate intersection = line1.intersection(line2);
		
		if (intersection != null) {
			relativePosition.x = intersection.x;
			relativePosition.y = intersection.y;
		}
	}
	
	@Override
	public void addDrawPrimativesPreChild(Coordinate base, List<DrawPrimative> primatives) {
		recalculateLocation();
		super.addDrawPrimativesPreChild(base, primatives);
	}
	
	
}
