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
	
	int labelReloadUID = 0;
	
	public abstract String getDefaultLabelText();
	
	public abstract void copy();
	
	public String primaryLineDeserialisation(String data) {
		String parts[] = data.split("@")[0].split(",");
		
		labelReloadUID = Integer.parseInt(parts[0]);
		padding = Double.parseDouble(parts[1]);
		labelRelShiftX = Double.parseDouble(parts[2]);
		labelRelShiftY = Double.parseDouble(parts[3]);
		labelExpectedX = Double.parseDouble(parts[4]);
		labelExpectedY = Double.parseDouble(parts[5]);
		firstRightmostCalculationDoneYet = parts[6].charAt(0) == '1';
		labelExpectedY = Double.parseDouble(parts[7]);
		labelExpectedY = Double.parseDouble(parts[8]);

		return data.substring(data.split("@")[0].length() + 1);
	}
	
	public String primaryLineSerialisation() {
		return String.format("%d,%f,%f,%f,%f,%f,%d,%f,%f@", label.uniqueID, padding, labelRelShiftX, labelRelShiftY, labelExpectedX, labelExpectedY, firstRightmostCalculationDoneYet ? 1 : 0, rightmostX, rightmostY);
	}
	
	public PrimaryLine(String fullSerial, int uid, Canvas canvas, DrawObject parent_) {
		super(fullSerial, uid, canvas, parent_);
		deserialiseTree(fullSerial);
		
		label = (Label) findChildWithUID(labelReloadUID);
	}
	
	public PrimaryLine(Coordinate coord) {
		super(coord);
		
		canDrag = false;
		label = new Label(new Coordinate(0.0, 0.0), "");
		labelExpectedX = 0.0;
		labelExpectedY = 0.0;
		addChild(label);
	}
	
	public abstract List<IntersectableLine> getLineBreakdown();
	
	@Override
	public void mouseDragging(double deltaX, double deltaY) {
		
		super.mouseDragging(deltaX, deltaY);
		
		if (label != null) {
			double oldLabelRelShiftX = labelRelShiftX;
			double oldLabelRelShiftY = labelRelShiftY;
			double oldLabelRelPosX = label.relativePosition.x;
			double oldLabelRelPosY = label.relativePosition.y;
			double oldLabelExpectedX = labelExpectedX;
			double oldLabelExpectedY = labelExpectedY;
			
			labelRelShiftX += label.relativePosition.x - labelExpectedX;
			labelRelShiftY += label.relativePosition.y - labelExpectedY;

			label.relativePosition.x = rightmostX + labelRelShiftX;
			label.relativePosition.y = rightmostY + labelRelShiftY;
			
			labelExpectedX = label.relativePosition.x;
			labelExpectedY = label.relativePosition.y;
		
			if (label.relativePosition.x != oldLabelRelPosX || label.relativePosition.y != oldLabelRelPosY) {
				getCanvasParent().econogram.actionManager.add(new Action() {
					double oldRelShiftX = oldLabelRelShiftX;
					double oldRelShiftY = oldLabelRelShiftY;
					double oldRelPosX = oldLabelRelPosX;
					double oldRelPosY = oldLabelRelPosY;
					double oldExpectedX = oldLabelExpectedX;
					double oldExpectedY = oldLabelExpectedY;
					
					double newRelShiftX = labelRelShiftX;
					double newRelShiftY = labelRelShiftY;
					double newRelPosX = label.relativePosition.x;
					double newRelPosY = label.relativePosition.y;
					double newExpectedX = labelExpectedX;
					double newExpectedY = labelExpectedY;
					
					@Override
					boolean isFence() {
						return false;
					}
					
					@Override
					boolean execute() {
						return true;
					}
	
					@Override
					boolean undo() {
						labelRelShiftX = oldRelShiftX;
						labelRelShiftY = oldRelShiftY;
						label.relativePosition.x = oldRelPosX;
						label.relativePosition.y = oldRelPosY;
						labelExpectedX = oldExpectedX;
						labelExpectedY = oldExpectedY;
						return true;
					}
	
					@Override
					boolean redo() {
						labelRelShiftX = newRelShiftX;
						labelRelShiftY = newRelShiftY;
						label.relativePosition.x = newRelPosX;
						label.relativePosition.y = newRelPosY;
						labelExpectedX = newExpectedX;
						labelExpectedY = newExpectedY;
						return true;
					}
				});
			}
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
	
	public List<PrimaryLine> getOverlappingPrimaryLines(double mx, double my) {
		List<PrimaryLine> lines = new ArrayList<PrimaryLine>();
		
		for (DrawObject obj : parent.getCanvasParent().children) {
			try {
				lines.addAll(((Axis) parent).getAllPrimaryLines());
			} catch (Exception e) {
				
			}
		}
		
		//((Axis) parent).getAllPrimaryLines();
		
		lines.remove(this);
		
		mx -= parent.getAbsolutePosition().x;
		my -= parent.getAbsolutePosition().y;

		List<PrimaryLine> overlappingLines = new ArrayList<PrimaryLine>();
		for (PrimaryLine line : lines) {
			if (line.intersection(this) != null && this.intersection(line) != null) {
				if (line.intersection(this).x > 0 && line.intersection(this).y > 0) {
					
					double distX = Math.abs(line.intersection(this).x - mx) + 1;
					double distY = Math.abs(line.intersection(this).y - my) + 1;
										
					if (Math.sqrt(distX * distX + distY * distY) < 85) {
						overlappingLines.add(line);
					}
				}
			}
		}
		
		return overlappingLines;
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
			PrimativeLine pl = new PrimativeLine(this, c1, c2);
			pl.colour = 0x004080;
			primatives.add(pl);
			
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
			label.text = getDefaultLabelText();
		}
	}
}
