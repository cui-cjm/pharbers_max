package controllers

import javax.inject.{Inject, Singleton}

import com.pharbers.aqll.dbmodule.MongoDBModule
import com.pharbers.aqll.pattern
import com.pharbers.aqll.pattern.LogMessage.msg_log
import com.pharbers.aqll.pattern.{CommonModule, MessageRoutes}
import com.pharbers.aqll.pattern.ResultMessage.msg_CommonResultMessage
import controllers.common.requestArgsQuery.requestArgs
import module.ResultQueryModuleMessage._
import play.api.libs.json.Json.toJson
import play.api.mvc._
/**
	* Created by Wli on 2017/1/5.
	*/
@Singleton
class ResultQueryController@Inject() (mdb: MongoDBModule) extends Controller{
	implicit val dbc = mdb.cores

	implicit val cm = CommonModule(Some(Map("db" -> dbc)))
    
  def resultQuerySearch = Action (request => requestArgs(request) { jv =>
		import pattern.LogMessage.common_log
		import pattern.ResultMessage.common_result
		MessageRoutes(msg_log(toJson(Map("method" -> toJson("resultQuerySearch"))), jv, request) ::  msg_resultquery(jv) :: msg_CommonResultMessage() :: Nil, None)
	})
	
}