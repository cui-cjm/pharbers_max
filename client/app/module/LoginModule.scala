package module

import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.pharbers.aqll.common.alEncryption.alEncryptionOpt
import play.api.libs.json.Json._
import play.api.libs.json._
import com.pharbers.aqll.common.alErrorCode.alErrorCode._
import com.pharbers.bmmessages.{CommonMessage, CommonModules, MessageDefines}
import com.pharbers.bmpattern.ModuleTrait
import com.pharbers.dbManagerTrait.dbInstanceManager

object LoginModuleMessage {
    sealed class msg_LoginBaseQuery extends CommonMessage("login", LoginModule)
    case class msg_login(data: JsValue, ip: String) extends msg_LoginBaseQuery
}

object LoginModule extends ModuleTrait {
    import LoginModuleMessage._
    def dispatchMsg(msg: MessageDefines)(pr: Option[Map[String, JsValue]])(implicit cm: CommonModules): (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_login(data, ip) => login(data, ip)
        case _ => ???
    }

    def login(data: JsValue, ip: String)(implicit cm: CommonModules): (Option[Map[String, JsValue]], Option[JsValue]) = {
        val conn = cm.modules.get.get("db").map (x => x.asInstanceOf[dbInstanceManager]).getOrElse(throw new Exception("no db connection"))
        implicit val dc = conn.queryDBConnection("cli").get
        def userConditions(getter : JsValue => Option[Any])(key : String, value : JsValue) : Option[DBObject] = getter(value) match {
          case None => None
          case Some(x) => {
              key match {
                  case "Account" => Some("User_lst."+key $eq x.toString)
                  case _ => Some("User_lst."+key $eq alEncryptionOpt.md5(x.asInstanceOf[String]))
              }
          }
        }

        def conditionsAcc(o: List[DBObject], keys: List[String], func: (String, JsValue) => Option[DBObject]): List[DBObject] = keys match {
            case Nil => o
            case head :: lst => func(head, (data \ head).as[JsValue]) match {
                case None => conditionsAcc(o, lst, func)
                case Some(y) => conditionsAcc(y :: o, lst, func)
            }
        }
        def conditions: List[DBObject] = conditionsAcc(Nil, "Account" :: "Password" :: Nil, userConditions(x => x.asOpt[String]))
        try {
            conditions match {
                case Nil => throw new Exception("warn user not exist")
                case _ => {
                    conditions match {
                        case x: List[DBObject] =>
//                            val t: List[DBObject] = List(("$unwind" $eq "$User_lst"), ("$match" $eq (x(0) ++ x(1))))
//                            val tmp = (from db () in "Company" where t).selectAggregate(resultData(_, ip)).toList
//                            tmp match {
//                                case Nil => throw new Exception("warn user not exist")
//                                case _ => (successToJson(tmp.head), None)
//                            }
                            val tmp = toJson(Map("UserName" -> toJson("qp"),
                                "Token" -> toJson("qp"),
                                "E_Mail" -> toJson("qp"),
                                "UserTimestamp" -> toJson("0"),
                                "UserAuth" -> toJson("0"),
                                "Auth" -> toJson("0"),
                                "User_Token" -> toJson("0"),
                                "CompanyNameCh" -> toJson("None"),
                                "CompanyNameEn" -> toJson("None"),
                                "ip" -> toJson(ip),
                                "Timestamp" -> toJson(0)))
                            (successToJson(tmp), None)
                    }
                }
            }
        } catch {
            case ex: Exception => (None, Some(errorToJson(ex.getMessage())))
        }
    }
    
    def resultData(x: MongoDBObject, ip: String): JsValue = {
        val User_lst = x.getAs[MongoDBObject]("User_lst").get
        val Company = x.getAs[MongoDBList]("Company_Name").get
        toJson(Map("UserName" -> toJson(User_lst.getAs[String]("Name").getOrElse("无")),
            "Token" -> toJson(x.getAs[String]("Company_Id").get),
            "E_Mail" -> toJson(x.getAs[String]("E-Mail").get),
            "UserTimestamp" -> toJson(User_lst.as[Number]("Timestamp").longValue()),
            "UserAuth" -> toJson(User_lst.as[Number]("auth").intValue()),
            "Auth" -> toJson(User_lst.as[Number]("isadministrator").intValue()),
            "User_Token" -> toJson(User_lst.getAs[String]("ID").getOrElse("无")),
            "CompanyNameCh" -> toJson(Company.head.asInstanceOf[BasicDBObject].get("Ch").toString),
            "CompanyNameEn" -> toJson(Company.head.asInstanceOf[BasicDBObject].get("En").toString),
            "ip" -> toJson(ip),
            "Timestamp" -> toJson(x.getAs[Number]("Timestamp").get.longValue())))
    }
}