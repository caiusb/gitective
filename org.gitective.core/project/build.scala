import sbt._

object GitectiveBuild extends com.typesafe.sbt.pom.PomBuild {
	override lazy val overrideRootProjectName:Option[String] = Some("Gitective")
}
