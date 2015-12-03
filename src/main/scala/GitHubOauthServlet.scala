package com.udacity.github.oauth

import scala.collection.concurrent.TrieMap
import javax.servlet.http.Cookie
import javax.servlet.http.{ HttpServletRequest => Req }
import javax.servlet.http.{ HttpServletResponse => Res }
import java.util.UUID
import java.net.URLEncoder
import java.net.URLDecoder

case class AuthKey(value: String)

object GitHubOauthServlet {
  private val authCache = TrieMap.empty[GitHubUser, AuthKey]
}

class GitHubOauthServlet extends javax.servlet.http.HttpServlet {

  override def doGet(req: Req, res: Res): Unit = {

    val continueUrl: Option[String] =
      for {
        uri      <- Option(req.getRequestURI)
        continue  = uri.drop(1)
        if (continue.length > 0)
      } yield URLDecoder.decode(continue)

    def param(name: String): Option[String] =
      Option(req.getParameter(name))

    def cookie(name: String): Option[String] =
      req.getCookies find { cookie =>
        name == cookie.getName
      } map { cookie =>
        cookie.getValue
      }

    def prop(name: String): Validation[String] =
      sys.props.get(name) match {
        case Some(value) => Valid(value)
        case None => Invalid(List(name))
      }

    val ghClientE: Validation[GitHubClient] =
      prop("GH_ORG").ap(
        prop("GH_API_URL").ap(
          prop("GH_OAUTH_URL").ap(
            prop("GH_CLIENT_SECRET").ap(
              prop("GH_CLIENT_ID").ap(
                Valid((GitHubClient.apply _).curried)
              )
            )
          )
        )
      )

    ghClientE match {
      case Invalid(names) =>
        res.sendError(500, s"""Missing system properties: ${names.mkString(", ")}""")
      case Valid(ghClient) =>

    def ghCreds: Option[(String, String)] =
      for {
        gitHubCode  <- param("code")
        gitHubState <- param("state")
        cookieState <- cookie("gh-state")
        if cookieState == gitHubState
      } yield (gitHubCode, gitHubState)

        ghCreds match {
          case Some((code, state)) =>
            ghClient.getAccessToken(code, state) match {
              case Some(token) => 
                ghClient.getOrgUser(token) match {
                  case Some(user) =>
                    val authKey = AuthKey(UUID.randomUUID.toString)
                    GitHubOauthServlet.authCache += user -> authKey
                    val cookie = new Cookie("auth-key", authKey.value)
                    cookie.setPath("/")
                    res.addCookie(cookie)
                    continueUrl foreach { res.sendRedirect(_) }
                  case None =>
                    res.sendError(403, "Unauthorized")
                }
              case None =>
                res.sendError(401, "Unauthenticated")
            }
          case None =>
            val ghState = UUID.randomUUID.toString
            res.addCookie(new Cookie("gh-state", ghState))
            res.sendRedirect(
              List(
                ghClient.oauthUrl + "/authorize",
                "?client_id=", ghClient.clientId,
                "&scope=read:org",
                "&state=", ghState,
                "&redirect_uri=", URLEncoder.encode(req.getRequestURL.toString)
              ).reduceLeft(_ ++ _)
            )

        }

    }

  }

}
