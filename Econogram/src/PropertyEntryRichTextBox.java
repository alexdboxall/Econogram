
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.beans.PropertyChangeListener;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Caret;

public class PropertyEntryRichTextBox extends PropertyEntry {
	String displayText;

	String dataText;
	
	PropertyEntryRichTextBox self;
	JEditorPane field;
	
	boolean listenersBlocked = false;
	
	void blockListeners(boolean state)  {
		listenersBlocked = state;
	}
	
	JEditorPane getTextField() {
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

		field = new JEditorPane();
		field.setContentType("text/html");
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
						if (oldText != dataText) {
							obj.updateProperty(self);
						}
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

		class DownAction extends AbstractAction {

	        @Override
	        public void actionPerformed(ActionEvent e) {
	            int dot = field.getCaret().getDot();
	            int mark = field.getCaret().getMark();
	            
	            if (dot != mark) {
	            	if (dot > mark) {
	            		dot = field.getCaret().getMark();
	    	            mark = field.getCaret().getDot();
	            	}

	            	String text = field.getText().split("<body>")[1].split("</body>")[0].strip();
		            String preText = text.substring(0, dot - 1);
		            String selText = text.substring(dot - 1, mark - 1);
		            String postText = text.substring(mark - 1);
		            		            
		            field.setText(String.format("%s<sub>%s</sub>%s ", preText, selText, postText));
	            }
	        }
	    }
		
	    class UpAction extends AbstractAction {

	        @Override
	        public void actionPerformed(ActionEvent e) {
	            int dot = field.getCaret().getDot();
	            int mark = field.getCaret().getMark();
	            
	            if (dot != mark) {
	            	if (dot > mark) {
	            		dot = field.getCaret().getMark();
	    	            mark = field.getCaret().getDot();
	            	}

	            	String text = field.getText().split("<body>")[1].split("</body>")[0].strip();
		            String preText = text.substring(0, dot - 1);
		            String selText = text.substring(dot - 1, mark - 1);
		            String postText = text.substring(mark - 1);
		            		            
		            field.setText(String.format("%s%s%s ", preText, selText.replace("<sub>", "").replace("</sub>", ""), postText));
	            }
	        }
	    }
		
	    int condition = JComponent.WHEN_FOCUSED;
        InputMap inputMap = field.getInputMap(condition);
        ActionMap actionMap = field.getActionMap();

        KeyStroke upKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        actionMap.put((String) inputMap.get(upKeyStroke), new UpAction());
		
        KeyStroke downKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        actionMap.put((String) inputMap.get(downKeyStroke), new DownAction());
        
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
				
		field.setPreferredSize(new Dimension(170, 70));
		c.gridx = 0;
		c.gridy = 0;
		panel.add(label, c);
		c.gridx = 1;
		c.gridy = 0;
		panel.add(field, c);
		
		return panel;
	}

	PropertyEntryRichTextBox(String id, String displayText, String dataText) {
		super(id);
		this.displayText = displayText;
		this.dataText = dataText;
		
	}
}
