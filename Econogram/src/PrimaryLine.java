import java.util.List;

public abstract class PrimaryLine extends DrawObject {

	double padding = 30.0;
	
	public PrimaryLine(Coordinate coord) {
		super(coord);
		
		canDrag = false;
	}
	
	public abstract List<IntersectableLine> getLineBreakdown();
	
	@Override
	public RightClickMenu getRightClickMenu(Econogram e, DrawObject o) {
		return new PrimaryLineRightClickMenu(e, o);
	}

	@Override
	public void addDrawPrimativesPreChild(Coordinate base, List<DrawPrimative> primatives) {
		
	}
	
	public Coordinate intersection(PrimaryLine other) {
		List<IntersectableLine> ourLines = getLineBreakdown();
		List<IntersectableLine> theirLines = other.getLineBreakdown();
		
		for (IntersectableLine ourLine : ourLines) {
			for (IntersectableLine theirLine : theirLines) {
				
				double ax1 = ourLine.p1.x + relativePosition.x;
				double ax2 = ourLine.p2.x + relativePosition.x;
				double ay1 = ourLine.p1.y + relativePosition.y;
				double ay2 = ourLine.p2.y + relativePosition.y;
			
				double bx1 = theirLine.p1.x + other.relativePosition.x;
				double bx2 = theirLine.p2.x + other.relativePosition.x;
				double by1 = theirLine.p1.y + other.relativePosition.y;
				double by2 = theirLine.p2.y + other.relativePosition.y;

				IntersectableLine ourNew = new IntersectableLine(new Coordinate(ax1, ay1), new Coordinate(ax2, ay2));
				IntersectableLine theirNew = new IntersectableLine(new Coordinate(bx1, by1), new Coordinate(bx2, by2));
				
				Coordinate coord = ourNew.intersects(theirNew);
				if (coord != null) {
					return coord;
				}
			}
		}

		return null;
	}
	
	public List<PrimaryLine> getOverlappingPrimaryLines() {
		List<PrimaryLine> lines = ((Axis) parent).getAllPrimaryLines();
		lines.remove(this);
		
		//TODO : remove non-overlapping lines
		
		return lines;
	}
	
	@Override
	public void addDrawPrimativesPostChild(Coordinate base, List<DrawPrimative> primatives) {
		if (parent == null) {
			return;
		}
		
		Axis axis = (Axis) parent;
		
		List<IntersectableLine> lines = getLineBreakdown();
		
		for (IntersectableLine line : lines) {
			IntersectableLine normalisedLine = line;
			
			if (normalisedLine.p1.x + base.x - padding < parent.getAbsolutePosition().x) {
				continue;
			}
			if (normalisedLine.p2.x + base.x - padding  < parent.getAbsolutePosition().x) {
				continue;
			}
			if (normalisedLine.p1.y + base.y - padding < parent.getAbsolutePosition().y) {
				continue;
			}
			if (normalisedLine.p2.y + base.y - padding < parent.getAbsolutePosition().y) {
				continue;
			}
			if (normalisedLine.p1.x + base.x + padding > parent.getAbsolutePosition().x + axis.axisSize) {
				continue;
			}
			if (normalisedLine.p2.x + base.x + padding > parent.getAbsolutePosition().x + axis.axisSize) {
				continue;
			}
			if (normalisedLine.p1.y + base.y + padding > parent.getAbsolutePosition().y + axis.axisSize) {
				continue;
			}
			if (normalisedLine.p2.y + base.y + padding > parent.getAbsolutePosition().y + axis.axisSize) {
				continue;
			}
			
			Coordinate c1 = new Coordinate(base, normalisedLine.p1);
			Coordinate c2 = new Coordinate(base, normalisedLine.p2);
			primatives.add(new PrimativeLine(this, c1, c2));
		}
	}
}
