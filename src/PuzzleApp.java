import java.io.IOException;

import org.basex.core.Context;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.value.Value;
import org.jdom2.Element;
import org.jdom2.JDOMException;



import config.ConfigXML;
import control.AbstractController;
import control.PuzzleController;
import model.BaseXModel;
import model.BoardModel;
import model.MongoDBModel;
import view.BoardView;
import view.PuzzleGUI;

/*
 * Copyright 2016 Miguel Ã�ngel RodrÃ­guez-GarcÃ­a (miguel.rodriguez@urjc.es).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Clase principal que ejecuta el juego
 * @Author Miguel Ã�ngel
 * @version 1.0
 */
public class PuzzleApp {
	

    public static void main(String args[]) throws Exception{
        String fileSeparator = System.getProperty("file.separator");
        String imagePath=System.getProperty("user.dir")+fileSeparator+"resources"+fileSeparator;
        String rootPathProject= System.getProperty("user.dir")+fileSeparator;
        String fileName="config.xml";
        //Intercambiar entre las dos siguientes lineas si se quieren meter los datos de inicialización por parametros, o por el archivo
        //de configuracion
        String ff=System.getProperty("user.dir")+fileSeparator+fileName;
        System.out.println(ff);
        /*Sentencia XQuery para validar DTD*/
        String validate="try {"
    			+ "let $doc:='"+fileName+"'"
    			+ "let $schema:='config.dtd'"
    			+ "return validate:dtd($doc,$schema) "
    			+ "  } catch validate:error{'DTD no valido'}";
        Context c = new Context();
        validateDTD(c,validate);
		ConfigXML config= new ConfigXML(ff);
		Element configElement =config.readXML();	
    	
        int imageSize = config.getImageSize();
        int rowNum = config.getRowNumber();
        int columnNum= config.getColNum();
        int shuffleNum =config.getShuffle();



        String[] imageList= new String[config.getImgNames().size()];
        int auxIndex=0;
	        for(String s:config.getImgNames()) {
	        	imageList[auxIndex]=imagePath+config.getImgNames().get(auxIndex);
	        	auxIndex++;
	        }
       
        // Creamos el modelo
        BoardModel boardModel =  new BoardModel(rowNum, columnNum, imageSize, imageList);
        BaseXModel boardBaseX= new BaseXModel(rowNum, columnNum, imageSize, imageList,rootPathProject,"board.xml");
        MongoDBModel boardMongo= new MongoDBModel(rowNum, columnNum, imageSize, imageList); 
        // Creamos el controlador
        PuzzleController controller =  new PuzzleController(configElement);
        
        // Inicializamos la GUI
        PuzzleGUI.initialize(controller, rowNum, columnNum, imageSize, imageList);
        
        // Obtenemos la vista del tablero
        BoardView boardView = PuzzleGUI.getInstance().getBoardView();
        
        controller.setBoardview(boardView);
        controller.setBoardModel(boardModel);
        controller.setBaseXModel(boardBaseX);
        controller.setMongoModel(boardMongo);
        //controller.setBoardModel(boardMongo);
        //controller.setBoardModel(boardBaseX);
        
        // AÃ±adimos un nuevo observador al controlador
        controller.addObserver(boardModel);
        controller.addObserver(boardMongo);
        controller.addObserver(boardBaseX);
        controller.addObserver(boardView);

        // Visualizamos la aplicaciÃ³n.
        PuzzleGUI.getInstance().setVisible(true);
        controller.shuffle(shuffleNum);
    }
    
    public static void validateDTD(Context c, String query) {
    	QueryProcessor proc = new QueryProcessor(query, c);
    	Value result=null;
		try {
			result = proc.value();
		} catch (QueryException e) {
			// TODO Auto-generated catch block
			System.out.println("DTD no valido");
			//e.printStackTrace();
			
		}
	      // Print result as string.
	      System.out.println("DTD valido");
	     
	}
}
