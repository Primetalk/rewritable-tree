ThisBuild / organization := "ru.primetalk"
ThisBuild / organizationName := "rewritable-tree"
ThisBuild / organizationHomepage := Some(url("http://synapse-grid.primetalk.ru/"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/Primetalk/rewritable-tree"),
    "scm:https://github.com/Primetalk/rewritable-tree.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "Primetalk",
    name  = "Arseniy Zhizhelev",
    email = "zhizhelev@primetalk.ru",
    url   = url("http://synapse-grid.primetalk.ru/")
  )
)

ThisBuild / description := "Rewritable tree typeclass and supporting algorithms."
ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/Primetalk/rewritable-tree"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true

pgpSecretRing := pgpPublicRing.value

usePgpKeyHex("855c7687")

