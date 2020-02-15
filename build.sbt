/*
 * Copyright 2020 CJWW Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.typesafe.config.ConfigFactory
import com.typesafe.sbt.packager.docker.Cmd

import scala.util.Try

val appName = "natter"

val btVersion: String = Try(ConfigFactory.load.getString("version")).getOrElse("0.1.0")

val dependencies: Seq[ModuleID] = Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0"  % Test,
  "org.mockito"             % "mockito-core"       % "3.2.4"  % Test
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala)
  .configs(IntegrationTest)
  .settings(PlayKeys.playDefaultPort := 6161)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    version                                       :=  btVersion,
    scalaVersion                                  :=  "2.13.1",
    organization                                  :=  "com.cjww-dev.apps",
    resolvers                                     ++= Seq(
      "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
      "cjww-dev"       at "https://dl.bintray.com/cjww-development/releases"
    ),
    libraryDependencies                           ++= dependencies,
    bintrayOrganization                           :=  Some("cjww-development"),
    bintrayReleaseOnPublish in ThisBuild          :=  true,
    bintrayRepository                             :=  "releases",
    bintrayOmitLicense                            :=  true,
    Keys.fork in IntegrationTest                  :=  false,
    unmanagedSourceDirectories in IntegrationTest :=  (baseDirectory in IntegrationTest)(base => Seq(base / "it")).value,
    parallelExecution in IntegrationTest          :=  false,
    dockerRepository                              :=  Some("cjwwdevelopment"),
    dockerCommands                                :=  Seq(
      Cmd("FROM", "openjdk:8u181-jdk"),
      Cmd("WORKDIR", "/opt/docker"),
      Cmd("ADD", "--chown=daemon:daemon opt /opt"),
      Cmd("USER", "daemon"),
      Cmd("ENTRYPOINT", s"""["/opt/docker/bin/$appName"]"""),
      Cmd("CMD", """[]""")
    )
  )