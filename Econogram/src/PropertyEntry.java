import javax.swing.*;

public abstract class PropertyEntry {
	String id;
	
	abstract JPanel producePanel(DrawObject obj);
	
	PropertyEntry(String id) {
		this.id = id;
	}
}
