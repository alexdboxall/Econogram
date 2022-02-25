
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.*;

public class PropertyEntryTextBox extends PropertyEntry {
	String displayText;

	String dataText;
	
	PropertyEntryTextBox self;
	
	JPanel producePanel(DrawObject obj) {
		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(layout);
		
		self = this;

		JLabel label = new JLabel(displayText, JLabel.LEFT);
		label.setFont(new Font("Courier New", Font.PLAIN, 12));

		JTextField field = new JTextField();
		field.setFont(new Font("Courier New", Font.PLAIN, 12));
		field.setText(dataText);
		field.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				dataText = field.getText();
				obj.updateProperty(self);
			}
		});
		field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dataText = field.getText();
				obj.updateProperty(self);
			}
		});
				
		field.setPreferredSize(new Dimension(130, 25));
		c.gridx = 0;
		c.gridy = 0;
		panel.add(label, c);
		c.gridx = 1;
		c.gridy = 0;
		panel.add(field, c);
		
		return panel;
	}

	PropertyEntryTextBox(String id, String displayText, String dataText) {
		super(id);
		this.displayText = displayText;
		this.dataText = dataText;
		
	}
}
