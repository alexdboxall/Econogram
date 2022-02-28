
public class IntersectableLine {
	Coordinate p1;
	Coordinate p2;
	
	IntersectableLine(Coordinate c1, Coordinate c2) {
		p1 = c1;
		p2 = c2;
	}
	
	boolean isVertical() {
		return Math.abs(p1.x - p2.x) < 0.02;
	}
	
	double getGradient() {
		return (p2.y - p1.y) / (p2.x - p1.x);
	}
	
	double getIntercept() {
		return p1.y - getGradient() * p1.x;
	}
	
	// https://www.geeksforgeeks.org/find-two-rectangles-overlap/
	boolean overlaps(Coordinate l1, Coordinate l2, Coordinate r1, Coordinate r2)
	{
	    if (l1.x == r1.x || l1.y == r1.y || l2.x == r2.x|| l2.y == r2.y) {
	        // the line cannot have positive overlap
	        //return false;
	    }
	 
	    if (l1.x > r2.x || l2.x > r1.x)
	        return false;
	 
	    if (r1.y > l2.y || r2.y > l1.y)
	        return false;
	 
	    return true;
	}
	
	Coordinate intersects(IntersectableLine o) {
		if (!overlaps(p1, p2, o.p1, o.p2)) {
			return null;
		}
		
		if (isVertical() && o.isVertical()) {
			//either doesn't intersect at all, or the entire line intersects,
			//and we don't need to support that (you would only see one line)
			return null;
		}
		
		if (o.isVertical()) {			
			//swap around and then do it
			return o.intersects(this);
		}
		
		if (isVertical()) {			
			System.out.printf("VERTICAL 3\n");

			return new Coordinate(p1.x, p1.x * o.getGradient() + o.getIntercept());
		}
		
		double ix = (o.getIntercept() - getIntercept()) / (getGradient() - o.getGradient());
		return new Coordinate(ix, ix * getGradient() + getIntercept());
	}
}
