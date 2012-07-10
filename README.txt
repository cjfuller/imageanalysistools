ImageAnalysisTools README.txt

Building from source:

Building is handled by Apache Ant (http://ant.apache.org/).  To build the ImageJ plugin, run from the top-level directory containing this file:

ant plugin

The resulting jar file will be placed in the dist subdirectory, and will contain only those dependencies not provided with a standard FIJI installation.

To build a standalone jar for running from the command line, run:

ant dist

To run the standalone version from the command line (you will likely need the extra memory from the -Xmx flag):

java -Xmx500M -jar ImageAnalysisTools_version_standalone.jar

For documentation on using the software, see the pages at http://cjfuller.github.com/imageanalysistools


ImageAnalysisTools is distributed under the MIT/X11 license.  See LICENSES/ImageAnalysisTools-license.txt for the full license text.

