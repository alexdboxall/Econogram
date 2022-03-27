import java.util.List;

public class CalculatedPoint extends Point {
	PrimaryLine line1;
	PrimaryLine line2;
	Axis primaryAxisAtCreation;
	
	int labelReloadUID;
	int hzLineReloadUID;
	int vtLineReloadUID;
	int line1ReloadUID;
	int line2ReloadUID;
	int primaryAxisReloadUID;
	
	@Override
	public void reloadOnDeserialisation(String data) {
		showDot = data.charAt(0) == 'T';
		vtShowing = data.charAt(1) == 'T';
		hzShowing = data.charAt(2) == 'T';
		circular = data.charAt(3) == 'T';
				
		String parts[] = data.substring(4).split(",");
		labelReloadUID = Integer.parseInt(parts[0]);
		hzLineReloadUID = Integer.parseInt(parts[1]);
		vtLineReloadUID = Integer.parseInt(parts[2]);
		line1ReloadUID = Integer.parseInt(parts[3]);
		line2ReloadUID = Integer.parseInt(parts[4]);
		primaryAxisReloadUID = Integer.parseInt(parts[5]);
	}
	
	@Override
	public String objectType3DigitID() {
		return "CPT";
	}
	
	@Override
	public String getSerialisation() {
		return String.format("%c%c%c%c%d,%d,%d,%d,%d,%d", 
				showDot ? 'T' : 'F', vtShowing ? 'T' : 'F', hzShowing ? 'T' : 'F', circular ? 'T' : 'F', 
				label.getUniqueID(), hzLine.getUniqueID(), vtLine.getUniqueID(), line1.getUniqueID(), line2.getUniqueID(), primaryAxisAtCreation.getUniqueID());
	}
	
	public CalculatedPoint(String fullSerial, int uid, Canvas canvas, DrawObject parent_) {
		super(fullSerial, uid, canvas, parent_);
		deserialiseTree(fullSerial);
		
		label = (Label) findChildWithUID(labelReloadUID);
		hzLine = (PointLine) findChildWithUID(hzLineReloadUID);
		vtLine = (PointLine) findChildWithUID(vtLineReloadUID);
		line1 = (PrimaryLine) findChildWithUID(line1ReloadUID);
		line2 = (PrimaryLine) findChildWithUID(line2ReloadUID);
		primaryAxisAtCreation = (Axis) findChildWithUID(primaryAxisReloadUID);
		
		recalculateLocation();
	}
	
	public CalculatedPoint(PrimaryLine l1, PrimaryLine l2, Axis primaryAxis) {
		super(new Coordinate(0.0, 0.0));
		
		canDrag = false;
		
		line1 = l1;
		line2 = l2;
		
		primaryAxisAtCreation = primaryAxis;

		recalculateLocation();
	}
	
	void recalculateLocation() {
		if (line1 == null || line2 == null) {
			return;
		}
		
		Coordinate intersection = line1.intersection(line2);
		
		if (intersection != null) {
			relativePosition.x = intersection.x + line1.parent.relativePosition.x - primaryAxisAtCreation.relativePosition.x;
			relativePosition.y = intersection.y + line1.parent.relativePosition.y - primaryAxisAtCreation.relativePosition.y;
		} else {
			intersection = line2.intersection(line1);
			if (intersection != null) {
				relativePosition.x = intersection.x + line1.parent.relativePosition.x - primaryAxisAtCreation.relativePosition.x;
				relativePosition.y = intersection.y + line1.parent.relativePosition.y - primaryAxisAtCreation.relativePosition.y;
			} 
		}
	}
	
	@Override
	public void addDrawPrimativesPreChild(Coordinate base, List<DrawPrimative> primatives) {
		recalculateLocation();
		super.addDrawPrimativesPreChild(base, primatives);
	}
	
	
}
