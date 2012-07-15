#ImageAnalysisTools README

##Plugin installation instructions

Place the `ImageAnalysisTools_version.jar` file in the ImageJ/FIJI plugins directory.  Analysis tasks will appear in Plugins > ImageAnalysisTools.

###Version 5.0 and later

If using FIJI, and you wish to use jruby scripts for analysis, you will need to uninstall the (outdated) version of jruby that is included with FIJI.  To do this, either delete `jruby.jar` from the jars subdirectory of the FIJI installation, or go to Help > Update FIJI.  Then select "Advanced Mode," and "View installed plugins only."  Select `jars/jruby.jar` and click uninstall.

##Running as a standalone application

To run the standalone version from the command line:

`java -jar ImageAnalysisTools_version_standalone.jar`

You will likely need to add additional memory using the `-Xmx` flag to java; to enable 500 MB of memory, for instance, run:

`java -Xmx500M -jar ImageAnalysisTools_version_standalone.jar`

##Building from source

Building is handled by Apache Ant (http://ant.apache.org/).  To build the ImageJ plugin, run from the top-level directory containing build.xml and this README file:

ant plugin

The resulting jar file will be placed in the dist subdirectory, and will contain only those dependencies not provided with a standard FIJI installation.

To build a standalone jar for running from the command line, run:

ant dist

##Documentation

For documentation on using the software, see the pages linked at http://cjfuller.github.com/imageanalysistools

##License

ImageAnalysisTools is distributed under the MIT/X11 license.  See LICENSES/ImageAnalysisTools-license.txt for the full license text.

