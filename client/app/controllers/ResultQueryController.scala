package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import com.pharbers.bmmessages.{CommonModules, MessageRoutes}
import com.pharbers.bmpattern.LogMessage.msg_log
import com.pharbers.bmpattern.ResultMessage.msg_CommonResultMessage
import com.pharbers.dbManagerTrait.dbInstanceManager
import com.pharbers.token.AuthTokenTrait
import module.ResultQueryModuleMessage._
import play.api.libs.json.Json.toJson
import play.api.mvc._
import controllers.common.requestArgsQuery

class ResultQueryController@Inject() (as_inject : ActorSystem, dbt : dbInstanceManager, att : AuthTokenTrait) extends Controller{
	implicit val as = as_inject
	
    def resultQuerySearch = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
	    import com.pharbers.bmpattern.LogMessage.common_log
	    import com.pharbers.bmpattern.ResultMessage.common_result
		MessageRoutes(msg_log(toJson(Map("method" -> toJson("resultQuerySearch"))), jv) ::  msg_resultquery(jv) :: msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt, "att" -> att))))
	})

}