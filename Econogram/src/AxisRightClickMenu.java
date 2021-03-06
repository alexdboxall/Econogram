import java.awt.event.*;
import javax.swing.*;

public class AxisRightClickMenu extends RightClickMenu {
	
	AxisRightClickMenu(Econogram e, DrawObject c) {
		super(e, c);
	}

	public void init() {
		
		JMenuItem setPrimary = new JMenuItem("Set as Primary Axis");
		setPrimary.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				econogram.setPrimaryAxis((Axis) object);
			}
		});
		add(setPrimary); 
		
		add(new JSeparator());
		
		JMenuItem delete = new JMenuItem("Delete");
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				econogram.actionManager.add(econogram.DELETE_SELECTED_OBJECT.build());
			}
		});
		add(delete); 
	}
}
