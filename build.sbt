val DepVersionLoci = "4.4.5"
val DepVersionIJ = "2.0.0-beta4"
val DepVersionImgLib = "2.0.0-beta5"
val DepVersionCommonsMath = "3.1.1"
val DepVersionJRuby = "1.7.0"
val VersionInfoCommitId = ("git log -1 --pretty=format:%H" !!).trim()
val VersionInfoFilename = "edu/stanford/cfuller/imageanalysistools/resources/version_info.xml"

val Version = "5.1.5-pre.0"

lazy val root = (project in file(".")).settings(
  name := "imageanalysistools",
  version := Version,
  scalaVersion := "2.11.8",
  resolvers ++= Seq(
    "imagej" at "http://maven.imagej.net/content/repositories/public"
  ),
  libraryDependencies ++= Seq(
    "junit" % "junit" % "4.10" % "test",
    "com.novocode" % "junit-interface" % "0.11" % Test,
    "org.scalactic" %% "scalactic" % "3.0.0",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "org.apache.commons" % "commons-math3" % DepVersionCommonsMath,
    "loci" % "scifio" % DepVersionLoci,
    "loci" % "bio-formats" % DepVersionLoci,
    "loci" % "ome_tools" % DepVersionLoci,
    "net.imagej" % "ij-core" % DepVersionIJ,
    "net.imglib2" % "imglib2" % DepVersionImgLib,
    "net.imglib2" % "imglib2-io" % DepVersionImgLib,
    "net.imglib2" % "imglib2-algorithms" % DepVersionImgLib,
    "org.jruby" % "jruby" % DepVersionJRuby
  )
)

