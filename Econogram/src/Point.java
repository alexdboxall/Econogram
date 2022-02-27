import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;

public class Point extends DrawObject {
	
	Label label;
	PointLine hzLine;
	PointLine vtLine;
	boolean hzShowing = true;
	boolean vtShowing = true;
	boolean showDot = true;

	@Override
	public String getSerialisation() {
		return String.format("%c%c%c", showDot ? 'T' : 'F', vtShowing ? 'T' : 'F', hzShowing ? 'T' : 'F');
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
	}
	
	@Override
	public RightClickMenu getRightClickMenu(Econogram e, DrawObject o) {
		return new PointRightClickMenu(e, o);
	}
	
	public List<PropertyEntry> getPropertiesPanelLayout() {
		ArrayList<PropertyEntry> properties = new ArrayList<PropertyEntry>();
		properties.add(new PropertyEntryCheckbox("dot", "Show dot:", showDot));
		properties.add(new PropertyEntryCheckbox("hzs", "Show horizontal line:", hzShowing));
		properties.add(new PropertyEntryCheckbox("vts", "Show vertical line:", vtShowing));

		return properties;
	}

	public void updateProperty(PropertyEntry property) {
		if (property.id.equals("hzs")) {
			boolean old = hzShowing;
			hzShowing = ((PropertyEntryCheckbox) property).selected;
			if (!hzShowing && old) {
				hzLine.delete();
			} else if (hzShowing && !old) {
				addChild(hzLine);
			}
			update();
		}
		if (property.id.equals("vts")) {
			boolean old = vtShowing;
			vtShowing = ((PropertyEntryCheckbox) property).selected;
			if (!vtShowing && old) {
				vtLine.delete();
			} else if (vtShowing && !old) {
				addChild(vtLine);
			}
			update();
		}
		if (property.id.equals("dot")) {
			showDot = ((PropertyEntryCheckbox) property).selected;
			update();
		}
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

		if (showDot) {
			primatives.add(new PrimativePolygon(this, coordList));
		}
	}

	@Override
	public String getName() {
		return "Point";
	}
}
