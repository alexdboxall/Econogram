
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.*;

public class PropertyEntryTextBox extends PropertyEntry {
	String displayText;

	String dataText;
	boolean listenersBlocked;
	PropertyEntryTextBox self;
	JTextField field;
	
	void blockListeners(boolean state)  {
		listenersBlocked = state;
	}
	
	JTextField getTextField() {
		return field;
	}
	
	PropertyEntryPanel producePanel(DrawObject obj) {
		PropertyEntryPanel panel = new PropertyEntryPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(layout);
		
		self = this;

		JLabel label = new JLabel(displayText, JLabel.LEFT);
		label.setFont(new Font("Courier New", Font.PLAIN, 12));

		field = new JTextField();
		field.setFont(new Font("Courier New", Font.PLAIN, 12));
		field.setText(dataText);
		field.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				if (listenersBlocked) {
					return;
				}
				obj.getCanvasParent().econogram.actionManager.add(new Action() {
					String oldText;
					
					@Override
					public boolean execute() {
						oldText = dataText;
						dataText = field.getText();
						obj.updateProperty(self);
						return true;
					}

					@Override
					public boolean undo() {
						dataText = oldText;
						field.setText(oldText);
						obj.updateProperty(self);
						return true;
					}
					
					@Override
					public boolean redo() {
						return execute();
					}
				});	
			}
		});
		
		panel.doubleClickHandler = new Action() {
			//not actually used as an action, it's just an action type so we can store a function
			@Override
			public boolean execute() {
				field.requestFocus();
				field.selectAll();
				return false;
			}

			@Override
			public boolean undo() {
				return false;
			}
			
			@Override
			public boolean redo() {
				return false;
			}
		};
		
		field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				obj.getCanvasParent().econogram.actionManager.add(new Action() {
					String oldText;
					
					@Override
					public boolean execute() {
						oldText = dataText;
						dataText = field.getText();
						obj.updateProperty(self);
						return true;
					}

					@Override
					public boolean undo() {
						dataText = oldText;
						obj.updateProperty(self);
						return true;
					}
					
					@Override
					public boolean redo() {
						return execute();
					}
				});	
			}
		});
				
		field.setPreferredSize(new Dimension(170, 25));
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
