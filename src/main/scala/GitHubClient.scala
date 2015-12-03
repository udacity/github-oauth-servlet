package com.udacity.github.oauth

import argonaut._
import Argonaut._

case class GitHubUser(name: String) {

  implicit def encodeJson: EncodeJson[GitHubUser] =
    jencode1L((u: GitHubUser) => (u.name))("name")

  def toJson: String = this.asJson.spaces2

}

object GitHubUser {

  implicit def decodeJson: DecodeJson[GitHubUser] =
    DecodeJson(c => for {
      name <- (c --\ "name").as[String]
    } yield GitHubUser(name))

  def parse(json: String): Option[GitHubUser] =
    json.decodeOption[GitHubUser]

}

case class GitHubOrg(login: String)

object GitHubOrg {

  implicit def decodeJson: DecodeJson[GitHubOrg] =
    DecodeJson(c => for {
      login <- (c --\ "login").as[String]
    } yield GitHubOrg(login))

  implicit def encodeJson: EncodeJson[GitHubOrg] =
    jencode1L((u: GitHubOrg) => (u.login))("login")

}

case class GitHubClient(
  clientId: String, clientSecret: String,
  oauthUrl: String, apiUrl: String,
  orgname: String) {

  import java.io._
  import java.net.URL
  import java.net.HttpURLConnection
  import java.util.UUID
  import scala.util.parsing.json.JSON

  def getUser(accessToken: String): Option[GitHubUser] = {
    val url = new URL(apiUrl + "/user")
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("GET")
    conn.setDoInput(true)
    conn.setDoOutput(false)
    conn.setRequestProperty("Authorization", s"token ${accessToken}")

    val is = conn.getInputStream
    val resp = scala.io.Source.fromInputStream(is).mkString

    for {
      u <- GitHubUser.parse(resp)
    } yield GitHubUser(name = u.name)

  }

  def getOrgs(accessToken: String): List[GitHubOrg] = {
    val url = new URL(apiUrl + "/user/orgs")
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("GET")
    conn.setDoInput(true)
    conn.setDoOutput(false)
    conn.setRequestProperty("Authorization", s"token ${accessToken}")

    val is = conn.getInputStream
    val resp = scala.io.Source.fromInputStream(is).mkString

    resp.decodeOption[List[GitHubOrg]].toList.flatten
  }

  def getOrgUser(accessToken: String): Option[GitHubUser] =
    for {
      user <- getUser(accessToken)
      orgs  = getOrgs(accessToken)
      if orgs.find({ org: GitHubOrg => org.login == orgname }).isDefined
    } yield user

  def getAccessToken(code: String, state: String): Option[String] = {

    val url = new URL(oauthUrl + "/access_token")
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("POST")
    conn.setDoInput(true)
    conn.setDoOutput(true)
    val os = conn.getOutputStream
    val wr = new OutputStreamWriter(os)
    wr.write(
      List(
        "client_id=", clientId,
        "&client_secret=", clientSecret,
        "&code=", code,
        "&state=", state
      ).reduceLeft(_ ++ _)
    )
    wr.close

    val is = conn.getInputStream
    val resp = scala.io.Source.fromInputStream(is).mkString
    val respMap = (for {
      kvString <- resp.split("&")
      kvArray   = kvString.split("=")
      key      <- kvArray.lift(0)
      value    <- kvArray.lift(1)
    } yield (key, value)).toMap

    respMap.get("access_token")
  }
}
