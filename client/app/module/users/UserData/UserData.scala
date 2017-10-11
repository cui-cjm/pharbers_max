package module.users.UserData

import java.util.Date

import com.mongodb.casbah.Imports._
import com.pharbers.aqll.common.alDate.scala.alDateOpt
import com.pharbers.sercuity.Sercurity
import com.pharbers.token.AuthTokenTrait
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

trait UserData {
	
	def conditions(data: JsValue): DBObject = {
		val builder = MongoDBObject.newBuilder
		(data \ "user_id").asOpt[String].map(x => builder += "user_id" -> x).getOrElse(Unit)
		(data \ "email").asOpt[String].map(x => builder += "profile.email" -> x).getOrElse(Unit)
		(data \ "name").asOpt[String].map(x => builder += "profile.name" -> x).getOrElse(Unit)
		(data \ "phone").asOpt[String].map(x => builder += "profile.phone" -> x).getOrElse(Unit)
		builder.result
	}
	
	
	implicit val m2d: JsValue => DBObject = { js =>
		val builder = MongoDBObject.newBuilder
		val email = (js \ "email").asOpt[String].map(x => x).getOrElse(throw new Exception("info input email"))
		val password = (js \ "password").asOpt[String].map(x => x).getOrElse(email)
		val secret = Sercurity.md5Hash(s"$email$password")
		val name = (js \ "name").asOpt[String].map(x => x).getOrElse(throw new Exception("info input linkman name"))
		val phone = (js \ "phone").asOpt[String].map(x => x).getOrElse(throw new Exception("info input phone"))
		val scope = (js \ "scope").asOpt[List[String]].map(x => x).getOrElse(Nil)
		val id = (js \ "user_id").asOpt[String].map(x => x).getOrElse(Sercurity.md5Hash(s"$email"))
		val profile = DBObject("email" -> email, "secret" -> secret, "name" -> name, "phone" -> phone, "scope" -> scope)
		
		builder += "user_id" -> id
		builder += "profile" -> profile
		builder += "other" -> Map.empty
		builder += "date" -> new Date().getTime
		builder.result
	}
	
	implicit val d2m: DBObject => Map[String, JsValue] = { obj =>
		val profile = obj.as[MongoDBObject]("profile")
//		val other = profile.getAs[MongoDBObject]("other").map(x => x).getOrElse("") // TODO: 详细解析留在后面在做，暂时先解析出来不做处理
		Map("user_id" -> toJson(obj.getAs[String]("user_id").map(x => x).getOrElse("")),
			"name" -> toJson(profile.getAs[String]("name").map(x => x).getOrElse("")),
			"email" -> toJson(profile.getAs[String]("email").map(x => x).getOrElse("")),
			"phone" -> toJson(profile.getAs[String]("phone").map(x => x).getOrElse("0")),
			"date" -> toJson(alDateOpt.Timestamp2yyyyMMdd(obj.getAs[Number]("date").getOrElse(0).toString.toLong)),
			"scope" -> toJson(profile.getAs[List[String]]("scope").map(x => x).getOrElse(Nil)))
	}
}
