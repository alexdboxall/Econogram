import java.util.ArrayList;
import java.util.List;

public class Arrow extends DrawObject {
	Arrowhead arrowhead;
	Arrowhead backArrowhead;
	
	double length;
	double angle;
	boolean showFrontArrow;
	boolean showBackArrow;
	
	int arrowheadReloadUID;
	int arrowhead2ReloadUID;

	Arrow(Coordinate relativePos, double len, double angle) {
		super(relativePos);
				
		this.length = len;
		this.angle = angle;
		
		arrowhead = new Arrowhead(new Coordinate(len * Math.cos(angle), len * Math.sin(angle)), Arrowhead.BuiltinStyle.Normal, 0.0);
		backArrowhead = new Arrowhead(new Coordinate(0.0, 0.0), Arrowhead.BuiltinStyle.Outline, 0.0);
		
		showFrontArrow = true;
		showBackArrow = false;
		
		addChild(arrowhead);
		addChild(backArrowhead);
		updateArrowheads();
	}
	
	void updateArrowheads() {
		arrowhead.relativePosition.x = length * Math.cos(angle);
		arrowhead.relativePosition.y = length * Math.sin(angle);
		arrowhead.setAngle(-angle);
		backArrowhead.setAngle(-angle + Math.PI);
	}

	@Override
	public String objectType3DigitID() {
		return "ARR";
	}

	public Arrow(String fullSerial, int uid, Canvas canvas, DrawObject parent_) {
		super(fullSerial, uid, canvas, parent_);
		deserialiseTree(fullSerial);
	
		arrowhead = (Arrowhead) findChildWithUID(arrowheadReloadUID);
		backArrowhead = (Arrowhead) findChildWithUID(arrowhead2ReloadUID);
	}

	@Override
	public RightClickMenu getRightClickMenu(Econogram e, DrawObject o) {
		return new ArrowRightClickMenu(e, o);
	}
	

	@Override
	public String getSerialisation() {
		return String.format("%f,%f,%c,%c,%d,%d", length, angle, showFrontArrow ? 'T' : 'F', showBackArrow ? 'T' : 'F', arrowhead.getUniqueID(), backArrowhead.getUniqueID());
	}
	
	@Override
	public void reloadOnDeserialisation(String data) {
		String parts[] = data.split(",");
		
		length = Double.parseDouble(parts[0]);
		angle = Double.parseDouble(parts[1]);
		showFrontArrow = parts[2].charAt(0) == 'T';
		showBackArrow = parts[3].charAt(0) == 'T';
		arrowheadReloadUID = Integer.parseInt(parts[4]);
		arrowhead2ReloadUID = Integer.parseInt(parts[5]);
	}
	
	static public double roundToNearest(double val, double incr) {
		return incr * Math.round(val / incr);
	}

	@Override
	public void addDrawPrimativesPreChild(Coordinate base, List<DrawPrimative> primatives) {
		arrowhead.setCanvasParent(getCanvasParent());
		arrowhead.hide = !showFrontArrow;
		backArrowhead.setCanvasParent(getCanvasParent());
		backArrowhead.hide = !showBackArrow;
				
		for (double w = 0; w < length; w += length / 100) {
			Coordinate start = new Coordinate(base,  new Coordinate(w * Math.cos(angle), w * Math.sin(angle)));
			Coordinate end = new Coordinate(base, new Coordinate((w + length / 10) * Math.cos(angle), (w + length / 10) * Math.sin(angle)));
		
			PrimativeLine line = new PrimativeLine(this, start, end);
			primatives.add(line);
		}
	}

	@Override
	public void addDrawPrimativesPostChild(Coordinate base, List<DrawPrimative> primatives) {
		
	}

	@Override
	public String getName() {
		return "Arrow";
	}

	@Override
	public List<PropertyEntry> getPropertiesPanelLayout() {
		ArrayList<PropertyEntry> properties = new ArrayList<PropertyEntry>();
		properties.add(new PropertyEntrySlider		("angle"	 , "Angle: ", 0.0, 360.0, angle, true, 90.0, 45.0));
		properties.add(new PropertyEntrySlider		("len"	 , "Length: ", 0.0, 600.0, length, false, 1.0, 1.0));
		properties.add(new PropertyEntryCheckbox("head", "Show arrowhead:", showFrontArrow));
		properties.add(new PropertyEntryCheckbox("tail", "Show arrowtail:", showBackArrow));
		return properties;
	}

	@Override
	public void updateProperty(PropertyEntry property) {
		if (property.id.equals("angle")) {
			PropertyEntrySlider slider = (PropertyEntrySlider) property;
			angle = slider.value / 360.0 * Math.PI * 2;
			updateArrowheads();
			update();
		}
		if (property.id.equals("len")) {
			PropertyEntrySlider slider = (PropertyEntrySlider) property;
			length = slider.value;
			updateArrowheads();
			update();
		}
		if (property.id.equals("head")) {
			showFrontArrow = ((PropertyEntryCheckbox) property).selected;
			update();
		}
		if (property.id.equals("tail")) {
			showBackArrow = ((PropertyEntryCheckbox) property).selected;
			update();
		}
	}
}
