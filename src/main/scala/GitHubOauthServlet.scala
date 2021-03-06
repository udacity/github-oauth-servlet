package com.udacity.github.oauth

import scala.collection.concurrent.TrieMap
import javax.servlet.http.Cookie
import javax.servlet.http.{ HttpServletRequest => Req }
import javax.servlet.http.{ HttpServletResponse => Res }
import java.util.UUID
import java.net.URLEncoder
import java.net.URLDecoder
import com.udacity.github.oauth.Validation._

case class AuthKey(value: String)

object GitHubOauthServlet {

  private val authCache = TrieMap.empty[AuthKey, GitHubUser]

  def get(k: AuthKey): Option[GitHubUser] = authCache.get(k)

}

class GitHubOauthServlet extends javax.servlet.http.HttpServlet {

  override def doGet(req: Req, res: Res): Unit = {

    val continueUrl: Option[String] =
      for {
        uri      <- Option(req.getRequestURI)
        continue  = uri.drop(req.getServletPath.length + 1)
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
      sys.props.get(name).orElse(sys.env.get(name)) match {
        case Some(value) => Valid(value)
        case None => Invalid(List(name))
      }

    def ghClientE: Validation[GitHubClient] =
      (GitHubClient.apply _).curried <%>
        prop("GH_CLIENT_ID") <*>
        prop("GH_CLIENT_SECRET") <*>
        prop("GH_OAUTH_URL") <*>
        prop("GH_API_URL") <*>
        prop("GH_ORG")

    ghClientE match {
      case Invalid(names) =>
        res.sendError(500, s"""Missing environment configuration: ${names.mkString(", ")}""")
      case Valid(ghClient) =>
        val ghCreds: Option[(String, String)] =
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
                    GitHubOauthServlet.authCache += authKey -> user
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
