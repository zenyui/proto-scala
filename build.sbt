import sbt.Credentials

val buildVersion: String = sys.env.getOrElse("VERSION", "development")
val sparkVersion = "2.4.3"

name := "proto-spark"
organization := "com.namely"
version := buildVersion
scalaVersion := "2.11.12"
isSnapshot := !version.value.matches("^\\d+\\.\\d+\\.\\d+$")
publishMavenStyle := true
parallelExecution in Test := false
scalacOptions ++= Seq("-target:jvm-1.8" )
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

unmanagedSourceDirectories in Compile += baseDirectory.value / "src" / "main" / "generated"

updateOptions := updateOptions.value.withCachedResolution(true)

resolvers ++= Seq(
  "Artima Maven Repository" at "https://repo.artima.com/releases",
  "Spark Packages Repo" at "https://dl.bintray.com/spark-packages/maven/"
)

libraryDependencies ++= Seq(
  "com.google.protobuf" % "protobuf-java" % "3.7.1" % "provided",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "io.grpc" % "grpc-stub" % "1.15.1",
  "io.grpc" % "grpc-protobuf" % "1.15.1",
  "org.apache.spark" %% "spark-sql" % "2.4.3" % "provided",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided"
)

/****************************************************************/
// SCALAPB SETUP

//libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"

// custom cli options
PB.protocOptions in Compile := Seq("--proto_path", ".")
PB.protocVersion := "-v3.7.1"


//val protoWhitelist: Set[String] = Set(
//  "protorepo/employment",
//  "protorepo/org_charts",
//  "protorepo/org_units",
//  "protorepo/permissions",
//  "protorepo/event_gateway",
//  "protorepo/namely/giraffe"
//)

// set the build path
PB.protoSources in Compile ++= Seq(file("protorepo"))

// Additional directories to search for imports:
PB.includePaths in Compile ++= Seq(file("protorepo"))

excludeFilter in PB.generate := new SimpleFileFilter((f: File) => f.getAbsolutePath.contains("protorepo/google/protobuf/"))

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
