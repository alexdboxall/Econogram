import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;

public class Point extends DrawObject {
	
	Label label;
	PointLine hzLine;
	PointLine vtLine;

	@Override
	public String getSerialisation() {
		return String.format("?");
	}
	
	public Point(Coordinate relativePos) {
		super(relativePos);
		
		label = new Label(new Coordinate(0, 0), "New point");
		addChild(label);
		
		alignDragWithPointLineDots = true;
		
		double newX = ((int)(relativePosition.x / 6)) * 6;
		double newY = ((int)(relativePosition.y / 6)) * 6;
		relCutoffX = 0;
		relCutoffY = 0;
		relativePosition.x = newX;
		relativePosition.y = newY;
		
		relativePosition.x = 250;
		relativePosition.y = 250;
	}
	
	@Override
	public RightClickMenu getRightClickMenu(Econogram e, DrawObject o) {
		return null;
	}
	
	public List<PropertyEntry> getPropertiesPanelLayout() {
		ArrayList<PropertyEntry> properties = new ArrayList<PropertyEntry>();
		
		return properties;
	}

	public void updateProperty(PropertyEntry property) {
		
	}

	@Override
	public void addDrawPrimativesPreChild(Coordinate base, List<DrawPrimative> primatives) {
		label.setCanvasParent(getCanvasParent());
		
		if (parent != null && hzLine == null) {
			hzLine = new PointLine(new Coordinate(0, 0), false);
			vtLine = new PointLine(new Coordinate(0, 0), true);
			addChild(hzLine);
			addChild(vtLine);
		}
		if (hzLine != null) {
			hzLine.setCanvasParent(getCanvasParent());
		}
	}

	@Override
	public void addDrawPrimativesPostChild(Coordinate base, List<DrawPrimative> primatives) {
		List<Coordinate> coordList = new ArrayList<Coordinate>();
		coordList.add(new Coordinate(base, new Coordinate(-5.0, -5.0)));
		coordList.add(new Coordinate(base, new Coordinate(5.0, -5.0)));
		coordList.add(new Coordinate(base, new Coordinate(5.0, 5.0)));
		coordList.add(new Coordinate(base, new Coordinate(-5.0, 5.0)));

		primatives.add(new PrimativePolygon(this, coordList));
	}

	@Override
	public String getName() {
		return "Point";
	}
}
