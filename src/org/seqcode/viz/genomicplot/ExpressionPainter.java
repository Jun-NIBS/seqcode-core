package org.seqcode.viz.genomicplot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.seqcode.gseutils.ArgParser;
import org.seqcode.gseutils.Pair;
import org.seqcode.viz.paintable.AbstractPaintable;
import org.seqcode.viz.paintable.PaintableFrame;


public class ExpressionPainter extends AbstractPaintable{

	private MultidataSpatialPaintable painter;
	private static PaintableFrame plotter;
	private static int deftScreenSizeX=800, deftScreenSizeY=1200;
	private int screenSizeX, screenSizeY;
	ArrayList<Pair<String, ArrayList<Double>>> expression = new ArrayList<Pair<String, ArrayList<Double>>>();
	private final int geneArcWidth=2, geneArcHeight=2;
	private final int expBoxHeight=20, expBoxWidth = 40;
	private final int clusterSpacing = 120;
	private final int colorbarHeight=15, colorbarWidth=120;
	private final double maxExp=4.0, midExp=0, minExp=-4;
	private Color expMaxColor = Color.yellow;
	private Color expMidColor = Color.black;
	private Color expMinColor = Color.blue;
	//private Color expMaxColor = new Color(18,54,36);
	//private Color expMidColor = Color.white;
	//private Color expMinColor = Color.white;
	private Color geneColor = Color.gray;
	private final int topBorder=50, bottomBorder=50;
	private final int leftBorder=25, rightBorder=25;
	private int topBound, bottomBound, leftBound, rightBound, baseLine, midLine;
	private static ArrayList<String> timeLabels= new ArrayList<String>();
	private static int numExpr =4;
	private final int geneBoxHeight=22, geneBoxWidth=(numExpr*expBoxWidth)+4;
		
	public static void main(String[] args) {
		ArgParser ap = new ArgParser(args);
        if(!ap.hasKey("data")) { 
            System.err.println("Usage:\n " +
                               "Expression Painter " +
                               "--data <file name> ");
            return;
        }
        String dfile = ap.getKeyValue("data");
        ArrayList<Pair<String, ArrayList<Double>>> expr = loadFile(dfile);
			
        //Paint the picture
        ExpressionPainter painter = new ExpressionPainter(expr);
		plotter = new PaintableFrame("Expression Painter", painter);
		plotter.setSize(deftScreenSizeX, deftScreenSizeY);
		plotter.setVisible(true);
		plotter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}
	
	public ExpressionPainter(ArrayList<Pair<String, ArrayList<Double>>> x){
		expression = x;
	}
	
	public void paintItem (Graphics g, int x1, int y1, int x2, int y2){
		Graphics2D g2d = (Graphics2D)g;
		FontMetrics metrics = g2d.getFontMetrics();
		screenSizeX = x2-x1;
		screenSizeY = y2-y1;
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, screenSizeX, screenSizeY);
		topBound = topBorder+140;
		bottomBound = screenSizeY-bottomBorder;
		leftBound = leftBorder+30;
		rightBound = screenSizeX-rightBorder;
		baseLine = (topBound+bottomBound)/2;
		midLine = (leftBound+rightBound)/2;
		int xPos = (leftBound+rightBound)/2;
		
		//Background rounded boxes
		int j=0;
		for(Pair <String, ArrayList<Double>> p : expression){
			String currName = p.car();
			ArrayList<Double> evals = p.cdr();
			boolean found =false;
			int boxX = xPos-(geneBoxWidth/2);
			int boxY = topBound+geneBoxHeight*(j-1);
			g2d.setColor(geneColor);
			g2d.setStroke(new BasicStroke(1.0f));
			g2d.fillRoundRect(boxX, boxY, geneBoxWidth, geneBoxHeight, geneArcWidth, geneArcHeight);
			g2d.setColor(Color.darkGray);
			g2d.drawRoundRect(boxX, boxY, geneBoxWidth, geneBoxHeight, geneArcWidth, geneArcHeight);
			
			//Default expression boxes
			int eY = boxY+(geneBoxHeight-expBoxHeight)/2;
			for(int e=0; e<numExpr; e++){
				int eX = xPos - ((numExpr*expBoxWidth)/2) +(e*expBoxWidth);
				g2d.setColor(Color.lightGray);
				g2d.fillRect(eX, eY,expBoxWidth, expBoxHeight);
				g2d.setColor(Color.white);
				g2d.setStroke(new BasicStroke(1.0f));
				g2d.drawRect(eX, eY,expBoxWidth, expBoxHeight);
			}
			
			//Find appropriate expression values
			if(evals != null){
				for(int e=0; e<numExpr; e++){
					Double v = evals.get(e);
					Color currCol = expColor(v);
					int eX = xPos - ((numExpr*expBoxWidth)/2) +(e*expBoxWidth);
					g2d.setColor(currCol);
					g2d.fillRect(eX, eY,expBoxWidth, expBoxHeight);
					g2d.setColor(Color.white);
					g2d.setStroke(new BasicStroke(1.0f));
					g2d.drawRect(eX, eY,expBoxWidth, expBoxHeight);					
				}
			}
			//Draw the String 
			g2d.setColor(Color.black);
			g2d.setFont(new Font("Ariel", Font.PLAIN, 16));
			g2d.drawString(currName, boxX+geneBoxWidth+10,boxY+(geneBoxHeight/2)+8);
			j++;
		}
		
		/*
		g2d.setColor(Color.black);
		g2d.setFont(new Font("Ariel", Font.BOLD, 20));
		metrics = g2d.getFontMetrics();
		int xPos = (midLine-(5*clusterSpacing/2));
		g2d.drawString("Paralog", xPos-(metrics.stringWidth("Paralog")/2), topBound-(geneBoxHeight/2));
		for(int j=1; j<=13; j++){
			g2d.setFont(new Font("Ariel", Font.PLAIN, 20));
			//Vert g2d.drawString(String.format("%d",j), xPos, topBound+(geneBoxHeight*(j-1))+(geneBoxHeight/2));
			AffineTransform oldtrans = g2d.getTransform();
	        AffineTransform newtrans = new AffineTransform();
	        newtrans.translate(xPos, topBound+(geneBoxHeight*(j-1))+(geneBoxHeight/2));
	        newtrans.rotate(Math.toRadians(90));
	        g2d.setTransform(newtrans);
	        g2d.drawString(String.format("%d",j), 0,0);
	        g2d.setTransform(oldtrans);
		}*/
			
		//Colorbar
		drawExpColorBar(g2d, midLine-(colorbarWidth/2), topBound-160);
		//Time Labels
		/*
		xPos = midLine;
		int yPos = topBound+geneBoxHeight*(j);
		g2d.setColor(Color.black);
		AffineTransform oldtrans = g2d.getTransform();
        AffineTransform newtrans = new AffineTransform();
        newtrans.translate(xPos, yPos);
        //Vert
        //newtrans.rotate(Math.toRadians(-90));
        newtrans.rotate(Math.toRadians(90));
        g2d.setTransform(newtrans);
        g2d.setFont(new Font("Ariel", Font.PLAIN, 16));
        metrics = g2d.getFontMetrics();
        g2d.drawString("Day",-1*metrics.stringWidth("Day")/2,-1*(geneBoxWidth/2));
        g2d.setFont(new Font("Ariel", Font.PLAIN, 16));
        metrics = g2d.getFontMetrics();
        for(int e=0; e<numExpr; e++){
        	//Vert			int etY = (e*expBoxWidth)-((numExpr*expBoxWidth)/2)+(expBoxWidth/2)+(metrics.getHeight()/2);
        	int etY = ((numExpr-e-1)*expBoxWidth)-((numExpr*expBoxWidth)/2)+(expBoxWidth/2)+(metrics.getHeight()/2);
			String text = timeLabels.get(e);
			g2d.drawString(text,0,etY);
		}
        g2d.setTransform(oldtrans);
		*/
	}
	
	private Color expColor(double v){
		Color c;
		if(v>midExp){
			Color maxColor = expMaxColor;
			Color minColor = expMidColor;
			double sVal = v>maxExp ? 1 : (v-midExp)/(maxExp-midExp);
			
			int red = (int)(maxColor.getRed() * sVal + minColor.getRed() * (1 - sVal));
		    int green = (int)(maxColor.getGreen() * sVal + minColor.getGreen() * (1 - sVal));
		    int blue = (int)(maxColor.getBlue() *sVal + minColor.getBlue() * (1 - sVal));
		    c = new Color(red, green, blue);
		}else{
			Color maxColor = expMidColor;
			Color minColor = expMinColor;
			double sVal = v<minExp ? 0 : ((midExp-minExp)-(midExp-v))/(midExp-minExp);
			int red = (int)(maxColor.getRed() * sVal + minColor.getRed() * (1 - sVal));
	        int green = (int)(maxColor.getGreen() * sVal + minColor.getGreen() * (1 - sVal));
	        int blue = (int)(maxColor.getBlue() *sVal + minColor.getBlue() * (1 - sVal));
	        c = new Color(red, green, blue);				
		}
		return(c);
	}
	private void drawExpColorBar(Graphics2D g2d, int x, int y){
		//Draw colors 
		GradientPaint colorbar = new GradientPaint(x, y, expMinColor, x+colorbarWidth/2, y, expMidColor, false);
		g2d.setPaint(colorbar);
		g2d.fillRect(x, y, colorbarWidth/2, colorbarHeight);
		colorbar = new GradientPaint(x+colorbarWidth/2, y, expMidColor, x+colorbarWidth, y, expMaxColor, false);
		g2d.setPaint(colorbar);
		g2d.fillRect(x+(colorbarWidth/2), y, colorbarWidth/2, colorbarHeight);
		
		//Draw border
		g2d.setPaint(Color.black);
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(1.0f));
		g2d.drawRect(x, y, colorbarWidth, colorbarHeight);
		
		//Legend
		g2d.setFont(new Font("Ariel", Font.PLAIN, 12));
		FontMetrics metrics = g2d.getFontMetrics();
		int textY = y+colorbarHeight+ (metrics.getHeight());
		g2d.drawString(String.format("%.0f",midExp), x+(colorbarWidth/2)-(metrics.stringWidth(String.format("%.0f",midExp))/2), textY);
		g2d.drawString(String.format("%.1f",minExp), x-(metrics.stringWidth(String.format(".1f",minExp))/2), textY);
		g2d.drawString(String.format("%.1f",maxExp), x+colorbarWidth-(metrics.stringWidth(String.format(".1f",maxExp))/2), textY);
		
		//Title
		g2d.setFont(new Font("Ariel", Font.ITALIC, 12));
		metrics = g2d.getFontMetrics();
		g2d.drawString("log-foldchange", x+(colorbarWidth/2)-(metrics.stringWidth("log-foldchange")/2), y- (metrics.getHeight())/2);
	}
	private static ArrayList<Pair<String, ArrayList<Double>>> loadFile(String inF){
		ArrayList<Pair<String, ArrayList<Double>>> ex = new ArrayList<Pair<String, ArrayList<Double>>>();
		try{
			File aFile = new File(inF);
			if(aFile.isFile()){
				BufferedReader reader;
				reader = new BufferedReader(new FileReader(aFile));
				String line;
				//Labels
				line= reader.readLine();
				String [] tokens = line.split("\\s+");
				for(int i=2; i<tokens.length; i++){
					timeLabels.add(tokens[i]);
				}numExpr = timeLabels.size();
				//Expression
				while((line= reader.readLine())!=null){
					tokens = line.split("\\s+");
					String name = tokens[0];
					ArrayList<Double> vals = new ArrayList<Double>();
					for(int i=2; i<tokens.length; i++){
						vals.add(new Double(tokens[i]));
					}
					ex.add(new Pair(name, vals));
				}
				reader.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return(ex);	
	}
	
	public static void drawArrow(Graphics2D g2d, int xCenter, int yCenter, int x, int y, float stroke) {
		double aDir=Math.atan2(xCenter-x,yCenter-y);
		g2d.drawLine(x,y,xCenter,yCenter);
		g2d.setStroke(new BasicStroke(stroke));					// make the arrow head solid even if dash pattern has been specified
		Polygon tmpPoly=new Polygon();
		int i1=12+(int)(stroke*2);
		int i2=6+(int)stroke;							// make the arrow head the same size regardless of the length length
		tmpPoly.addPoint(x,y);							// arrow tip
		tmpPoly.addPoint(x+xCor(i1,aDir+.5),y+yCor(i1,aDir+.5));
		tmpPoly.addPoint(x+xCor(i2,aDir),y+yCor(i2,aDir));
		tmpPoly.addPoint(x+xCor(i1,aDir-.5),y+yCor(i1,aDir-.5));
		tmpPoly.addPoint(x,y);							// arrow tip
		g2d.drawPolygon(tmpPoly);
		g2d.fillPolygon(tmpPoly);						// remove this line to leave arrow head unpainted
   	}
    private static int yCor(int len, double dir) {return (int)(len * Math.cos(dir));}
	private static int xCor(int len, double dir) {return (int)(len * Math.sin(dir));}
}
