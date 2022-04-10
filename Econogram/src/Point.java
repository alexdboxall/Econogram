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
	boolean circular = false;
	
	int labelReloadUID;
	int hzLineReloadUID;
	int vtLineReloadUID;
	
	// IF YOU UPDATE THE SERIAL/DESERIAL HERE, YOU NEED TO UPDATE IT IN CalculatedPoint TOO
	@Override
	public void reloadOnDeserialisation(String data) {
		showDot = data.charAt(0) == 'T';
		vtShowing = data.charAt(1) == 'T';
		hzShowing = data.charAt(2) == 'T';
		circular = data.charAt(3) == 'T';
		
		System.out.printf("Pnt: %s\n", data.substring(4));
		
		String parts[] = data.substring(4).split(",");
		labelReloadUID = Integer.parseInt(parts[0]);
		hzLineReloadUID = Integer.parseInt(parts[1]);
		vtLineReloadUID = Integer.parseInt(parts[2]);
	}
	
	@Override
	public String getSerialisation() {
		return String.format("%c%c%c%c%d,%d,%d", 
				showDot ? 'T' : 'F', vtShowing ? 'T' : 'F', hzShowing ? 'T' : 'F', circular ? 'T' : 'F', 
				label.getUniqueID(), hzLine.getUniqueID(), vtLine.getUniqueID());
	}
	
	public Point(String fullSerial, int uid, Canvas canvas, DrawObject parent_) {
		super(fullSerial, uid, canvas, parent_);
		deserialiseTree(fullSerial);
		
		label = (Label) findChildWithUID(labelReloadUID);
		hzLine = (PointLine) findChildWithUID(hzLineReloadUID);
		vtLine = (PointLine) findChildWithUID(vtLineReloadUID);
	}
	
	@Override
	public String objectType3DigitID() {
		return "PNT";
	}
	
	public Point(Coordinate relativePos) {
		super(relativePos);
		
		label = new Label(new Coordinate(16, 4), "New point");
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
		properties.add(new PropertyEntryCheckbox("circular", "Circular:", circular));

		return properties;
	}

	public void updateProperty(PropertyEntry property) {
		try {
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
		} catch (Exception e) {
			
		}
		if (property.id.equals("dot")) {
			showDot = ((PropertyEntryCheckbox) property).selected;
			update();
		}
		if (property.id.equals("circular")) {
			circular = ((PropertyEntryCheckbox) property).selected;
			update();
		}
	}

	@Override
	public void addDrawPrimativesPreChild(Coordinate base, List<DrawPrimative> primatives) {
		if (label != null) label.setCanvasParent(getCanvasParent());
		
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
		if (!showDot) return;
		
		if (!circular) {
			List<Coordinate> coordList = new ArrayList<Coordinate>();
			coordList.add(new Coordinate(base, new Coordinate(-5.0, -5.0)));
			coordList.add(new Coordinate(base, new Coordinate(5.0, -5.0)));
			coordList.add(new Coordinate(base, new Coordinate(5.0, 5.0)));
			coordList.add(new Coordinate(base, new Coordinate(-5.0, 5.0)));
			primatives.add(new PrimativePolygon(this, coordList));

		} else {
			primatives.add(new PrimativeEllipse(this, new Coordinate(base, new Coordinate(-5.0, -5.0)), 10.0, 10.0));
		}
	}

	@Override
	public String getName() {
		return "Point";
	}
}
