import javax.swing.*;

public abstract class PropertyEntry {
	String id;
	
	abstract PropertyEntryPanel producePanel(DrawObject obj);
	
	PropertyEntry(String id) {
		this.id = id;
	}
}
