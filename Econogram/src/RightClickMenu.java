import javax.swing.*;
import java.awt.*;

public abstract class RightClickMenu extends JPopupMenu {
	Econogram econogram;
	DrawObject object;
	
	public abstract void init();
	
	RightClickMenu(Econogram e, DrawObject c) {
		super();
		
		econogram = e;
		object = c;

		init();
	}
}
