package controllers

import java.util.Date
import javax.inject._

import akka.actor.ActorSystem
import com.pharbers.aqll.common.alAdminEnum
import com.pharbers.cliTraits.DBTrait
import com.pharbers.dbManagerTrait.dbInstanceManager
import com.pharbers.message.send.SendMessageTrait
import com.pharbers.mongodbConnect.connection_instance
import com.pharbers.token.AuthTokenTrait
import play.api.mvc._

class alMaxRouterController @Inject()(as_inject : ActorSystem, dbt : dbInstanceManager, att : AuthTokenTrait) extends Controller {
    implicit val as = as_inject
    implicit val db_cores : DBTrait = dbt.queryDBInstance("calc").get
    implicit val db_basic : DBTrait = dbt.queryDBInstance("cli").get

    implicit val db_cores_connection : connection_instance = dbt.queryDBConnection("calc").get
    implicit val db_basic_connection : connection_instance = dbt.queryDBConnection("cli").get
    
    
    //从cookie中取出token验证用户角色
    def auth_user = Action { request =>
        val token = java.net.URLDecoder.decode(getUserTokenByCookies(request), "UTF-8")
        if ((att.decrypt2JsValue(token) \ "scope").asOpt[List[String]].getOrElse(Nil).contains("BD")) Redirect("/login/db")
        else Redirect("/index")
    }
    
    def validation_token(parm: String) = Action { request =>
        val token = att.decrypt2JsValue(parm)
        val temp = java.net.URLEncoder.encode(parm, "ISO-8859-1")
        val expire_in = (token \ "expire_in").asOpt[Long].map (x => x).getOrElse(throw new Exception("token parse error"))
        if (new Date().getTime > expire_in) Redirect("/token/fail")
        else
        (token \ "action").asOpt[String].getOrElse(None) match {
            case None => Redirect("/error")
            case "forget_password" => Redirect(s"/password/new/$temp/${(token \ "email").as[String]}")
            case "first_login" => Redirect(s"/password/set/$temp/${(token \ "email").as[String]}")
        }
    }

    def login = Action { request =>
        Ok(views.html.authPages.login())
    }

    def infoRegistration = Action { request =>
        Ok(views.html.authPages.infoRegistration())
    }

    def dbLogin = Action { request =>
        Ok(views.html.authPages.bdSignSelect())
    }

    def userInfoConfirm = Action { request =>
        Ok(views.html.authPages.userInfoConfirm())
    }

    def verificationRegister = Action { request =>
        Ok(views.html.authPages.registerCodeValidation())
    }

    def tokenFail = Action { request =>
        Ok(views.html.authPages.activeAccountFailed())
    }

    def emailInvocation(name : String, email : String) = Action { request =>
        Ok(views.html.authPages.emailBeenSend(name, email))
    }

    def findpwd = Action{
        Ok(views.html.authPages.findPassword())
    }
    def findpwd_success = Action{
        Ok(views.html.authPages.findPasswordSuccess())
    }
    def new_pwd(token: String, email: String) = Action{
        Ok(views.html.authPages.newPassword(email))
    }
    def set_pwd(token: String, email: String) = Action{
        Ok(views.html.authPages.setPassword(email))
    }

    def index = Action { request =>
//        if (getUserTokenByCookies(request).equals("")) {
//            Ok(views.html.login())
//        } else {
//            Ok(views.html.index(getAdminByCookies(request)))
            Ok(views.html.calcPages.index())
//        }
    }
    
    def calcData = Action { request =>
//        Ok(views.html.newhome.calcData(getAdminByCookies(request)))
        Ok(views.html.calcPages.newhome.calcData(""))
    }

    def historyData = Action { request =>
        Ok(views.html.calcPages.historyData())
//            getAdminByCookies(request),
//            PageDefaultData(alModularEnum.RQ, db_basic_connection, db_basic_connection)._1))
    }
    

    def getUserTokenByCookies(request: Request[AnyContent]): String = {
        request.cookies.get("user_token").map(x => x.value).getOrElse("")
    }

    def getAdminByCookies(request: Request[AnyContent]): String = {
        request.cookies.get("auth").map(x => x.value).get.toInt match {
            case 0 => alAdminEnum.users.toString
            case 1 => alAdminEnum.admin.toString
            case 2 => alAdminEnum.admin.toString
        }
    }
    
    def postSuccess = Action{
        Ok(views.html.authPages.successEmailPost())
    }
    
//    def registerSuccess() = Action{
//        Ok(views.html.successRegister())
//    }

    def registerSuccess(name : String, email : String) = Action{
        Ok(views.html.authPages.successRegister(name, email))
    }
}