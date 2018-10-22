package command;

import control.PuzzleController;

public class MovementCommand implements Command {

	private int[] movement;
	private PuzzleController controller;

	public MovementCommand(int[] movement, PuzzleController controller) {
		this.movement = movement;
		this.controller = controller;
	}
	
	//Ejecuta el movimiento guardado en el command
	public void execute(){
		controller.notifyObservers(movement[0], movement[1]);
	}
	
	public int[] getMovement(){
		return movement;
	}
}
