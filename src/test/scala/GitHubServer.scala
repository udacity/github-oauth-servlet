package com.udacity.github.oauth.test

import org.scalatra.ScalatraServlet
import com.udacity.github.oauth.GitHubUser
import com.udacity.github.oauth.GitHubOrg
import argonaut._
import Argonaut._

class GitHubServlet extends ScalatraServlet {

  get("/enable-access-token") {
    post("/oauth/access_token") {
      "access_token=FOO"
    }
  }

  get("/add-user-to-org") {
    get("/api/user/orgs") {
      List(GitHubOrg(login = "megacorp")).asJson.spaces2
    }
  }

  get("/api/user") {
    GitHubUser(login = "bob").toJson
  }

  get("/api/user/orgs") {
    List[GitHubOrg]().asJson.spaces2
  }

  post("/oauth/access_token") {
  }

  get("/oauth/authorize") {
    val code = "CODE"
    val state = params("state")
    val redirectUri = params("redirect_uri")
    val url = s"${redirectUri}?code=${code}&state=${state}"
    redirect(url)
  }

}

object GitHubServer {

  import org.eclipse.jetty.server.Server
  import org.eclipse.jetty.server.ServerConnector
  import org.eclipse.jetty.servlet.ServletContextHandler

  val port = 11112
  val server = new Server(port)

  val contextRoot = "/oauth/github"
  val host = s"http://localhost:${port}${contextRoot}"

  val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
  context.setContextPath(contextRoot)
  context.addServlet(classOf[GitHubServlet], "/*")
  server.setHandler(context)

  def start(): Unit = {
    server.start()
  }

  def stop(): Unit = {
    server.stop()
  }

}
