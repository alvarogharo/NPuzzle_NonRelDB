package command;

/**
 * Clase que representa un comando que nos permite implementar las funciones de
 * undo y redo.
 * 
 * @author Miguel Ã�ngel
 * @version 1.0
 */
public interface Command {
	
	public void execute();
	
	public int[] getMovement();
}
