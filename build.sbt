import sbt.Credentials

val buildVersion: String = sys.env.getOrElse("VERSION", "development")

name := "proto-scala"
organization := "com.namely"
version := buildVersion
scalaVersion := "2.11.12"
isSnapshot := !version.value.matches("^\\d+\\.\\d+\\.\\d+$")
publishMavenStyle := true
parallelExecution in Test := false
scalacOptions ++= Seq("-target:jvm-1.8" )
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

updateOptions := updateOptions.value.withCachedResolution(true)

libraryDependencies ++= Seq(
  "com.google.protobuf" % "protobuf-java" % "3.7.1" % "provided"
)

/****************************************************************/
// SCALAPB SETUP

//libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"

// custom cli options
PB.protocOptions in Compile := Seq("--proto_path", ".")
PB.protocVersion := "-v3.7.1"


val services = Seq(
  "employment",
  "org_charts",
  "org_units",
  "permissions",
  "event_gateway",
  "google",
  "namely/giraffe"
)

// set the build path
PB.protoSources in Compile := services.distinct.map(service => baseDirectory.value / s"protorepo/$service")

// Additional directories to search for imports:
PB.includePaths in Compile := Seq(baseDirectory.value / "protorepo")

// don't know why we can build this
excludeFilter in PB.generate := new SimpleFileFilter((f: File) =>
  f.getAbsolutePath.contains("google/protobuf/")
)

// set output and options
PB.targets in Compile := Seq(
  scalapb.gen(
    flatPackage=false, // set to true to remove file name subpackage for scala classes
    javaConversions=false,
    grpc=false
  ) -> (sourceManaged in Compile).value
)

// END SCALAPB SETUP
/****************************************************************/

lazy val printArtifactName = taskKey[Unit]("Get the artifact name")

printArtifactName := {
  println((artifactPath in (Compile, packageBin)).value.getName)
}

// Publish assembly
artifact in (Compile, assembly) := {
  val art = (artifact in (Compile, assembly)).value
  art.withClassifier(Some("assembly"))
}

addArtifact(artifact in (Compile, assembly), assembly)

// Skip tests on assembly
test in assembly := {}

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
