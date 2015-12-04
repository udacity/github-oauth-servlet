name         := "github-oauth-servlet"
organization := "com.udacity"
scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
    "io.argonaut"               %% "argonaut"         % "6.1-M4"
  , "javax.servlet"             % "javax.servlet-api" % "3.1.0"           % "provided"
  , "org.eclipse.jetty"         %  "jetty-webapp"     % "9.1.0.v20131115" % "test"
  , "org.eclipse.jetty"         %  "jetty-plus"       % "9.1.0.v20131115" % "test"
  , "org.scalatra"              %% "scalatra"         % "2.3.1"           % "test"
  , "org.scalatest"             %% "scalatest"        % "2.2.1"           % "test"
  , "org.apache.httpcomponents" % "httpclient"        % "4.5.1"           % "test"
)
