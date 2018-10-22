package model;

public class PieceModel {

	// id de la imagen
	private int id;
	// índice de fila
	private int indexRow;
	// índice de columna
	private int indexColumn;
	// Tamanno de la imagen
	private int pieceSize;
	// Path de la imagen
	private String imagePath;

	public PieceModel(int id, int indexRow, int indexColumn, int pieceSize, String imagePath) {
		super();
		this.id = id;
		this.indexRow = indexRow;
		this.indexColumn = indexColumn;
		this.pieceSize = pieceSize;
		this.imagePath = imagePath;
	}
	
	
	//Getters y setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIndexRow() {
		return indexRow;
	}

	public void setIndexRow(int indexRow) {
		this.indexRow = indexRow;
	}

	public int getIndexColumn() {
		return indexColumn;
	}

	public void setIndexColumn(int indexColumn) {
		this.indexColumn = indexColumn;
	}

	public int getImageSize() {
		return pieceSize;
	}

	public void setImageSize(int imageSize) {
		this.pieceSize = imageSize;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	@Override
	public String toString() {
		return ("id:" + id);
	}

}
