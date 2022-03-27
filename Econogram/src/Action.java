
public abstract class Action {
	abstract boolean execute();
	abstract boolean undo();
	abstract boolean redo();
	
	boolean isFence() {
		return true;
	}
	
	DrawObject selectedObjectAtTheTime;
	Axis primaryAxisAtTheTime;
}
