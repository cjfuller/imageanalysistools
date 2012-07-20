/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.meta;

import edu.stanford.cfuller.imageanalysistools.method.Method;
import edu.stanford.cfuller.imageanalysistools.image.ImageSet;

import java.io.BufferedReader;
import java.io.FileReader;

import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;

import edu.stanford.cfuller.imageanalysistools.util.FileHashCalculator;

/**
* AnalysisMetadata objects hold all the information on an analysis run including
* parameters used for input and the state of the parameters at output,
* input and output images, code versions, scripts, and quantification files.
* 
* @author Colin J. Fuller
*/
public class AnalysisMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 2395791L;
	
	public static final String LIBRARY_VERSION_RESOURCE_PATH = "edu/stanford/cfuller/imageanalysistools/resources/version_info.xml";
	
	/**
	* Holder for a hash calculcated on a file and a String that names the algorithm used.
	*/
	protected class FileHash {
		
		String algorithm;
		String value;
		
		public FileHash(String algorithm, String value) {this.algorithm = algorithm; this.value = value;}
		
		public String getAlgorithm() {return this.value;}
		public String getValue() {return this.value;}
		
	}
	
	private ParameterDictionary inputState;
	private ParameterDictionary outputState;
	
	private ImageSet inputImages;
	private ImageSet outputImages;
	
	private java.util.Map<String, FileHash> inputImageHashes;
	
	private java.util.List<String> outputFilenames;
	private java.util.Map<String, FileHash> outputFileHashes;
	
	private RubyScript script;
	
	private java.util.Date time;
	
	private Method method;
	
	private boolean hasRunPreviously;
		
	private String outputMetadataFile;
	
	/**
	* Creates an empty AnalysisMetadata object.
	*/
	public AnalysisMetadata() {
		this.inputImageHashes = new java.util.HashMap<String, FileHash>();
		this.outputFilenames = new java.util.ArrayList<String>();
		this.outputFileHashes = new java.util.HashMap<String, FileHash>();
		this.script = null;
	}
	
	/**
	* Makes a deep copy of this AnalysisMetadata object.
	* @return another AnalysisMetadata object that is a copy of this one.
	*/
	public AnalysisMetadata makeCopy() {
		return null; //TODO
	}
	
	/**
	* Sets the value of hashing the input Image stored in the named 
	* file with the specified algorithm.
	* 
	* @param filename The name of the image file
	* @param algorithm A string identifying the algorithm used to calculate the hash
	* @param hash the result of calculating the hash as a hexadecimal string
	*/
	public void setInputImageHash(String filename, String algorithm, String hash) {
		this.inputImageHashes.put(filename, new FileHash(algorithm, hash));
	}
	
	/**
	* Sets the ParameterDictionary used as input for the analysis.
	* @param in the ParameterDictionary initialized with the input parameters.
	*/
	public void setInputParameters(ParameterDictionary in) {this.inputState = in;}
	
	/**
	* Sets the ParameterDictionary to be stored as output from the analysis.
	* @param out the ParameterDictionary containing the output parameters.
	*/
	public void setOutputParameters(ParameterDictionary out) {this.outputState = out;}
	
	
	/**
	* Sets whether the AnalysisMetadata is loaded from a previous analysis run;
	* this will enable certain checks like matching code versions and file hashes.
	* 
	* @param has 	whether the analysis has information from a previous run
	*/
	public void setHasPreviousOutput(boolean has) {this.hasRunPreviously = has;}
	
	/**
	* Sets the images used for input to the analysis.
	* @param in an ImageSet containing the input images.
	*/
	public void setInputImages(ImageSet in) {this.inputImages = in;}
	
	/**
	* Sets the images to be stored as output from the analysis.
	* @param out an ImageSet containing the output images.
	*/
	public void setOutputImages(ImageSet out) {this.outputImages = out;}
	
	/**
	* Gets an XML string containing all the analysis metadata.
	*/
	public String toXML() {
		return "";
	}
	
	/**
	* @see #toXML
	*/
	public String toString() {
		return this.toXML();
	}
	
	/**
	* Gets an XML-formatted String containing the library version information,
	* including which commit in the git repository was used to build it.
	* @return a String containing an XML element called library with version and 
	* 				commit attributes
	*/
	public static String getLibraryVersionXMLString() {
		try {
			String resourcePath = AnalysisMetadata.class.getClassLoader().getResource(LIBRARY_VERSION_RESOURCE_PATH).toString();
			return (new BufferedReader(new FileReader(resourcePath))).readLine();
		} catch (java.io.IOException e) {
			edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities.getLogger().warning("Unable to retrieve library version information: " + e.getMessage());
			return null;
		}
	}
	
	/**
	* Gets the parameters to be used as input for the analysis.
	* @return a ParameterDictionary containing the input.
	*/
	public ParameterDictionary getInputParameters() {
		return this.inputState;
	}
	
	/**
	* Gets the parameters stored as output from the analysis.
	* @return a ParameterDictionary containing the output.
	*/
	public ParameterDictionary getOutputParameters() {
		return this.outputState;
	}
	
	/**
	* Gets the Images to be used as input for the analysis.
	* @return an ImageSet containing the input Images.
	*/
	public ImageSet getInputImages() {
		return this.inputImages;
	}
	
	/**
	* Gets the Images stored as output from the analysis.
	* @return an ImageSet containing the output Images; these may or may not be loaded already.
	*/
	public ImageSet getOutputImages() {
		return this.outputImages;
	}
	
	/**
	* Gets any ruby script associated with the analysis.
	* @return a RubyScript containing the script.
	*/
	public RubyScript getScript() {
		return this.script;
	}
	
	/**
	* Sets the ruby script to be run for the analysis.
	* @param r a RubyScript containing a valid script to be run.
	*/
	public void setScript(RubyScript r) {
		this.script = r;
	}
	
	/**
	* Queries whether the analysis has a script associated with it.
	* @return true if there is a script to run, false otherwise.
	*/
	public boolean hasScript() {
		return this.script != null;
	}
	
	/**
	* Gets the timestamp associated with the analysis.
	* @return a Date object containing the timestamp
	*/
	public java.util.Date getTime() {
		return this.time;
	}
	
	/**
	* Timestamps the AnalysisMetadata with the current time.
	*/
	public void timestamp() {
		this.time = new java.util.Date();
	}
	
	/**
	* Adds an output file containing some non-image data.
	*/
	public void addOutputFile(String filename) {
		this.outputFilenames.add(filename);
		try {
			this.outputFileHashes.put(filename, new FileHash(FileHashCalculator.ALG_DEFAULT, FileHashCalculator.calculateHash(FileHashCalculator.ALG_DEFAULT, filename)));
		} catch (java.io.IOException e) {
			edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities.getLogger().warning("Unable to calculate hash on image: " + filename + "\n" + e.getMessage());
		}
	}
	
	/**
	* Gets the names of all non-image output files.
	* @return a List containing the filenames.
	*/
	public java.util.List<String> getOutputFiles() {
		return this.outputFilenames;
	}
	
	/**
	* Gets the hash of the named output file using the algorithm obtained
	* by calling {@link #getOutputFileHashAlgorithm(String)}.
	* @param filename the filename of the output file.
	* @return the hash of that output file.
	*/
	public String getOutputFileHash(String filename) {
		FileHash fh =  this.outputFileHashes.get(filename);
		if (fh == null) return null;
		return fh.getValue();
	}
	
	/**
	* Gets the algorithm used to hash the named output file
	* @param filename the filename of the output file.
	* @return the algorithm used to hash that output file.
	*/
	public String getOutputFileHashAlgorithm(String filename) {
		FileHash fh =  this.outputFileHashes.get(filename);
		if (fh == null) return null;
		return fh.getAlgorithm();
	}
	
}

