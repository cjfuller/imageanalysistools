#ImageAnalysisTools README

ImageAnalysisTools is a java library developed for processing and quantifying biological microscopy images that focuses on saving developer time by easing rapid development of application-specific analysis tools and on keeping a detailed record of all analysis to guarantee reproducibility.

##Plugin installation instructions

Place the `imageanalysistools-version-plugin.jar` file in the ImageJ/FIJI plugins directory.  Analysis tasks will appear in Plugins > ImageAnalysisTools.

If using FIJI, and you wish to use jruby scripts for analysis, you will need to uninstall the (outdated) version of jruby that is included with FIJI.  To do this, either delete `jruby.jar` from the jars subdirectory of the FIJI installation, or go to Help > Update FIJI.  Then select "Advanced Mode," and "View installed plugins only."  Select `jars/jruby.jar` and click uninstall.

##Building from source

Building is handled by Apache Maven (http://maven.apache.org/).  To build the plugin, standalone, and dependency-free jars, run from the top-level directory containing pom.xml and this README file:

`mvn package`

The resulting jar file will be placed in the target subdirectory.  The jar labeled plugin will contain only those dependencies not included in a standard fiji installation.  The jar labeled standalone will contain all dependencies.  The jar with no additional label contains no dependencies.

Versions post-5.1.0 enable tracking which commit to the git repository was used to run analysis.  If you checked out the source using git and want to compile with this information, supply a `versioninfo.commit_id` property to maven when building.  To automatically use the current commit information from git when building, use the option:

`-Dversioninfo.commit_id=`git log -n 1 --format="%H"`


##Running as a standalone application

To run the standalone version from the command line:

`java -jar imageanalysistools-version-standalone.jar`

You will likely need to add additional memory using the `-Xmx` flag to java; to enable 500 MB of memory, for instance, run:

`java -Xmx500M -jar imageanalysistools-version-standalone.jar`



##Documentation

For documentation on using the software, see the pages linked at http://cjfuller.github.com/imageanalysistools

##License

ImageAnalysisTools is distributed under the MIT/X11 license.  See LICENSES/ImageAnalysisTools-license.txt for the full license text.

Several third-party libraries are included in the distributed jar file.  See LICENSES/README_LICENSES.txt for more information on their licenses.

