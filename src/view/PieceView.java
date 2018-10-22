package view;

import javax.swing.*;
import java.awt.*;

/**
 * Clase que representa la vista del tablero
 * 
 * @author Miguel Ã�ngel
 * @version 1.0
 */
public class PieceView extends ImageIcon implements Cloneable {

	// id de la imagen
	private int id;
	// Ã­ndice de fila
	private int indexRow;
	// Ã­ndice de columna
	private int indexColumn;
	// Ã­ndice de fila
	private int drawIndexRow;
	// Ã­ndice de columna
	private int drawIndexColumn;
	// TamaÃ±o de la imagen
	private int imageSize;

	/**
	 * Constructor de una clase
	 * 
	 * @param indexRow
	 *            indice de fila
	 * @param indexColumn
	 *            indice de columna
	 * @param imagePath
	 *            ubicaciÃ³n de la imagen.
	 */
	public PieceView(int id, int indexRow, int indexColumn, int imageSize, String imagePath) {
		super(imagePath);
		this.id = id;
		this.indexRow = indexRow;
		this.indexColumn = indexColumn;
		this.imageSize = imageSize;
	}

	public PieceView(int id, int indexRow, int indexColumn, int imageSize, Image image) {
		super(image);
		this.id = id;
		this.indexRow = indexRow;
		this.indexColumn = indexColumn;
		this.imageSize = imageSize;
	}
	
	//Getters y setters
	public int getIndexRow() {
		return indexRow;
	}

	public int getIndexColumn() {
		return indexColumn;
	}

	public void setIndexRow(int indexRow) {
		this.indexRow = indexRow;
		this.drawIndexRow = indexRow * imageSize;
	}

	public void setIndexColumn(int indexColumn) {
		this.indexColumn = indexColumn;
		this.drawIndexColumn = indexColumn * imageSize;
	}

	public int getImageSize() {
		return imageSize;
	}

	public void setImageSize(int imageSize) {
		this.imageSize = imageSize;
	}

	public int getId() {
		return this.id;
	}

	public String toString() {
		return ("id:" + id);
	}

	public int getDrawIndexRow() {
		return drawIndexRow;
	}

	public void setDrawIndexRow(int drawIndexRow) {
		this.drawIndexRow = drawIndexRow;
	}

	public int getDrawIndexColumn() {
		return drawIndexColumn;
	}

	public void setDrawIndexColumn(int drawIndexColumn) {
		this.drawIndexColumn = drawIndexColumn;
	}

}
