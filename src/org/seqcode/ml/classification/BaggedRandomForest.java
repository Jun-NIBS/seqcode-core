package org.seqcode.ml.classification;



import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.RandomTree;
import weka.core.AdditionalMeasureProducer;
import weka.core.Aggregateable;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.PartitionGenerator;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.core.SerializationHelper;

/**
 <!-- globalinfo-start -->
 * Class for constructing a forest of random trees.<br/>
 * <br/>
 * For more information see: <br/>
 * <br/>
 * Leo Breiman (2001). Random Forests. Machine Learning. 45(1):5-32.
 * <p/>
 <!-- globalinfo-end -->
 * 
 <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;article{Breiman2001,
 *    author = {Leo Breiman},
 *    journal = {Machine Learning},
 *    number = {1},
 *    pages = {5-32},
 *    title = {Random Forests},
 *    volume = {45},
 *    year = {2001}
 * }
 * </pre>
 * <p/>
 <!-- technical-bibtex-end -->
 * 
 <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -I &lt;number of trees&gt;
 *  Number of trees to build.
 *  (default 100)</pre>
 * 
 * <pre> -K &lt;number of features&gt;
 *  Number of features to consider (&lt;1=int(log_2(#predictors)+1)).
 *  (default 0)</pre>
 * 
 * <pre> -S
 *  Seed for random number generator.
 *  (default 1)</pre>
 * 
 * <pre> -depth &lt;num&gt;
 *  The maximum depth of the trees, 0 for unlimited.
 *  (default 0)</pre>
 * 
 * <pre> -O
 *  Don't calculate the out of bag error.</pre>
 * 
 * <pre> -print
 *  Print the individual trees in the output</pre>
 * 
 * <pre> -num-slots &lt;num&gt;
 *  Number of execution slots.
 *  (default 1 - i.e. no parallelism)</pre>
 * 
 * <pre> -output-debug-info
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console</pre>
 * 
 * <pre> -do-not-check-capabilities
 *  If set, classifier capabilities are not checked before classifier is built
 *  (use with caution).</pre>
 * 
 <!-- options-end -->
 * 
 * @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 * @version $Revision: 11003 $
 */
public class BaggedRandomForest extends AbstractClassifier implements OptionHandler,
  Randomizable, WeightedInstancesHandler, AdditionalMeasureProducer,
  TechnicalInformationHandler, PartitionGenerator, Aggregateable<BaggedRandomForest> {
	
	
	public double[] getAttributeWeights()
	{
		if (m_bagger == null)
			throw new Error("Random forest not built yet");

		double[] sum = null;

		for (int i = 0; i < m_bagger.getNumIterations(); i++)
		{
			Classifier c = m_bagger.getClassifier(i);
			AttributeRandomTree t = (AttributeRandomTree) c;
			//			System.out.println(t);
			int[] numInstances = t.nodeNumInstances();
			if (sum == null)
				sum = new double[numInstances.length];
			for (int j = 0; j < numInstances.length; j++)
				sum[j] += numInstances[j];
		}

		double max = -1;
		for (int i = 0; i < sum.length; i++)
			if (sum[i] > max)
				max = sum[i];
		for (int i = 0; i < sum.length; i++)
			sum[i] /= max;
		return sum;
	}
	
	public String[] getAttributes()
	{
		return ((AttributeRandomTree) m_bagger.getClassifier(0)).getAttributes();
	}

  /** for serialization */
  static final long serialVersionUID = 1116839470751428698L;

  /** Number of trees in forest. */
  protected int m_numTrees = 100;
  
  /** The size of each bag sample, as a percentage of the training size */
  protected int m_BagSizePercent = 100;
  
  /** Whether to calculate the out of bag error */
  protected boolean m_CalcOutOfBag = false;

  /**
   * Number of features to consider in random feature selection. If less than 1
   * will use int(log_2(M)+1) )
   */
  protected int m_numFeatures = 0;

  /** The random seed. */
  protected int m_randomSeed = 1;

  /** Final number of features that were considered in last build. */
  protected int m_KValue = 0;

  /** The bagger. */
  protected AttributeBagging m_bagger = null;

  /** The maximum depth of the trees (0 = unlimited) */
  protected int m_MaxDepth = 0;

  /** The number of threads to have executing at any one time */
  protected int m_numExecutionSlots = 1;

  /** Print the individual trees in the output */
  protected boolean m_printTrees = false;

  /** Don't calculate the out of bag error */
  protected boolean m_dontCalculateOutOfBagError;

  /**
   * Returns a string describing classifier
   * 
   * @return a description suitable for displaying in the explorer/experimenter
   *         gui
   */
  public String globalInfo() {

    return "Class for constructing a forest of random trees.\n\n"
      + "For more information see: \n\n" + getTechnicalInformation().toString();
  }

  /**
   * Returns an instance of a TechnicalInformation object, containing detailed
   * information about the technical background of this class, e.g., paper
   * reference or book this class is based on.
   * 
   * @return the technical information about this class
   */
  @Override
  public TechnicalInformation getTechnicalInformation() {
    TechnicalInformation result;

    result = new TechnicalInformation(Type.ARTICLE);
    result.setValue(Field.AUTHOR, "Leo Breiman");
    result.setValue(Field.YEAR, "2001");
    result.setValue(Field.TITLE, "Random Forests");
    result.setValue(Field.JOURNAL, "Machine Learning");
    result.setValue(Field.VOLUME, "45");
    result.setValue(Field.NUMBER, "1");
    result.setValue(Field.PAGES, "5-32");

    return result;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String numTreesTipText() {
    return "The number of trees to be generated.";
  }

  /**
   * Get the value of numTrees.
   * 
   * @return Value of numTrees.
   */
  public int getNumTrees() {

    return m_numTrees;
  }

  /**
   * Set the value of numTrees.
   * 
   * @param newNumTrees Value to assign to numTrees.
   */
  public void setNumTrees(int newNumTrees) {

    m_numTrees = newNumTrees;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String numFeaturesTipText() {
    return "The number of attributes to be used in random selection (see RandomTree).";
  }

  /**
   * Get the number of features used in random selection.
   * 
   * @return Value of numFeatures.
   */
  public int getNumFeatures() {

    return m_numFeatures;
  }

  /**
   * Set the number of features to use in random selection.
   * 
   * @param newNumFeatures Value to assign to numFeatures.
   */
  public void setNumFeatures(int newNumFeatures) {

    m_numFeatures = newNumFeatures;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String seedTipText() {
    return "The random number seed to be used.";
  }

  /**
   * Set the seed for random number generation.
   * 
   * @param seed the seed
   */
  @Override
  public void setSeed(int seed) {

    m_randomSeed = seed;
  }

  /**
   * Gets the seed for the random number generations
   * 
   * @return the seed for the random number generation
   */
  @Override
  public int getSeed() {

    return m_randomSeed;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String maxDepthTipText() {
    return "The maximum depth of the trees, 0 for unlimited.";
  }

  /**
   * Get the maximum depth of trh tree, 0 for unlimited.
   * 
   * @return the maximum depth.
   */
  public int getMaxDepth() {
    return m_MaxDepth;
  }

  /**
   * Set the maximum depth of the tree, 0 for unlimited.
   * 
   * @param value the maximum depth.
   */
  public void setMaxDepth(int value) {
    m_MaxDepth = value;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String printTreesTipText() {
    return "Print the individual trees in the output";
  }

  /**
   * Set whether to print the individual ensemble trees in the output
   * 
   * @param print true if the individual trees are to be printed
   */
  public void setPrintTrees(boolean print) {
    m_printTrees = print;
  }

  /**
   * Get whether to print the individual ensemble trees in the output
   * 
   * @return true if the individual trees are to be printed
   */
  public boolean getPrintTrees() {
    return m_printTrees;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String dontCalculateOutOfBagErrorTipText() {
    return "If true, then the out of bag error is not computed";
  }

  /**
   * Set whether to turn off the calculation of out of bag error
   * 
   * @param b true to turn off the calculation of out of bag error
   */
  public void setDontCalculateOutOfBagError(boolean b) {
    m_dontCalculateOutOfBagError = b;
  }

  /**
   * Get whether to turn off the calculation of out of bag error
   * 
   * @return true to turn off the calculation of out of bag error
   */
  public boolean getDontCalculateOutOfBagError() {
    return m_dontCalculateOutOfBagError;
  }

  /**
   * Gets the out of bag error that was calculated as the classifier was built.
   * 
   * @return the out of bag error
   */
  public double measureOutOfBagError() {

    if (m_bagger != null && !m_dontCalculateOutOfBagError) {
      return m_bagger.measureOutOfBagError();
    } else {
      return Double.NaN;
    }
  }

  /**
   * Set the number of execution slots (threads) to use for building the members
   * of the ensemble.
   * 
   * @param numSlots the number of slots to use.
   */
  public void setNumExecutionSlots(int numSlots) {
    m_numExecutionSlots = numSlots;
  }

  /**
   * Get the number of execution slots (threads) to use for building the members
   * of the ensemble.
   * 
   * @return the number of slots to use
   */
  public int getNumExecutionSlots() {
    return m_numExecutionSlots;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String numExecutionSlotsTipText() {
    return "The number of execution slots (threads) to use for "
      + "constructing the ensemble.";
  }

  /**
   * Returns an enumeration of the additional measure names.
   * 
   * @return an enumeration of the measure names
   */
  @Override
  public Enumeration<String> enumerateMeasures() {

    Vector<String> newVector = new Vector<String>(1);
    newVector.addElement("measureOutOfBagError");
    return newVector.elements();
  }

  /**
   * Returns the value of the named measure.
   * 
   * @param additionalMeasureName the name of the measure to query for its value
   * @return the value of the named measure
   * @throws IllegalArgumentException if the named measure is not supported
   */
  @Override
  public double getMeasure(String additionalMeasureName) {

    if (additionalMeasureName.equalsIgnoreCase("measureOutOfBagError")) {
      return measureOutOfBagError();
    } else {
      throw new IllegalArgumentException(additionalMeasureName
        + " not supported (RandomForest)");
    }
  }

  /**
   * Returns an enumeration describing the available options.
   * 
   * @return an enumeration of all the available options
   */
  @Override
  public Enumeration<Option> listOptions() {

    Vector<Option> newVector = new Vector<Option>();

    newVector.addElement(new Option("\tNumber of trees to build.\n\t(default 100)", "I", 1,
      "-I <number of trees>"));

    newVector.addElement(new Option(
      "\tNumber of features to consider (<1=int(log_2(#predictors)+1)).\n\t(default 0)", "K", 1,
      "-K <number of features>"));

    newVector.addElement(new Option("\tSeed for random number generator.\n"
      + "\t(default 1)", "S", 1, "-S"));

    newVector.addElement(new Option(
      "\tThe maximum depth of the trees, 0 for unlimited.\n" + "\t(default 0)",
      "depth", 1, "-depth <num>"));

    newVector.addElement(new Option("\tDon't calculate the out of bag error.",
      "O", 0, "-O"));

    newVector.addElement(new Option(
      "\tPrint the individual trees in the output", "print", 0, "-print"));

    newVector.addElement(new Option("\tNumber of execution slots.\n"
      + "\t(default 1 - i.e. no parallelism)", "num-slots", 1,
      "-num-slots <num>"));

    newVector.addAll(Collections.list(super.listOptions()));

    return newVector.elements();
  }

  /**
   * Gets the current settings of the forest.
   * 
   * @return an array of strings suitable for passing to setOptions()
   */
  @Override
  public String[] getOptions() {
    Vector<String> result = new Vector<String>();

    result.add("-I");
    result.add("" + getNumTrees());

    result.add("-K");
    result.add("" + getNumFeatures());

    result.add("-S");
    result.add("" + getSeed());

    if (getMaxDepth() > 0) {
      result.add("-depth");
      result.add("" + getMaxDepth());
    }

    if (getDontCalculateOutOfBagError()) {
      result.add("-O");
    }

    if (m_printTrees) {
      result.add("-print");
    }

    result.add("-num-slots");
    result.add("" + getNumExecutionSlots());

    Collections.addAll(result, super.getOptions());

    return result.toArray(new String[result.size()]);
  }

  /**
   * Parses a given list of options.
   * <p/>
   * 
   <!-- options-start -->
   * Valid options are: <p/>
   * 
   * <pre> -I &lt;number of trees&gt;
   *  Number of trees to build.
   *  (default 100)</pre>
   * 
   * <pre> -K &lt;number of features&gt;
   *  Number of features to consider (&lt;1=int(log_2(#predictors)+1)).
   *  (default 0)</pre>
   * 
   * <pre> -S
   *  Seed for random number generator.
   *  (default 1)</pre>
   * 
   * <pre> -depth &lt;num&gt;
   *  The maximum depth of the trees, 0 for unlimited.
   *  (default 0)</pre>
   * 
   * <pre> -O
   *  Don't calculate the out of bag error.</pre>
   * 
   * <pre> -print
   *  Print the individual trees in the output</pre>
   * 
   * <pre> -num-slots &lt;num&gt;
   *  Number of execution slots.
   *  (default 1 - i.e. no parallelism)</pre>
   * 
   * <pre> -output-debug-info
   *  If set, classifier is run in debug mode and
   *  may output additional info to the console</pre>
   * 
   * <pre> -do-not-check-capabilities
   *  If set, classifier capabilities are not checked before classifier is built
   *  (use with caution).</pre>
   * 
   <!-- options-end -->
   * 
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  @Override
  public void setOptions(String[] options) throws Exception {
    String tmpStr;

    tmpStr = Utils.getOption('I', options);
    if (tmpStr.length() != 0) {
      m_numTrees = Integer.parseInt(tmpStr);
    } else {
      m_numTrees = 100;
    }
    
    m_CalcOutOfBag = Utils.getFlag('O', options);
    
    tmpStr = Utils.getOption('P', options);
    if (tmpStr.length() != 0) {
      m_BagSizePercent = Integer.parseInt(tmpStr);
    } else {
      m_BagSizePercent = 100;
    }

    tmpStr = Utils.getOption('K', options);
    if (tmpStr.length() != 0) {
      m_numFeatures = Integer.parseInt(tmpStr);
    } else {
      m_numFeatures = 0;
    }

    tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0) {
      setSeed(Integer.parseInt(tmpStr));
    } else {
      setSeed(1);
    }

    tmpStr = Utils.getOption("depth", options);
    if (tmpStr.length() != 0) {
      setMaxDepth(Integer.parseInt(tmpStr));
    } else {
      setMaxDepth(0);
    }

    setDontCalculateOutOfBagError(Utils.getFlag('O', options));

    setPrintTrees(Utils.getFlag("print", options));

    tmpStr = Utils.getOption("num-slots", options);
    if (tmpStr.length() > 0) {
      setNumExecutionSlots(Integer.parseInt(tmpStr));
    } else {
      setNumExecutionSlots(1);
    }

    super.setOptions(options);

    Utils.checkForRemainingOptions(options);
  }

  /**
   * Returns default capabilities of the classifier.
   * 
   * @return the capabilities of this classifier
   */
  @Override
  public Capabilities getCapabilities() {
    return new AttributeRandomTree().getCapabilities();
  }

  /**
   * Builds a classifier for a set of instances.
   * 
   * @param data the instances to train the classifier with
   * @throws Exception if something goes wrong
   */
  @Override
  public void buildClassifier(Instances data) throws Exception {

    // can classifier handle the data?
    getCapabilities().testWithFail(data);

    // remove instances with missing class
    data = new Instances(data);
    data.deleteWithMissingClass();

    m_bagger = new AttributeBagging();

    // RandomTree implements WeightedInstancesHandler, so we can
    // represent copies using weights to achieve speed-up.
    m_bagger.setRepresentCopiesUsingWeights(true);

    AttributeRandomTree rTree = new AttributeRandomTree();

    // set up the random tree options
    m_KValue = m_numFeatures;
    if (m_KValue < 1) {
      m_KValue = (int) Utils.log2(data.numAttributes() - 1) + 1;
    }
    rTree.setKValue(m_KValue);
    rTree.setMaxDepth(getMaxDepth());
    rTree.setDoNotCheckCapabilities(true);

    // set up the bagger and build the forest
    m_bagger.setBagSizePercent(m_BagSizePercent);
    m_bagger.setCalcOutOfBag(m_CalcOutOfBag);
    m_bagger.setClassifier(rTree);
    m_bagger.setSeed(m_randomSeed);
    m_bagger.setNumIterations(m_numTrees);
    m_bagger.setNumExecutionSlots(m_numExecutionSlots);
    m_bagger.buildClassifier(data);
  }

  /**
   * Returns the class probability distribution for an instance.
   * 
   * @param instance the instance to be classified
   * @return the distribution the forest generates for the instance
   * @throws Exception if computation fails
   */
  @Override
  public double[] distributionForInstance(Instance instance) throws Exception {

    return m_bagger.distributionForInstance(instance);
  }

  /**
   * Outputs a description of this classifier.
   * 
   * @return a string containing a description of the classifier
   */
  @Override
  public String toString() {

    if (m_bagger == null) {
      return "Random forest not built yet";
    } else {
      StringBuffer temp = new StringBuffer();
      temp.append("Random forest of "
        + m_numTrees
        + " trees, each constructed while considering "
        + m_KValue
        + " random feature"
        + (m_KValue == 1 ? "" : "s")
        + ".\n"
        + (!getDontCalculateOutOfBagError() ? "Out of bag error: "
          + Utils.doubleToString(m_bagger.measureOutOfBagError(), 4) : "")
        + "\n"
        + (getMaxDepth() > 0 ? ("Max. depth of trees: " + getMaxDepth() + "\n")
          : ("")) + "\n");
      if (m_printTrees) {
        temp.append(m_bagger.toString());
      }
      return temp.toString();
    }
  }

  /**
   * Builds the classifier to generate a partition.
   */
  @Override
  public void generatePartition(Instances data) throws Exception {

    buildClassifier(data);
  }

  /**
   * Computes an array that indicates leaf membership
   */
  @Override
  public double[] getMembershipValues(Instance inst) throws Exception {

    return m_bagger.getMembershipValues(inst);
  }

  /**
   * Returns the number of elements in the partition.
   */
  @Override
  public int numElements() throws Exception {

    return m_bagger.numElements();
  }

  /**
   * Returns the revision string.
   * 
   * @return the revision
   */
  @Override
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 11003 $");
  }

  /**
   * Main method for this class.
   * 
   * @param argv the options
   */
  public static void main(String[] argv) {
    runClassifier(new BaggedRandomForest(), argv);
  }

  @Override
  public BaggedRandomForest aggregate(BaggedRandomForest toAggregate) throws Exception {
    m_bagger.aggregate(toAggregate.m_bagger);
    return this;
  }

  @Override
  public void finalizeAggregation() throws Exception {
    m_bagger.finalizeAggregation();
  }
}
