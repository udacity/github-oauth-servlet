package com.udacity.github.oauth.test

import com.udacity.github.oauth.GitHubOauthServlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite
import java.net.URLEncoder
import Http.get

object GitHubOauthServletServer {

  val port = 11111
  val server = new Server(port)

  val host = s"http://localhost:${port}"

  val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
  context.setContextPath("/")
  context.addServlet(classOf[GitHubOauthServlet], "/*")
  server.setHandler(context)

  def start(): Unit = {
    server.start()
  }

  def stop(): Unit = {
    server.stop()
  }

}

class GitHubOauthServletTests extends FunSuite with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    GitHubServer.start()
    GitHubOauthServletServer.start()
  }

  override def afterAll(): Unit = {
    GitHubOauthServletServer.stop()
    GitHubServer.stop()
  }

  test("get 'missing system properties' page") {
    System.clearProperty("GH_CLIENT_ID")
    System.clearProperty("GH_CLIENT_SECRET")
    System.clearProperty("GH_OAUTH_URL")
    System.clearProperty("GH_API_URL")
    System.clearProperty("GH_ORG")

    get(GitHubOauthServletServer.host) match { case Response(status, headers, body) =>
        assert(500 === status)
        assert(body.contains("Missing system properties:"))
        assert(body.contains("GH_CLIENT_ID"))
        assert(body.contains("GH_CLIENT_SECRET"))
        assert(body.contains("GH_OAUTH_URL"))
        assert(body.contains("GH_API_URL"))
        assert(body.contains("GH_ORG"))
    }

    System.setProperty("GH_CLIENT_ID", "42")
    System.setProperty("GH_CLIENT_SECRET", "42")
    System.setProperty("GH_OAUTH_URL", s"${GitHubServer.host}/oauth")
    System.setProperty("GH_API_URL", s"${GitHubServer.host}/api")
    System.setProperty("GH_ORG", "megacorp")
  }

  val continueUrl = "http://example.com/"
  var redirectUrl: String = ""

  test("redirect to github to initiate oauth") {
    val continue = URLEncoder.encode(continueUrl)
    get(GitHubOauthServletServer.host + "/" + continue) match {
      case Response(status, headers, body) =>
        assert(302 === status)
        assert(headers.get("Location").isDefined)
        redirectUrl = headers("Location")
        assert(redirectUrl.startsWith(GitHubServer.host + "/oauth/authorize"))
    }
  }

  test("authenticate via github oauth") {
    get(redirectUrl) match {
      case Response(status, headers, body) =>
        assert(302 === status)
        assert(headers.get("Location").isDefined)
        redirectUrl = headers("Location")
        assert(redirectUrl.startsWith(GitHubOauthServletServer.host + "/"))
    }
  }

  test("fail to sign-in without access_token") {
    get(redirectUrl) match {
      case Response(status, headers, body) =>
        assert(401 === status)
    }
  }

  test("enable access_token") {
    get(GitHubServer.host + "/enable-access-token") match {
      case Response(status, headers, body) =>
        assert(200 === status)
    }
  }

  test("fail to sign-in with non-org user") {
    get(redirectUrl) match {
      case Response(status, headers, body) =>
        assert(403 === status)
    }
  }

  test("add user to the udacity org") {
    get(GitHubServer.host + "/add-user-to-org") match {
      case Response(status, headers, body) =>
        assert(200 === status)
    }
  }

  test("complete sign-in") {
    get(redirectUrl) match {
      case Response(status, headers, body) =>
        assert(302 === status)
        assert(headers.get("Location") === Some(continueUrl))
    }
  }

}
