
public interface Action {
	boolean execute();
	boolean undo();
	boolean redo();
}
