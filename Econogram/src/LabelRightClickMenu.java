import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

public class LabelRightClickMenu extends RightClickMenu {
	
	LabelRightClickMenu(Econogram e, DrawObject c) {
		super(e, c);
	}

	public void init() {
		JMenuItem setPrimary = new JMenuItem("Delete");
		setPrimary.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				econogram.actionManager.add(econogram.DELETE_SELECTED_OBJECT.build());
			}
		});
		add(setPrimary); 
	}
}
