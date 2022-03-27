import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

public class KeynesianLRAS extends PrimaryLine {

	double sliderVal;
	double keynesianSectionRadius = 100.0;
	
	public void copy() {
		
	}

	@Override
	public String objectType3DigitID() {
		return "KNS";
	}
	
	public KeynesianLRAS(String fullSerial, int uid, Canvas canvas, DrawObject parent_) {
		super(fullSerial, uid, canvas, parent_);
		deserialiseTree(fullSerial);
	}
	
	@Override
	public void reloadOnDeserialisation(String data_) {
		String data = primaryLineDeserialisation(data_);

		String parts[] = data.split(",");
		keynesianSectionRadius = Double.parseDouble(parts[0]);
	}
	
	@Override
	public String getSerialisation() {
		return String.format("%s%f", primaryLineSerialisation(), keynesianSectionRadius);
	}
	
	public KeynesianLRAS(Coordinate coord, double defaultRadius) {
	
		super(coord);
		
		alignDragWithPointLineDotsX2 = true;
		keynesianSectionRadius = defaultRadius;
		canDrag = true;
	}
	
	@Override
	public String getDefaultLabelText() {
		return "LRAS";
	}

	public double getYFromX(double x, double radius) {
		x /= radius;
		return (1 - Math.sqrt(1 - x * x)) * radius;
	}
	
	@Override
	public List<IntersectableLine> getLineBreakdown() {
		List<IntersectableLine> l = new ArrayList<IntersectableLine>();
		
		double gradient = 0;
		
		
		final double increment = 2.0;
		
		//ugly but it works.
		for (double i = -keynesianSectionRadius; i < 0; i += increment) {
			l.add(new IntersectableLine(new Coordinate(-i - keynesianSectionRadius, -getYFromX(i, keynesianSectionRadius)), new Coordinate(-i - increment - keynesianSectionRadius, -getYFromX(i + increment, keynesianSectionRadius) )));
		}
		
		for (double i = -999.0; i < -keynesianSectionRadius; i += increment) {
			l.add(new IntersectableLine(new Coordinate(i, i * gradient), new Coordinate(i + increment, (i + increment) * gradient)));
		}
		
		
		for (double i = -keynesianSectionRadius; i > -999.0; i -= increment) {
			l.add(new IntersectableLine(new Coordinate(0, i), new Coordinate(0, i + increment)));
		}
	
		return l;
	}

	@Override
	public String getName() {
		return "Keynesian LRAS Line";
	}
	
	PropertyEntrySlider slider;
	
	@Override
	public List<PropertyEntry> getPropertiesPanelLayout() {
		ArrayList<PropertyEntry> properties = new ArrayList<PropertyEntry>();
		
		slider = new PropertyEntrySlider("radius", "Radius:", 10, 400, keynesianSectionRadius, false, 1, 1);
		properties.add(new PropertyEntryTextBox("x", "X:", String.format("%.1f", relativePosition.x)));
		properties.add(new PropertyEntryTextBox("y", "Y:", String.format("%.1f", relativePosition.y)));
		properties.add(slider);

		update();

		return properties;
	}

	@Override
	public void updateProperty(PropertyEntry property) {		
		if (property.id.equals("radius")) {
			double v = ((PropertyEntrySlider) property).value;
			sliderVal = ((PropertyEntrySlider) property).value;
			keynesianSectionRadius = v;
			update();
		}
		if (property.id.equals("x")) {
			try {
				relativePosition.x = Double.parseDouble(((PropertyEntryTextBox) property).dataText);
				update();
			} catch (Exception e) {
				
			}
		}
		if (property.id.equals("y")) {
			try {
				relativePosition.y = Double.parseDouble(((PropertyEntryTextBox) property).dataText);
				update();
			} catch (Exception e) {
				
			}
		}
	}
}
