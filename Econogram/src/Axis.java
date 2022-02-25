import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;

public class Axis extends DrawObject {
	
	int axisSize;
	
	Label vtAxisLabel;
	Label hzAxisLabel;
	Arrowhead hzArrowhead;
	Arrowhead vtArrowhead;
	
	public Axis(Coordinate relativePosition) {
		super(relativePosition);
		axisSize = 500;
		
		hzArrowhead = new Arrowhead(new Coordinate(axisSize, axisSize), Arrowhead.BuiltinStyle.Normal, Arrowhead.ANGLE_EAST);
		vtArrowhead = new Arrowhead(new Coordinate(0, 0), Arrowhead.BuiltinStyle.Normal, Arrowhead.ANGLE_NORTH);

		vtAxisLabel = new Label(new Coordinate(-70, 0), "Price\n($)");
		hzAxisLabel = new Label(new Coordinate(axisSize - 15, axisSize + 32), "Qty.");
		vtAxisLabel.setFontSize(18.0);
		hzAxisLabel.setFontSize(18.0);

		addChild(hzArrowhead);
		addChild(vtArrowhead);
		addChild(vtAxisLabel);
		addChild(hzAxisLabel);
	}
	
	public void updateProperty(PropertyEntry property) {
		if (property.id.equals("size")) {
			try {
				int newSize = Integer.parseInt(((PropertyEntryTextBox) property).dataText);
				int difference = newSize - axisSize;
				axisSize = newSize;
				
				hzAxisLabel.relativePosition.x += difference;
				hzAxisLabel.relativePosition.y += difference;
				hzArrowhead.relativePosition.x += difference;
				hzArrowhead.relativePosition.y += difference;
				
				update();
			} catch (Exception e) {
				
			}
		}
	}

	@Override
	public void addDrawPrimativesPreChild(Coordinate base, List<DrawPrimative> primatives) {
		//canvas.addObject(thisAxis) happens AFTER the constructor is called, so the children never get a canvasParent
		vtAxisLabel.setCanvasParent(canvasParent);
		hzAxisLabel.setCanvasParent(canvasParent);
	}

	@Override
	public void addDrawPrimativesPostChild(Coordinate base, List<DrawPrimative> primatives) {
		Coordinate topLeft = new Coordinate(base.x, base.y);
		Coordinate bottomLeft = new Coordinate(base.x, base.y + axisSize);
		Coordinate bottomRight = new Coordinate(base.x + axisSize, base.y + axisSize);
		
		primatives.add(new PrimativeLine(this, topLeft, bottomLeft));
		primatives.add(new PrimativeLine(this, bottomLeft, bottomRight));
		
		Coordinate zeroPosition = new Coordinate(base.x - 18, base.y + axisSize + 18);
		primatives.add(new PrimativeText(this, "0", zeroPosition));
	}
	
	public List<PropertyEntry> getPropertiesPanelLayout() {
		ArrayList<PropertyEntry> properties = new ArrayList<PropertyEntry>();
		properties.add(new PropertyEntryTextBox("size", "Size:", String.format("%d", axisSize)));

		return properties;
	}

	@Override
	public String getName() {
		return "Axis";
	}

	@Override
	public String getSerialisation() {
		return String.format("%d", axisSize);
	}

	@Override
	public RightClickMenu getRightClickMenu(Econogram e, DrawObject c) {
		return new AxisRightClickMenu(e, c);
	}
}
