package edu.psu.compbio.seqcode.projects.chexmix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProteinDNAInteractionModel: defines a collection of composite model components 
 * 
 *   Coordinates are 0-based, but the midpoint of the model is defined by the centerOffset variable. 
 * @author mahony
 *
 */
public class ProteinDNAInteractionModel {

	protected int modelWidth;
	protected int centerOffset;
	protected int numConditions; // number of conditions compositeProfiles are based on
	protected double[][] compositeWatson; //per-condition compositeProfile
	protected double[][] compositeCrick; //per-condition compositeProfile
	protected ChExMixConfig cmConfig;
	protected List<CompositeModelComponent> allComponents;
	protected List<CompositeModelComponent> XLComponents;
	protected Map<CompositeModelComponent, TagProbabilityDensity> XLComponentDensities;
	protected CompositeModelComponent CSComponent;
	protected CompositeModelComponent backgroundComponent;
	protected int numXLComponents; 
	
	/**
	 * Initialize a new ProteinDNAInteractionModel
	 * @param config
	 * @param width
	 * @param initXLdistrib
	 * @param initCSdistrib
	 * @param initBackDistrib
	 * @param noisePi
	 */
	public ProteinDNAInteractionModel(ChExMixConfig config, CompositeTagDistribution composite, TagProbabilityDensity initXLdistrib, TagProbabilityDensity initCSdistrib, 
			TagProbabilityDensity initBackDistrib, double noisePi){
		modelWidth = composite.getWinSize();
		centerOffset = composite.getCenterOffset();
		numConditions = composite.getNumConditions();
		compositeWatson = composite.getCompositeWatson();
		compositeCrick = composite.getCompositeCrick();
		cmConfig = config;
		
		//Background component
		backgroundComponent = new CompositeModelComponent(initBackDistrib, centerOffset, 0, "Back",  false, true);

		//ChIP-seq component
		CSComponent = new CompositeModelComponent(initCSdistrib, centerOffset, 1, "CS", false, true);
				
		//XL components
		XLComponents = new ArrayList<CompositeModelComponent>();
		XLComponentDensities = new HashMap<CompositeModelComponent, TagProbabilityDensity>();
		numXLComponents = (initCSdistrib.getInfluenceRange()/2)/cmConfig.getXLComponentSpacing();
		int xlPos = centerOffset-(numXLComponents*cmConfig.getXLComponentSpacing())/2;
		for(int i=0; i<numXLComponents; i++){
			TagProbabilityDensity XLTagDist = initXLdistrib.clone();
			CompositeModelComponent newComp = new CompositeModelComponent(XLTagDist, xlPos, i+2, "XL", true, true);
			XLComponents.add(newComp);
			XLComponentDensities.put(newComp, XLTagDist);
			xlPos += cmConfig.getXLComponentSpacing(); 
		}
		
		//All components
		allComponents = new ArrayList<CompositeModelComponent>();
		allComponents.add(backgroundComponent);
		allComponents.add(CSComponent);
		allComponents.addAll(XLComponents);
		
		//Set initial pi values
		setInitialPi(noisePi);
	}
	
	/**
	 * Initialize a ProteinDNAInteractionModel using existing components (e.g. when loading saved model)
	 * @param config
	 * @param width
	 * @param CSComp
	 * @param backComp
	 * @param XLComps
	 */
	public ProteinDNAInteractionModel(ChExMixConfig config, int width, int centerOff, int numConds, double[][] compWatson, double[][] compCrick, CompositeModelComponent CSComp, CompositeModelComponent backComp, List<CompositeModelComponent> XLComps){
		modelWidth = width;
		centerOffset = centerOff;
		numConditions = numConds;
		compositeWatson = compWatson;
		compositeCrick = compCrick;
		cmConfig = config;
		backgroundComponent = backComp;
		CSComponent = CSComp;
		XLComponents = XLComps;
		numXLComponents = XLComponents==null ? 0 : XLComponents.size();
		allComponents = new ArrayList<CompositeModelComponent>();
		allComponents.add(backgroundComponent);
		allComponents.add(CSComponent);
		XLComponentDensities = new HashMap<CompositeModelComponent, TagProbabilityDensity>();
		if(numXLComponents>0){
			allComponents.addAll(XLComponents);
			for(CompositeModelComponent xl : XLComponents){
				XLComponentDensities.put(xl, xl.getTagDistribution());
			}
		}
	}
	
	//Accessors
	public int getWidth(){return modelWidth;}
	public int getCenterOffset(){return centerOffset;}
	public TagProbabilityDensity getXLTagDistribution(CompositeModelComponent comp){return XLComponentDensities.get(comp);}
	public TagProbabilityDensity getCSTagDistribution(){return CSComponent.getTagDistribution();}
	public TagProbabilityDensity getBackTagDistribution(){return backgroundComponent.getTagDistribution();}
	public int getNumComponents(){return allComponents.size();}
	public List<CompositeModelComponent> getAllComponents(){return allComponents;}
	public List<CompositeModelComponent> getXLComponents(){return XLComponents;}
	public CompositeModelComponent getCSComponent(){return CSComponent;}
	public CompositeModelComponent getBackgroundComponent(){return backgroundComponent;}
	
	/**
	 * Return the non-zero components
	 * @return
	 */
	public List<CompositeModelComponent> getNonZeroComponents(){
		List<CompositeModelComponent> comps = new ArrayList<CompositeModelComponent>();
		for(CompositeModelComponent c : allComponents)
			if(c.isNonZero())
				comps.add(c);
		return comps;
	}
	
	/**
	 * Set the initial pi values for all components. 
	 * Assumes model is initialized. 
	 */
	protected void setInitialPi(double noisePi){
		backgroundComponent.setPi(noisePi);
		double bindingPi = 1-noisePi;
		double initCS = Math.max(bindingPi*cmConfig.INIT_CS_TO_XL_RATIO, cmConfig.MIN_CS_PI);
		CSComponent.setPi(initCS);
		for(CompositeModelComponent xl : XLComponents){
			double xoPi = bindingPi*(1-initCS)/(double)numXLComponents;
			xl.setPi(xoPi);
		}
	}
	
	
	/**
	 * toString
	 */
	public String toString(){
		Collections.sort(XLComponents);
		String out = "ProteinDNAInteractionModel:\n";
		for(CompositeModelComponent xl : XLComponents){
			if(xl.isNonZero())
				out = out+"\t"+xl.getIndex()+"\tXL:\t"+xl.toString(centerOffset)+"\n";
		}
		out = out+"\t"+CSComponent.getIndex()+"\tCS:\t"+CSComponent.toString(centerOffset)+"\n";
		out = out+"\t"+backgroundComponent.getIndex()+"\tBack:\t"+backgroundComponent.toString(centerOffset)+"\n";
		return out;
	}
	
	/**
	 * Save the entire model to a String
	 * @return
	 */
	public String saveString(){
		String out = "#ProteinDNAInteractionModel,"+modelWidth+","+centerOffset+","+numConditions+",\n";
		//Composite profiles
		for(int c=0; c<numConditions; c++){
			out = out+"#CompositeWatson,"+c+",";
			for(int x=0; x<modelWidth; x++)
				out=out+compositeWatson[c][x]+",";
			out = out+"\n";
			out = out+"#CompositeCrick,"+c+",";
			for(int x=0; x<modelWidth; x++)
				out=out+compositeCrick[c][x]+",";
			out = out+"\n";
		}
		//Tag distributions & Components
		// Here we need to either put all components out here (incl non-zero) or re-index non-zero components
		for(CompositeModelComponent comp : getAllComponents())
			out = out+comp.getTagDistribution().saveString();
		for(CompositeModelComponent comp : getAllComponents())
			out = out+comp.saveString();		
		return out;
	}
	
	/**
	 * Save the model to a file in the format specified by saveString()
	 * @param filename
	 */
	public void saveToFile(String filename){
		try {
			FileWriter fout = new FileWriter(filename);
			fout.write(this.saveString());
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load ProteinDNAInteractionModel using Strings of the same format used in saveString()
	 * @param con
	 * @param lines
	 * @return
	 */
	public static ProteinDNAInteractionModel load(ChExMixConfig con, List<String> lines){
		ProteinDNAInteractionModel mod = null;
		String[] bits = lines.get(0).split(",");
		if(bits.length!=4 || !bits[0].equals("#ProteinDNAInteractionModel")){
			System.err.println("ProteinDNAInteractionModel.load(): Unexpected format");
			System.exit(1);
		}else{
			Integer modWidth = new Integer(bits[1]);
			Integer centerOff = new Integer(bits[2]);
			Integer numConds = new Integer(bits[3]);
			CompositeModelComponent cs=null, back=null;
			List<CompositeModelComponent> xls = new ArrayList<CompositeModelComponent>();
			//Load composite profiles
			double[][] compWatson = new double[numConds][modWidth];
			double[][] compCrick = new double[numConds][modWidth];
			for(String l : lines){
				if(l.startsWith("#CompositeWatson")){
					String[] pieces = l.split(",");
					Integer cond = new Integer(pieces[1]);
					for(int s=2; s<pieces.length; s++)
						compWatson[cond][s-2]=new Double(pieces[s]);
				}
				if(l.startsWith("#CompositeCrick")){
					String[] pieces = l.split(",");
					Integer cond = new Integer(pieces[1]);
					for(int s=2; s<pieces.length; s++)
						compCrick[cond][s-2]=new Double(pieces[s]);
				}
			}
			//Load tag densities
			Map<Integer, TagProbabilityDensity> tagDensities = new HashMap<Integer, TagProbabilityDensity>();
			List<String> currTriplet = new ArrayList<String>(); 
			boolean record=false;
			for(String l : lines){
				if(l.startsWith("#TagProbabilityDensity")){
					currTriplet = new ArrayList<String>();
					record=true;
				}
				if(record){
					currTriplet.add(l);
					if(currTriplet.size()==3){
						record=false;
						TagProbabilityDensity tpd = TagProbabilityDensity.load(currTriplet);
						tagDensities.put(tpd.getIndex(), tpd);
					}
				}
			}
			//Load Components
			for(String l : lines){
				if(l.startsWith("#CompositeModelComponent")){
					CompositeModelComponent comp = CompositeModelComponent.load(l, tagDensities);
					if(comp.getLabel().equals("CS"))
						cs = comp;
					else if (comp.getLabel().equals("Back"))
						back = comp;
					else if (comp.getLabel().equals("XL"))
						xls.add(comp);
				}
			}
			//Make the model
			if(cs==null || back==null){
				System.err.println("ProteinDNAInteractionModel.load(): CS or Back components cannot be empty");
				System.exit(1);
			}
			mod = new ProteinDNAInteractionModel(con, modWidth, centerOff, numConds, compWatson, compCrick, cs, back, xls);
		}
		return mod;
	}
	
	/**
	 * Load model from File using format specified in saveString
	 * @param mod
	 * @return
	 */
	public static ProteinDNAInteractionModel loadFromFile(ChExMixConfig con, File mod){
		List<String> strings = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader( new FileReader (mod));
			String line = null;
		    while( ( line = reader.readLine() ) != null ) {
		        strings.add(line);
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return load(con, strings);
	}
}
