import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;

public class Label extends DrawObject {
	String text;
	double fontsize;
	boolean rotated = false;

	public Label(String fullSerial, int uid, Canvas canvas, DrawObject parent_) {
		super(fullSerial, uid, canvas, parent_);
		deserialiseTree(fullSerial);
	}
	
	@Override
	public void reloadOnDeserialisation(String data) {
		String parts[] = data.split(",");
		
		fontsize = Double.parseDouble(parts[0]);
		rotated = parts[2].charAt(0) == 'Y';
		
		text = parts[1].replace("@!", ",").replace("@?", "@");
	}
	
	@Override
	public String getSerialisation() {
		return String.format("%f,%s,%c", fontsize, text.replace("@", "@?").replace(",", "@!"), rotated ? 'Y' : 'N');
	}
	
	public Label(Coordinate relativePos, String string) {
		super(relativePos);

		fontsize = 18.0;
		text = string;
	}
	
	@Override
	public RightClickMenu getRightClickMenu(Econogram e, DrawObject o) {
		return new LabelRightClickMenu(e, o);
	}
	
	PropertyEntryTextBox textEntry;
	PropertyEntryRichTextBox richTextEntry;
	
	public List<PropertyEntry> getPropertiesPanelLayout() {
		ArrayList<PropertyEntry> properties = new ArrayList<PropertyEntry>();
		
		String formattedText = text.replace("\n", "<br>");
		while (formattedText.contains("^")) {
			formattedText = formattedText.replaceFirst("\\^", "<sub>").replaceFirst("\\^", "</sub>");
		}
		
		richTextEntry = new PropertyEntryRichTextBox("richtext", "Text:", formattedText);
		properties.add(richTextEntry);
		
		textEntry = new PropertyEntryTextBox("text", "Raw: ", text.replace("\n", "\\n"));
		properties.add(textEntry);
		
		properties.add(new PropertyEntryTextBox("x", "X:", String.format("%.1f", relativePosition.x)));
		properties.add(new PropertyEntryTextBox("y", "Y:", String.format("%.1f", relativePosition.y)));
		properties.add(new PropertyEntrySlider("fontsize", "Font size:", 8.0, 48.0, fontsize, false, 1.0, 0.5));
		properties.add(new PropertyEntryCheckbox("rotate", "Vertical:", rotated));

		return properties;
	}

	@Override
	public String objectType3DigitID() {
		return "LBL";
	}
	
	public void updateProperty(PropertyEntry property) {
		if (property.id.equals("text")) {
			String oldText = text;
			text = ((PropertyEntryTextBox) property).dataText.replace("\\n", "\n");
			
			String formattedText = text.replace("\n", "<br>");
			while (formattedText.contains("^")) {
				formattedText = formattedText.replaceFirst("\\^", "<sub>").replaceFirst("\\^", "</sub>");
			}
			
			String formattedTextFinal = formattedText;

			if (!oldText.equals(text)) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						richTextEntry.blockListeners(true);
						int dot = textEntry.getTextField().getCaret().getDot();
						richTextEntry.getTextField().setText(formattedTextFinal);	
						textEntry.getTextField().getCaret().setDot(dot);
						richTextEntry.blockListeners(false);
					}
				});
				update();
			}
		}
		
		if (property.id.equals("richtext")) {
			String oldText = text;
			text = ((PropertyEntryRichTextBox) property).dataText.replace("<br>", "\n").split("<body>")[1].split("</body>")[0].strip().replace("\\n", "\n").replace("<sub>", "^").replace("</sub>", "^");

			if (!oldText.equals(text)) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						textEntry.blockListeners(true);
						int dot = richTextEntry.getTextField().getCaret().getDot();
						textEntry.getTextField().setText(text.replace("\n", "\\n"));
						richTextEntry.getTextField().getCaret().setDot(dot);
						textEntry.blockListeners(false);
					}
				});
				update();
			}
		}
		
		if (property.id.equals("rotate")) {
			rotated = ((PropertyEntryCheckbox) property).selected;
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
		if (property.id.equals("fontsize")) {
			try {
				fontsize = ((PropertyEntrySlider) property).value;
				update();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void addDrawPrimativesPreChild(Coordinate base, List<DrawPrimative> primatives) {
		
	}

	@Override
	public void addDrawPrimativesPostChild(Coordinate base, List<DrawPrimative> primatives) {
		String[] lines = text.split("\n");

		double zoom = getCanvasParent().zoomPanSettings.zoom;
		
		double yShift = 0.0;
		boolean small = false;
		double widestLine = 0.0;
		List<Double> lineLengths = new ArrayList<Double>();
		for (String bigline : lines) {
			small = false;

			String[] subline = bigline.split("\\^");
								
			double thisLineWidth = 0.0;

			for (String line : subline) {				
				PrimativeText dummy2 = new PrimativeText(this, line, base);
				if (small) {
					dummy2.setFontSize(fontsize / 1.1);
				}
				
				thisLineWidth += dummy2.getWidth() / zoom;
				small = !small;
				thisLineWidth -= (small ? 3 : -3) / zoom;
			}
			
			if (thisLineWidth > widestLine) {
				widestLine = thisLineWidth;
			}
			lineLengths.add(thisLineWidth);
		}
		
		for (String bigline : lines) {
			String[] subline = bigline.split("\\^");
			double xShift = 0;
			double maxHeight = 0;
			
			small = false;
					
			xShift = (widestLine - lineLengths.get(0)) / 2;
			lineLengths.remove(0);
			
			for (String line : subline) {				
				while (line.length() != 0 && line.charAt(0) == '`') {
					line = line.substring(1);
					xShift -= 3 / zoom;
				}
				
				PrimativeText dummy2 = new PrimativeText(this, line, base);
				if (small) {
					yShift += 7;
					dummy2.setFontSize(fontsize / 1.1);
				}
				if (dummy2.getHeight() + (small ? 9 : 0) / zoom> maxHeight) {
					maxHeight = dummy2.getHeight() + (small ? 9 : 0) / zoom;
				}
				Coordinate coordShift = rotated ? new Coordinate(yShift, -xShift) : new Coordinate(xShift, yShift);
				PrimativeText real = new PrimativeText(this, line, new Coordinate(base, coordShift));			
				real.setRotation(rotated ? Math.PI / 2 : 0);
				real.setFontSize(fontsize / (small ? 1.1 : 1.0));
				primatives.add(real);
				if (small) yShift -= 7;
				small = !small;
				xShift += dummy2.getWidth() / zoom - (small ? 3 : -3) / zoom;
			}
			
			yShift += maxHeight;
		}
	}

	@Override
	public String getName() {
		return "Label";
	}

	public void setFontSize(double d) {
		fontsize = d;
	}
}
