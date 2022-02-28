import java.util.ArrayList;
import java.util.List;

public abstract class PrimaryLine extends DrawObject {

	double padding = 30.0;
	Label label;
	double rightmostX = 0;
	double rightmostY = 0;
	
	boolean firstRightmostCalculationDoneYet = false;
	
	double labelRelShiftX = 0;
	double labelRelShiftY = 0;

	double labelExpectedX = 0;
	double labelExpectedY = 0;
	
	public PrimaryLine(Coordinate coord) {
		super(coord);
		
		canDrag = false;
		label = new Label(new Coordinate(0.0, 0.0), "L");
		labelExpectedX = 0.0;
		labelExpectedY = 0.0;
		addChild(label);
	}
	
	public abstract List<IntersectableLine> getLineBreakdown();
	
	@Override
	public void mouseDragging(double deltaX, double deltaY) {
		
		super.mouseDragging(deltaX, deltaY);
		
		if (label != null) {
			labelRelShiftX += label.relativePosition.x - labelExpectedX;
			labelRelShiftY += label.relativePosition.y - labelExpectedY;

			label.relativePosition.x = rightmostX + labelRelShiftX;
			label.relativePosition.y = rightmostY + labelRelShiftY;
			
			labelExpectedX = label.relativePosition.x;
			labelExpectedY = label.relativePosition.y;
		}
	}
	
	@Override
	public RightClickMenu getRightClickMenu(Econogram e, DrawObject o) {
		return new PrimaryLineRightClickMenu(e, o);
	}

	@Override
	public void addDrawPrimativesPreChild(Coordinate base, List<DrawPrimative> primatives) {
		if (label != null && label.canvasParent == null) {
			label.setCanvasParent(getCanvasParent());
			label.update();			//force calculation of the label's position
		}
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
		
		rightmostX = 0;
		
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
			
			if (c1.x > rightmostX) {
				rightmostX = normalisedLine.p1.x;
				rightmostY = normalisedLine.p1.y;
			}
			if (c2.x > rightmostX) {
				rightmostX = normalisedLine.p2.x;
				rightmostY = normalisedLine.p2.y;
			}
		}

		if (!firstRightmostCalculationDoneYet) {
			labelExpectedX = rightmostX + 10;
			labelExpectedY = rightmostY - 10;
			labelRelShiftX = 10;
			labelRelShiftY = -10;
			label.relativePosition.x = labelExpectedX;
			label.relativePosition.y = labelExpectedY;
			firstRightmostCalculationDoneYet = true;
		}
	}
}
