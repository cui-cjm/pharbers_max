package module

import com.mongodb.{BasicDBList, DBObject}
import com.pharbers.aqll.common.alEncryption.alEncryptionOpt
import play.api.libs.json.JsValue
import com.pharbers.aqll.common.alErrorCode.alErrorCode._
import play.api.libs.json.Json.toJson
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import com.pharbers.aqll.common.alDate.scala.alDateOpt
import com.pharbers.bmmessages.{CommonMessage, CommonModules, MessageDefines}
import com.pharbers.bmpattern.ModuleTrait
import com.pharbers.cliTraits.DBTrait
import com.pharbers.dbManagerTrait.dbInstanceManager
import com.pharbers.mongodbConnect.connection_instance

object CompanyManageModuleMessage {
    sealed class msg_CompanyManageBase extends CommonMessage("companymanage", CompanyManageModule)
    case class msg_companymanage_query(data: JsValue) extends msg_CompanyManageBase
    case class msg_companymanage_delete(data: JsValue) extends msg_CompanyManageBase
    case class msg_companymanage_findOne(data: JsValue) extends msg_CompanyManageBase
    case class msg_companymanage_save(data: JsValue) extends msg_CompanyManageBase
}

object CompanyManageModule extends ModuleTrait {
    import CompanyManageModuleMessage._
    def dispatchMsg(msg: MessageDefines)(pr: Option[Map[String, JsValue]])(implicit cm : CommonModules): (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_companymanage_query(data) => query_company_func(data)
        case msg_companymanage_delete(data) => delete_company_func(data)
        case msg_companymanage_findOne(data) => findOne_company_func(data)
        case msg_companymanage_save(data) => save_company_func(data)
    }

    def query_company_func(data: JsValue)(implicit cm : CommonModules): (Option[Map[String, JsValue]], Option[JsValue]) = {
        val db = cm.modules.get.get("db").map (x => x.asInstanceOf[connection_instance]).getOrElse(throw new Exception("no db connection"))
        try {
            val Company_Id = (data \ "Company_Id").get.asOpt[String].getOrElse(throw new Exception("warn input"))
            val result = Company_Id match {
                case "788d4ff5836bcee2ebf4940fec882ac8" => {
                    db.getCollection("Company").find().toList.map(x => queryCompanyDBObject(x: DBObject))
                }
                case _ => {
                    val query = MongoDBObject("Company_Id" -> Company_Id)
                    db.getCollection("Company").find(query).toList.map(x => queryCompanyDBObject(x: DBObject))
                }
            }
            (successToJson(toJson(result)), None)
        } catch {
            case ex: Exception => (None, Some(errorToJson(ex.getMessage())))
        }
    }

    def queryCompanyDBObject(x: DBObject): JsValue ={
        val Company_Name_lst = x.get("Company_Name").asInstanceOf[BasicDBList].toArray.head.asInstanceOf[DBObject]
        toJson(Map(
            "Company_Id" -> toJson(x.get("Company_Id").asInstanceOf[String]),
            "Ch" -> toJson(Company_Name_lst.get("Ch").asInstanceOf[String]),
            "En" -> toJson(Company_Name_lst.get("En").asInstanceOf[String]),
            "E_Mail" -> toJson(x.get("E-Mail").asInstanceOf[String]),
            "Timestamp" -> toJson(alDateOpt.Timestamp2yyyyMMdd(x.get("Timestamp").asInstanceOf[Number].longValue()))))
    }

    def delete_company_func(data: JsValue)(implicit cm : CommonModules): (Option[Map[String, JsValue]], Option[JsValue]) = {
        val db = cm.modules.get.get("db").map (x => x.asInstanceOf[connection_instance]).getOrElse(throw new Exception("no db connection"))
        try {
            val ids = (data \ "Company_Id").get.asOpt[List[String]].getOrElse(throw new Exception("warn input"))
            val r = ids map(x => db.getCollection("Company").findAndRemove(MongoDBObject("Company_Id" -> x)))
            r.size match {
                case i if i.equals(ids.size) => (successToJson(toJson(getErrorMessageByName("warn operation success"))), None)
                case _ => throw new Exception("warn operation failed")
            }
        } catch {
            case ex: Exception => (None, Some(errorToJson(ex.getMessage())))
        }
    }

    def findOne_company_func(data: JsValue)(implicit cm : CommonModules): (Option[Map[String, JsValue]], Option[JsValue]) = {
        val db = cm.modules.get.get("db").map (x => x.asInstanceOf[connection_instance]).getOrElse(throw new Exception("no db connection"))
        try {
            (successToJson(findOneCompany(db,data)), None)
        } catch {
            case ex: Exception => (None, Some(errorToJson(ex.getMessage())))
        }
    }

    def save_company_func(data: JsValue)(implicit cm : CommonModules): (Option[Map[String, JsValue]], Option[JsValue]) = {
        val db = cm.modules.get.get("db").map (x => x.asInstanceOf[connection_instance]).getOrElse(throw new Exception("no db connection"))
        try {
            val au = (data \ "au").get.asOpt[String].getOrElse(throw new Exception("warn input"))
            val Company_Id = (data \ "Company_Id").get.asOpt[String].getOrElse(throw new Exception("warn input"))
            val Company_Name_Ch = (data \ "Company_Name_Ch").get.asOpt[String].getOrElse(throw new Exception("warn input"))
            val Company_Name_En = (data \ "Company_Name_En").get.asOpt[String].getOrElse(throw new Exception("warn input"))
            val E_Mail = (data \ "E_Mail").get.asOpt[String].getOrElse(throw new Exception("warn input"))
            au match {
                case "add" => {
                    val Company_Id_MD5 = Company_Id match {
                        case "" => alEncryptionOpt.md5(Company_Name_Ch match {
                            case "" => Company_Name_En
                            case _ => Company_Name_Ch
                        })
                        case _ => Company_Id
                    }
                    db.getCollection("Company").findOne(MongoDBObject("Company_Id" -> Company_Id_MD5)) match {
                        case None => {
                            db.getCollection("Company").insert(
                                MongoDBObject(
                                    "Company_Id" -> Company_Id_MD5,
                                    "Company_Name" -> MongoDBList(MongoDBObject(
                                        "Ch" -> (data \ "Company_Name_Ch").get.asOpt[String].get,
                                        "En" -> (data \ "Company_Name_En").get.asOpt[String].get
                                    )),
                                    "E-Mail" -> (data \ "E_Mail").get.asOpt[String].get,
                                    "Timestamp" -> System.currentTimeMillis(),
                                    "User_lst" -> MongoDBList()
                                )
                            ) getN match {
                                case 0 => (successToJson(toJson(getErrorMessageByName("warn operation success"))), None)
                                case _ => throw new Exception("warn operation failed")
                            }
                        }
                        case _ => {
                            throw new Exception("warn target already exists")
                        }
                    }
                }
                case "update" => {
                    val query = MongoDBObject("Company_Id" -> Company_Id)
                    val company = db.getCollection("Company").findOne(query)
                    db.getCollection("Company").update(query,
                        MongoDBObject(
                            "Company_Id" -> Company_Id,
                            "Company_Name" -> MongoDBList(MongoDBObject(
                                "Ch" -> (data \ "Company_Name_Ch").get.asOpt[String].get,
                                "En" -> (data \ "Company_Name_En").get.asOpt[String].get
                            )),
                            "E-Mail" -> (data \ "E_Mail").get.asOpt[String].get,
                            "Timestamp" -> System.currentTimeMillis(),
                            "User_lst" -> company.get.get("User_lst").asInstanceOf[BasicDBList].toArray
                        )
                    ).getN match {
                        case 1 => (successToJson(toJson(getErrorMessageByName("warn operation success"))), None)
                        case _ => throw new Exception("warn operation failed")
                    }
                }
            }
        } catch {
            case ex: Exception => (None, Some(errorToJson(ex.getMessage())))
        }
    }

    def findOneCompany(database: connection_instance, data: JsValue): JsValue ={
        val Company_Id = (data \ "Company_Id").get.asOpt[String].getOrElse(throw new Exception("warn input"))
        val query =MongoDBObject("Company_Id" -> Company_Id)
        val company = database.getCollection("Company").findOne(query)
        company match {
            case None => throw new Exception("warn target does not exist")
            case _ => {
                val Company_Name = company.get.get("Company_Name").asInstanceOf[BasicDBList].toArray.head.asInstanceOf[DBObject]
                val User_lst = company.get.get("User_lst").asInstanceOf[BasicDBList].toArray
                toJson(Map(
                    "Company_Id" -> toJson(company.get.get("Company_Id").asInstanceOf[String]),
                    "Ch" -> toJson(Company_Name.get("Ch").asInstanceOf[String]),
                    "En" -> toJson(Company_Name.get("En").asInstanceOf[String]),
                    "E_Mail" -> toJson(company.get.get("E-Mail").asInstanceOf[String]),
                    "Timestamp" -> toJson(alDateOpt.Timestamp2yyyyMMdd(company.get.get("Timestamp").asInstanceOf[Number].longValue())),
                    "User_lst" -> toJson(User_lst.size)
                ))
            }
        }
    }
}