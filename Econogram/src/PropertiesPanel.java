
import javax.swing.*;
import javax.swing.border.*;
import java.util.List;

import java.awt.*;

public class PropertiesPanel extends JPanel {
	private static final long serialVersionUID = -2639415047143053852L;
		
	DrawObject object;
	PropertyEntryPanel doubleClickPanel;
	
	void detach() {
		doubleClickPanel = null;
		if (object != null) {
			object.markSelected(false);
		}
		removeAll();
		validate();
		super.repaint();
		object = null;
		
		JLabel label = new JLabel("Select an object by clicking on it           ");
		label.setFont(new Font(label.getFont().getFamily(), Font.BOLD, 12));
		add(label, BorderLayout.NORTH);

		validate();
	}
	
	void goToDoubleClickProperty(DrawObject obj) {
		regenerate();
		
		if (doubleClickPanel != null && doubleClickPanel.doubleClickHandler != null) {
			doubleClickPanel.doubleClickHandler.execute();
		}
	}
	
	void regenerate() {
		doubleClickPanel = null;
		removeAll();
		validate();
		super.repaint();
		
		if (object == null) {
			detach();
			return;
		}
		
		JLabel titleLine = new JLabel(object.getName());
		titleLine.setFont(new Font(titleLine.getFont().getFamily(), Font.BOLD, 16));
		add(titleLine, BorderLayout.NORTH);
		
		JPanel subPanel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		subPanel.setLayout(layout);
		add(subPanel, BorderLayout.WEST);
		
		List<PropertyEntry> properties = object.getPropertiesPanelLayout();
		for (PropertyEntry property : properties) {
			c.gridx = 0;
			c.gridy++;
			PropertyEntryPanel panel = property.producePanel(object);
			if (doubleClickPanel == null && panel.doubleClickHandler != null) {
				doubleClickPanel = panel;
			}
			subPanel.add(panel, c);
		}
		
		validate();
	}
	
	void attach(DrawObject obj) {
		doubleClickPanel = null;
		if (object != null) {
			object.markSelected(false);
		}
		obj.markSelected(true);

		if (obj == object) return;
				
		removeAll();
		validate();
		super.repaint();
		object = obj;
		
		regenerate();
	}
	
	PropertiesPanel() {			
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(20, 20, 20, 20));
		
		detach();
	}
}
