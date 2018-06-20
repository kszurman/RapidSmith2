
package edu.byu.ece.rapidSmith.device.browser;

import com.sun.javafx.geom.Line2D;
import edu.byu.ece.rapidSmith.design.xdl.XdlDesign;
import edu.byu.ece.rapidSmith.device.*;
import edu.byu.ece.rapidSmith.device.families.FamilyInfo;
import edu.byu.ece.rapidSmith.device.families.FamilyInfos;
import edu.byu.ece.rapidSmith.device.families.Virtex6;
import edu.byu.ece.rapidSmith.gui.NumberedHighlightedTile;
import edu.byu.ece.rapidSmith.gui.TileColorsJavaFx;
import edu.byu.ece.rapidSmith.gui.wireItemJavaFx;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;//Do I need?
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.w3c.dom.css.Rect;

import java.util.*;

public class TileViewTest extends Pane {
        /**DeviceBrowser copy*/
        DeviceBrowserJavaFx deviceBrowser;

        wireItemJavaFx displayWire;

        /**The current X location of the mouse*/
        public int currX;
        /**The current Y location of the mouse*/
        public int currY;
        /**The previous X location of the mouse*/
        public int prevX;
        /**The previous Y location of the mouse*/
        public int prevY;
        /** current selected tile x coordinate*/
        public int selX;
        /** current selected tile y coordinate*/
        public int selY;

        /**The rendered size of each XDL Tile*/
        public int tileSize = 40;//
        public double lineWidth = 1;
        int offset = (int) Math.ceil((lineWidth / 2.0));
        public int rectSide = (tileSize - 4)*offset;
        /**Number of tile columns being referenced on the device*/
        public int cols;
        /**Number of tile rows being referenced on the device*/
        public int rows;
        /**When hiding tiles, this contains the grid of drawn tiles*/
        public Tile[][] drawnTiles;
        /**Gets the X coordinate of the tile in the drawnTiles grid*/
        public HashMap<Tile, Integer> tileXMap;
        /** Gets the Y coordinate of the tile in the drawnTiles grid */
        public HashMap<Tile,Integer> tileYMap;
        /**Width of the lines drawn in between tiles when columns/rows are hidden*/

        /**The device corresponding to this scene*/
        protected Device device;
        /**The current design associated with this tileScene*/
        private XdlDesign design;
        /**The wire enumerator corresponding to this scene*/
        protected WireEnumerator we;
        /** This is the set of column tile types which should not be drawn */
        private HashSet<TileType> tileColumnTypesToHide;
        /** This is the set of row tile types which should not be drawn */
        private HashSet<TileType> tileRowTypesToHide;

        public HashMap<Tile,Rectangle> drawnTilesMap;
        /**canvas for drawing everything*/
        private static GraphicsContext gc;
        /**Menu for getting reachability of tiles*/
        ContextMenu contextMenu = new ContextMenu();

        public Rectangle highlight;

        /** */
        protected static Font font1 = new Font("Times New Roman", 10);
        /** */
        protected static Font font2 = new Font("Times New Roman", 16);
        /** */
        protected static Font font3 = new Font("Times New Roman", 20);

        /**	 */


        private ArrayList<NumberedHighlightedTile> currentTiles = new ArrayList<>();

        private ArrayList<Line> lines = new ArrayList<>();

        private ObservableList<Line> wires =  FXCollections.observableList(lines);

        //Tile src, Wire wireSrc, Tile dst, Wire wireDst
        private Tile source;
        private Wire wireSource;
        private Tile dest;
        private Wire wireDest;
        private Integer hops;
        boolean reachabilityDrawn;
        //boolean newDevice;

        Tile selectedTile;
        Tile previousTile = null;
        private Tile reachabilityTile;
        //Initialize object with null



        /**
         * Creates a new tile window with a selected design.
         * @param device The device to associate with this scene.
         * @param hideTiles A flag to hide/show certain tiles to make the fabric appear more homogeneous.
         * @param drawSites A flag to draw boxes to represent primitives.
         */
        public TileViewTest(DeviceBrowserJavaFx dB, Device device, boolean hideTiles, boolean drawSites){
            setStyle("-fx-background-color: black;");//set background to black
            this.deviceBrowser = dB;
            setDevice(device);
            setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            selectedTile = null;
            //widthProperty().addListener(evt -> initializeScene(hideTiles, drawSites));
            //heightProperty().addListener(evt ->initializeScene(hideTiles, drawSites));
            initializeScene(hideTiles, drawSites);
            MenuItem action1 = new MenuItem("Draw Reachability (1 Hop)");
            MenuItem action2 = new MenuItem("Draw Reachability (2 Hop)");
            MenuItem action3= new MenuItem("Draw Reachability (3 Hop)");
            MenuItem action4 = new MenuItem("Draw Reachability (4 Hop)");
            MenuItem action5 = new MenuItem("Draw Reachability (5 Hop)");
            MenuItem actionClear = new MenuItem("Clear Highlighted Tiles");
            action1.setOnAction(a-> {
                hops = 1;
                menuReachability();
            });
            action2.setOnAction(a->{
                hops = 2;
                menuReachability();
            });
            action3.setOnAction(a->{
                hops = 3;
                menuReachability();
            });
            action4.setOnAction(a->{
                hops = 4;
                menuReachability();
            });
            action5.setOnAction(a->{
                hops = 5;
                menuReachability();
            });
            actionClear.setOnAction(a->menuReachabilityClear());
            contextMenu.getItems().addAll(action1, action2, action3, action4, action5, actionClear);
            reachabilityDrawn = false;
        }

        /**
         * Initializes the canvas where tiles are shown in Device Browser
         * @param hideTiles Set true to hide tiles, else false to not
         * @param drawSites Set true to drawSites, else false to not
         */
        public void initializeScene(boolean hideTiles, boolean drawSites) {
            prevX = 0;
            prevY = 0;
            displayWire = null;
            if (device != null) {
                tileColumnTypesToHide = new HashSet<>();
                tileRowTypesToHide = new HashSet<>();
                if (hideTiles) {
                    populateTileTypesToHide();
                }
                drawFPGAFabric(drawSites);
            }
        }
        /**
         * This Method returns the tile on the FPGA Fabric grid depending on the coordinates given
         * @param x coordinate in x plane
         * @param y coordinate in y plane
         * @return correct tile at (x,y) location
         */
        public Tile getTile(double x, double y){
            currX = (int) Math.floor(x / tileSize);
            currY = (int) Math.floor(y / tileSize);

            if (currX >= 0 && currY >= 0 && currX < cols && currY < rows){// && (currX != prevX || currY != prevY)){
                return drawnTiles[currY][currX];
            }
            return null;
        }

        /**
         *Just passes on info to other getTile function. Not sure if still need
         * @param event is where mouse pointer was double clicked
         * @return tile where event occurred
         */
        public Tile getTile(MouseEvent event){
            return getTile(event.getX(), event.getY());
        }


        /**
         * If primary mouse button is clicked twice on a tile, then it is highlighted with a yellow box(i.e. selected)
         * Sites and Wire Connections are shown in respective tables.
         * @param event is the double click event and used to get coordinates for drawing
         */
        public void mouseDoubleClickEvent(MouseEvent event) {
            selectedTile = getTile(event);//currX and currY are set here
            selX = currX;
            selY = currY;
            prevX = currX;
            prevY = currY;
            previousTile = selectedTile;
            if (selX >= 0 && selY >= 0 && selX < cols && selY < rows) {
                this.getChildren().remove(highlight);
                highlight = new Rectangle(currX * tileSize, currY * tileSize,rectSide-1, rectSide);
                highlight.setStroke(Color.YELLOW);
                highlight.setStrokeWidth(3);
                highlight.setFill(Color.TRANSPARENT);
                this.getChildren().add(highlight);
            }
        }

        /**
         * Method for drawing tile wires
         * @param src The tile where wire and associated connections begin
         * @param wireSrc The wire which has many connections
         * @param dst The tile where wire and associated connections end
         * @param wireDst
         */
        void drawWire(Tile src, Wire wireSrc, Tile dst, Wire wireDst){
            double enumSize = we.getWires().length;
            double offsetX1 = 1/enumSize;
            double offsetX2 = 10%enumSize;
            double offsetY = -2;
            System.out.println("value of wireSrc.getWireEnum()="+wireSrc.getWireEnum());
            System.out.println("value for javafx enumSize="+enumSize);

            try {
                double x1 = (double) tileXMap.get(src) * tileSize + (wireSrc.getWireEnum() % tileSize);
                double y1 = (double) tileYMap.get(src) * tileSize + (wireSrc.getWireEnum() * tileSize)/enumSize;
                double x2 = (double) tileXMap.get(dst) * tileSize + (wireDst.getWireEnum() % tileSize);
                double y2 = (double) tileYMap.get(dst) * tileSize + (wireDst.getWireEnum() * tileSize)/enumSize;
                System.out.println("x1="+x1);
                System.out.println("y1="+y1);
                System.out.println("x2="+x2);
                System.out.println("y2="+y2);

                Line line = new Line(x1, y1, x2, y2);
                line.setStrokeWidth(1.2);
                line.setStroke(Color.ORANGE);
                line.setFill(Color.ORANGE);
                line.setOnMouseEntered(e-> {
                    line.setFill(Color.RED);
                    line.setStroke(Color.RED);
                });
                line.setOnMouseExited(e-> {
                    line.setFill(Color.ORANGE);
                    line.setStroke(Color.ORANGE);
                });
                line.setOnMouseClicked(e-> drawConnectingWires(dst, wireDst));
                lines.add(line);
                getChildren().add(line);


            }
            catch(NullPointerException e){
                return;
            }
        }

     void drawConnectingWires(Tile tile, Wire wire){
        resetAfterWire();
        if(tile == null) return;
        TileWire tileWire = tile.getWire(wire.getName());
        if(tileWire.getWireConnections().isEmpty()) return;
        for(Connection newWire : tileWire.getWireConnections()){
            drawWire(tile, wire, newWire.getSinkWire().getTile(), newWire.getSinkWire());
        }
    }
        /**
         * Method for drawing FPGA design including tiles and sites
         * Used many times for erasing/resetting tileWindow
         * @param drawSites Set true if user wants to display sites of design
         */
        private void drawFPGAFabric(boolean drawSites){
            TreeSet<Integer> colsToSkip = new TreeSet<>();
            TreeSet<Integer> rowsToSkip = new TreeSet<>();
            for(Tile tile : device.getTiles()){
                TileType type = tile.getType();
                if(tileColumnTypesToHide.contains(type)){
                    colsToSkip.add(tile.getColumn());
                }
                if(tileRowTypesToHide.contains(type)){
                    rowsToSkip.add(tile.getRow());
                }
            }
            // Create new tile layout without hidden tiles
            int i=0,j=0;
            drawnTiles = new Tile[rows-rowsToSkip.size()][cols-colsToSkip.size()];
            tileXMap = new HashMap<>();
            tileYMap = new HashMap<>();
            for(int row = 0; row < rows; row++) {
                if(rowsToSkip.contains(row)) continue;
                for (int col = 0; col < cols; col++) {
                    if(colsToSkip.contains(col)) continue;
                    Tile tile = device.getTile(row, col);
                    drawnTiles[i][j] = tile;
                    tileXMap.put(tile, j);
                    tileYMap.put(tile, i);
                    j++;
                }
                i++;
                j=0;
            }
            rows = rows-rowsToSkip.size();
            cols = cols-colsToSkip.size();
            setPrefSize((cols + 1) * (tileSize + 1), (rows + 1) * (tileSize + 1));
            i = 0;
            for(int col : colsToSkip){
                int realCol = col - i;
                this.getChildren().add(new Line(tileSize*realCol-1, 0, tileSize*realCol-1, rows*tileSize-3));
                i++;
            }
            i=0;
            for(int row : rowsToSkip){
                int realRow = row - i;
                this.getChildren().add(new Line(0,tileSize*realRow-1, cols*tileSize-3,tileSize*realRow-1));
                i++;
            }
//
            // Draw the tile layout
//        int offset = (int) Math.ceil((lineWidth / 2.0));
//
            FamilyInfo familyInfo = FamilyInfos.get(device.getFamily());
            for(int y = 0; y < rows; y++){
                for(int x = 0; x < cols; x++){
                    Tile tile = drawnTiles[y][x];
                    TileType tileType = tile.getType();
                    Color color = TileColorsJavaFx.getSuggestedTileColor(tile);

                    int rectX = x * tileSize;
                    int rectY = y * tileSize;
                    if(drawSites){
                        if (familyInfo.clbTiles().contains(tileType)) {
                            drawCLB(rectX, rectY, tile, color);
                        }else if (familyInfo.switchboxTiles().contains(tileType)) {
                            drawSwitchBox(rectX, rectY, color);
                        } else if (familyInfo.bramTiles().contains(tileType)) {
                            drawBRAM(rectX, rectY, color);
                        } else if (familyInfo.dspTiles().contains(tileType)) {
                            drawDSP(rectX, rectY, color);
                        } else { // Just fill the tile in with a appropriate color
                            colorTile(x, y, color);
                        }
                    }
                    else{
                        colorTile(x, y, color);
                    }
                }
            }
        }

        /**
         * Redraws FPGA background and also erases all previously drawn wires
         * Then redraws correct selected tile
         */
        public void resetAfterWire(){
            getChildren().removeAll(lines);
            lines.clear();
        }

        /**
         *
         * @param e The event that tells where mouse cursor is located on TileWindow
         * @return String contains the contents for status label at bottom of application. Includes partName, tileName, coordinates, etc.
         */
        public String mouseMoveEvent(MouseEvent e) {
            Point2D mousePos = new Point2D(e.getX(), e.getY());
            String tileName;
            if (device != null) {
                Tile tile = getTile(mousePos.getX(), mousePos.getY());
                if(tile != null){
                    tileName = device.getPartName() + " | " + tile.getName() +
                            " | " + tile.getType() + " (" + currX + "," + currY + ")";
                    return tileName;
                }
            }
            return "";
        }

        /*
         * Getters and Setters
         */

        public Tile getSelectedTile() {return selectedTile;}

        public int getDrawnTileX (Tile tile){
            Integer tmp = tileXMap.get(tile);
            if (tmp == null)
                return -1;
            return tmp;
        }

        public int getDrawnTileY (Tile tile){
            Integer tmp = tileYMap.get(tile);
            if (tmp == null)
                return -1;
            return tmp;
        }


        public XdlDesign getDesign () {
            return design;
        }

        public void setDesign (XdlDesign design){
            this.design = design;
            if (this.design != null) {
                setDevice(design.getDevice());
            }
        }

        public Device getDevice () {
            return device;
        }

        /**
         * Sets device from part selected from partsList TreeView
         * Sets other needed variables from device
         * @param device
         */
        public void setDevice (Device device){
            this.device = device;
            this.we = device.getWireEnumerator();
            rows = device.getRows();
            cols = device.getColumns();
            if(device!=null) {
                setWidth((cols + 1) * (tileSize + 1));
                setHeight((rows + 1) * (tileSize + 1));
            }
            else{
                setWidth(tileSize + 1);
                setHeight(tileSize + 1);
            }
        }

        public double getCurrX () {
            return currX;
        }

        public double getCurrY () {
            return currY;
        }

        public int getTileSize () {
            return tileSize;
        }

       public ObservableList<Line> getDrawnWires() {return wires;}
        /*
         * Helper Drawing Methods..Needs redone using graphics context of Canvas instead.
         */

        /**
         * Helper drawing methods for drawing FPGA fabric. Correctly draws different tiles with respective color.
         */
        private void drawCLB(int rectX, int rectY, Tile tile, Color color){
            Rectangle rect = new Rectangle(rectX, rectY + rectSide / 2, rectSide / 2 - 1, rectSide / 2 - 1);
            rect.setStroke(color);
            Rectangle rect1 = new Rectangle(rectX + rectSide / 2, rectY, rectSide / 2 - 1, rectSide / 2 - 1);
            rect1.setStroke(color);
            getChildren().addAll(rect, rect1);

        }

        private void drawBRAM(int rectX, int rectY,  Color color){
            switch(device.getFamily().name()) {
//			case SPARTAN6:
//				gc.strokeRect(rectX, rectY - 3 * tileSize, rectSide - 1, 4 * rectSide + 3 * 2 * offset - 1);
//				gc.setStroke(color.darker());
//                gc.strokeRect(rectX + 2, rectY - 3 * tileSize + 2, rectSide - 1 - 4, 2 * rectSide + 2 * offset - 1 - 2);
//                gc.strokeRect(rectX + 2, rectY - tileSize, rectSide - 1 - 4, 2 * rectSide + 2 * offset - 1 - 2);
//				break;
//			case VIRTEX5:
//                gc.strokeRect(rectX, rectY - 4 * tileSize, rectSide - 1, 5 * rectSide + 3 * 2 * offset - 1);
//                gc.setStroke(color.darker());
//                gc.strokeRect(rectX+2, rectY-4 * tileSize + 2, rectSide - 5, 5 * rectSide + 3 * 2 * offset - 5);
//				break;
                case "VIRTEX6":
                case "ARTIX7":
                case "KINTEX7":
                case "VIRTEX7":
                    Rectangle rect0 = new Rectangle(rectX, rectY - 4 * tileSize, rectSide , 5 * (rectSide) + 5 * 2);
                    rect0.setStroke(color);
                    color = color.darker();
                    color = color.darker();
                    color = color.darker();
                    Rectangle rect1 = new Rectangle(rectX+4, (rectY-4 * tileSize) + 4, rectSide - 8, ((int)(2.5 * (rectSide-2))) + 30);
                    rect1.setStroke(color);
                    Rectangle rect2 = new Rectangle(rectX+4, (rectY-2 * tileSize) + 16, rectSide - 8, ((int)(2.5 * (rectSide-2))) + 5);
                    rect2.setStroke(color);
                    getChildren().addAll(rect0, rect1, rect2);
                    break;
            }
        }

        private void drawDSP(int rectX, int rectY, Color color){
            switch(device.getFamily().name()) {
//			case SPARTAN6:
//				painter.drawRect(rectX, rectY - 3 * tileSize, rectSide - 1, 4 * rectSide + 3 * 2 * offset - 1);
//				painter.setPen(color.darker());
//				painter.drawRect(rectX+2, rectY-3 * tileSize + 2, rectSide - 5, 4 * rectSide + 3 * 2 * offset - 5);
//				break;
//			case VIRTEX5:
                case "VIRTEX6":
                case "ARTIX7":
                case "KINTEX7":
                case "VIRTEX7":
                    Rectangle rect0 = new Rectangle(rectX, rectY - 4 * tileSize, rectSide , 5 * (rectSide) + 5 * 2);
                    rect0.setStroke(color);
                    color = color.darker();
                    color = color.darker();
                    color = color.darker();
                    Rectangle rect1 = new Rectangle(rectX+4, (rectY-4 * tileSize) + 4, rectSide - 8, ((int)(2.5 * (rectSide-2))) + 30);
                    rect1.setStroke(color);
                    Rectangle rect2 = new Rectangle(rectX+4, (rectY-2 * tileSize) + 16, rectSide - 8, ((int)(2.5 * (rectSide-2))) + 5);
                    rect2.setStroke(color);
                    getChildren().addAll(rect0, rect1, rect2);
                    break;
            }
        }

        private void drawSwitchBox(int rectX, int rectY, Color color){
            Rectangle rect = new Rectangle(rectX + rectSide / 6, rectY, 4 * rectSide / 6 - 1, rectSide - 1);
            rect.setStroke(color);
            getChildren().add(rect);

        }


        private void colorTile(int x, int y, Color color){
            Rectangle tile = new Rectangle(x * tileSize, y * tileSize,rectSide, rectSide);
            tile.setFill(color);
            this.getChildren().add(tile);
//            drawnTilesMap.put()
        }

        private void populateTileTypesToHide () {
            switch (device.getFamily().name()) {
//                case VIRTEX5:
//                    tileColumnTypesToHide.add(TileType.CFG_VBRK);
//                    tileColumnTypesToHide.add(TileType.CLKV);
//                    tileColumnTypesToHide.add(TileType.INT_BUFS_L);
//                    tileColumnTypesToHide.add(TileType.INT_BUFS_R);
//                    tileColumnTypesToHide.add(TileType.INT_BUFS_R_MON);
//                    tileColumnTypesToHide.add(TileType.INT_INTERFACE);
//                    tileColumnTypesToHide.add(TileType.IOI);
//                    tileRowTypesToHide.add(TileType.HCLK);
//                    tileRowTypesToHide.add(TileType.BRKH);
                case "VIRTEX6":
                    tileRowTypesToHide.add(Virtex6.TileTypes.HCLK);
                    tileRowTypesToHide.add(Virtex6.TileTypes.BRKH);
                    tileColumnTypesToHide.add(Virtex6.TileTypes.INT_INTERFACE);
                    tileColumnTypesToHide.add(Virtex6.TileTypes.VBRK);
                    break;
//                case SPARTAN6:
//                    tileColumnTypesToHide.add(TileType.INT_INTERFACE);
//                    tileColumnTypesToHide.add(TileType.VBRK);
//                    tileRowTypesToHide.add(TileType.HCLK_CLB_XL_CLE);
//                    tileRowTypesToHide.add(TileType.REGH_CLEXL_CLE);
//                    break;
            }
        }

        private HashMap<Tile, Integer> findReachability(Tile t, Integer hops){
            if(t==null || hops == null)return null;
            HashMap<Wire, Integer> level = new HashMap<>();
            HashMap<Tile, Integer> reachabilityMap = new HashMap<>();

            Queue<Wire> queue = new LinkedList<>();
            for(Wire wire : t.getWires()){
                for(Connection c : wire.getWireConnections()){
                    Wire w = c.getSinkWire();
                    queue.add(w);
                    level.put(w, 0);
                }
            }

            while(!queue.isEmpty()){
                Wire currWire = queue.poll();
                Integer i = reachabilityMap.get(currWire.getTile());
                if(i == null){
                    i = 1;
                    reachabilityMap.put(currWire.getTile(), i);
                }
                else{
                    reachabilityMap.put(currWire.getTile(), i+1);
                }
                Integer lev = level.get(currWire);
                if(lev < hops-1){
                    for(Connection c : currWire.getWireConnections()){
                        Wire w = c.getSinkWire();
                        queue.add(w);
                        level.put(w, lev+1);
                    }
                }
            }
            return reachabilityMap;
        }
        private void drawReachability(HashMap<Tile, Integer> map){
            for(Tile t : map.keySet()){
                int color = map.get(t)*16 > 255 ? 255 : map.get(t)*16;
                Color fillColor = Color.rgb(0, color, 0);
                int rectSide1 = rectSide * offset;
                //draw tile with new brighter color
                int number = map.get(t);
                int x = getDrawnTileX(t) * tileSize;
                int y = getDrawnTileY(t) * tileSize;
                Rectangle rect0 = new Rectangle(x, y, rectSide1, rectSide1);
                rect0.setFill(fillColor);
                getChildren().add(rect0);
                Text text = new Text(Integer.toString(number));

                if(number<100){
                    text.setFont(font3);
                    text.setX(x+4);
                    text.setY(y+(tileSize*2)/3);
                    text.setFill(Color.rgb(105,107,107));
                    text.setStroke(Color.rgb(105,107,107));
                    getChildren().add(text);
//                    gc.strokeText(text.getText(), x+4, y+(tileSize*2)/3);
                }else if(number < 1000){
                    text.setFont(font2);
                    text.setX(x+tileSize/8);
                    text.setY(y+(tileSize*2)/3);
                    getChildren().add(text);
                }else {
                    text.setFont(font1);
                    text.setX(x+2);
                    text.setY(y+(tileSize*2)/3);
                    getChildren().add(text);
                }
            }
        }

        private void menuReachability(){
            menuReachabilityClear();
            drawReachability(findReachability(reachabilityTile, hops));
            reachabilityDrawn = true;
        }

        private void menuReachabilityClear(){
            reachabilityDrawn = false;
            this.getChildren().clear();
            drawFPGAFabric(true);
//            gc.setStroke(Color.YELLOW); //set square to yellow selected box
//            gc.strokeRect(selX * tileSize, selY * tileSize, rectSide, rectSide);//draw square
            this.getChildren().add(highlight);
            displayWire = deviceBrowser.getDisplayWire();
            if(displayWire!=null)deviceBrowser.wireDoubleClicked(displayWire.getName());
            currentTiles.clear();
        }

        public void rightClickEvent(MouseEvent e) {
            //System.out.println("right click detected. NonPan");
            reachabilityTile = getTile(e);
            contextMenu.hide();
            contextMenu.show(this, e.getScreenX(), e.getScreenY());
        }

}
