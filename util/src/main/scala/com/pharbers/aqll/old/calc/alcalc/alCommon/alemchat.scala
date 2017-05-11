package com.pharbers.aqll.old.calc.alcalc.alCommon

import com.pharbers.aqll.old.calc.util.GetProperties
import io.swagger.client.ApiException
import io.swagger.client.api.AuthenticationApi
import io.swagger.client.model.Token

/**
  * Created by qianpeng on 2017/4/13.
  */

trait EasemobAPI {
	def invokeEasemobAPI(): String
}

object alOrgInfo {

	val alOrgName = GetProperties.org_name

	val alAppName = GetProperties.app_name
}

object ResponseHandler {
	def handle(easemobAPI: EasemobAPI): String = {
		try {
			easemobAPI.invokeEasemobAPI()
		} catch {
			case e: ApiException => {
				e.getCode match {
					case 400 => "400"
					case 401 =>
						TokenUtil.initTokenByProp
						easemobAPI.invokeEasemobAPI
					case 429 => "429"
					case 500 =>
						val r = retry(easemobAPI)
						if(r != null) r else "500"
					case x => x.toString
				}
			}
		}
	}

	def retry(easemobAPI: EasemobAPI): String = {
		var time = 5
		var r = ""
		for(i <- 1 to 2) {
			try {
				println(s"Reconnection is in progress... $i")
				val r = easemobAPI.invokeEasemobAPI
				if(r != null) r
			} catch {
				case e: ApiException => time *= 3
				case e2: InterruptedException => println(e2.getMessage)
			}
		}
		r
	}
}

case class token(t: Option[String], expirdat: Double)

object TokenUtil {
	val grant_type = GetProperties.grant_type
	val client_id = GetProperties.client_id
	val client_secret = GetProperties.client_secret
	val body = new Token().clientId(client_id).grantType(grant_type).clientSecret(client_secret)
	val api = new AuthenticationApi
//	var access_token: Option[Any] = None
//	var expirdat = -1D

	def initTokenByProp(): token = {
		val map = alFromJson.formJson(api.orgNameAppNameTokenPost(alOrgInfo.alOrgName, alOrgInfo.alAppName, body))
		new token(Some(" Bearer " + map.get("access_token")), (System.currentTimeMillis() + map.get("expires_in").asInstanceOf[Double]))
	}

	def getAccessToken(): String = {
		initTokenByProp().t.getOrElse(initTokenByProp().t.get)
	}

	def isExpired(): Boolean = {
		System.currentTimeMillis() > initTokenByProp().expirdat
	}
}
