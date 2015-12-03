package com.udacity.github.oauth.test

import org.apache.http.client._
import org.apache.http.client.methods._
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.entity.AbstractHttpEntity
import org.apache.http.message.BasicNameValuePair
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.apache.http.client.entity.EntityBuilder
import scala.collection.JavaConverters._

case class Response(status: Int, headers: Map[String,String], body: String)

object Http {

  val httpClient =
    HttpClientBuilder.create().disableRedirectHandling().build()

  def get(url: String): Response = {
    val reqBuilder = RequestBuilder.create("GET").setUri(url)
/*
    authKey foreach { k: AuthKey =>
      reqBuilder.addHeader("Cookie", "auth-key=" + k.value)
    }
*/
    val req = reqBuilder.build()
    val res = httpClient.execute(req)
    val resEntity = res.getEntity()
    val resStatus = res.getStatusLine().getStatusCode()
    val resHeaders = res.getAllHeaders().map(h =>
      (h.getName() -> h.getValue())).toMap
    val resBody = EntityUtils.toString(resEntity, "UTF-8")
    Response(resStatus, resHeaders, resBody)
  }

}
