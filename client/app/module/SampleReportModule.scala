package module

import com.mongodb.casbah.commons.MongoDBObject
import com.pharbers.aqll.pattern.{CommonMessage, MessageDefines, ModuleTrait}
import com.pharbers.aqll.util.dao.{_data_connection_cores, from}
import play.api.libs.json.Json.toJson
import play.api.libs.json._
import com.pharbers.aqll.util.DateUtils

import scala.collection.mutable.ListBuffer

object SampleReportModuleMessage {
	sealed class msg_ReportBaseQuery extends CommonMessage
	case class msg_samplereport(data: JsValue) extends msg_ReportBaseQuery
}

object SampleReportModule extends ModuleTrait {

	import SampleReportModuleMessage._
	import controllers.common.default_error_handler.f

	def dispatchMsg(msg: MessageDefines)(pr: Option[Map[String, JsValue]]): (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
		case msg_samplereport(data) => msg_check_func(data)
		case _ => println("Error---------------"); ???
	}

	def msg_check_func(data: JsValue)(implicit error_handler: Int => JsValue): (Option[Map[String, JsValue]], Option[JsValue]) = {
		val company = (data \ "company").asOpt[String].getOrElse("")

//		val top_query_mismatch = MongoDBObject("Company" -> company,"Market" -> market,"Date" -> MongoDBObject("$eq" -> DateUtils.MMyyyy2Long(date)))
//		val top_query_early = MongoDBObject("Company" -> company,"Market" -> market,"Date" -> MongoDBObject("$eq" -> DateUtils.MMyyyy2EarlyLong(date)))
//		val top_query_last = MongoDBObject("Company" -> company,"Market" -> market,"Date" -> MongoDBObject("$eq" -> DateUtils.MMyyyy2LastLong(date)))

			val query = MongoDBObject("Company" -> company)

		try {
			val markets = (from db() in "SampleCheck" where query).selectSort("Date")(MongoDBReport(_))(_data_connection_cores).toList

			val markets_g = markets.asInstanceOf[List[Map[String,Any]]].groupBy(x => x.get("Market").get)
			val lb = new ListBuffer[JsValue]()
			markets_g.foreach { x =>
				val date_lst_sb = new ListBuffer[JsValue]()
				val dhp_lst_sb = new ListBuffer[JsValue]()
				x._2.foreach { y =>
					val date = y.get("Date").get
					date_lst_sb.append(toJson(s"$date"))
					val e_query = MongoDBObject("Company" -> company,"Market" -> y.get("Market").get,"Date" -> MongoDBObject("$eq" -> DateUtils.yyyyMM2EarlyLong(date.toString)))
					val l_query = MongoDBObject("Company" -> company,"Market" -> y.get("Market").get,"Date" -> MongoDBObject("$eq" -> DateUtils.yyyyMM2LastLong(y.get("Date").get.toString)))
					val en_productNum = (from db() in "SampleCheck" where e_query).select(MongoDBProductNum(_))(_data_connection_cores).toList
					val ln_marketNum = (from db() in "SampleCheck" where e_query).select(MongoDBHospitalNum(_))(_data_connection_cores).toList
					val el_productNum = (from db() in "SampleCheck" where l_query).select(MongoDBProductNum(_))(_data_connection_cores).toList
					val ll_marketNum = (from db() in "SampleCheck" where l_query).select(MongoDBHospitalNum(_))(_data_connection_cores).toList
					dhp_lst_sb.append(
					toJson(Map("Date" -> toJson(y.get("Date").get.toString),
						"c_HospNum" -> toJson(y.get("HospNum").get.toString),
						"c_ProductNum" -> toJson(y.get("ProductNum").get.toString),
						"e_HospNum" -> toJson(getNum(en_productNum,"ProductNum").toString),
						"e_ProductNum" -> toJson(getNum(ln_marketNum,"HospNum").toString),
						"l_HospNum" -> toJson(getNum(el_productNum,"ProductNum").toString),
						"l_ProductNum" -> toJson(getNum(ll_marketNum,"HospNum").toString)
					)))
				}
				lb.append(toJson(Map("Market" -> toJson(x._1.toString),"date_lst_sb" -> toJson(date_lst_sb.toList),"dhp_lst_sb" -> toJson(dhp_lst_sb.toList))))
			}
			println(lb)
			(Some(Map("result" -> toJson(lb))), None)
		} catch {
			case ex: Exception => (None, Some(error_handler(ex.getMessage().toInt)))
		}
	}

	def getNum(lst : List[Map[String,Any]],keystr : String) : Long = {
		if(lst.size!=0){
			if(!lst.head.equals(null)){
				lst.head.get(keystr).get.asInstanceOf[Number].longValue()
			}else{
				0
			}
		}else{
			0
		}
	}

	def MongoDBProductNum(d: MongoDBObject): Map[String,Any] = {
		Map("ProductNum" -> d.getAs[Number]("ProductNum").get.longValue())
	}

	def MongoDBHospitalNum(d: MongoDBObject): Map[String,Any] = {
		Map("HospNum" -> d.getAs[Number]("HospNum").get.longValue())
	}

	def MongoDBReport(d: MongoDBObject): Map[String,Any] = {
		Map(
			"HospNum" -> d.getAs[Number]("HospNum").get.longValue(),
			"ProductNum" -> d.getAs[Number]("ProductNum").get.longValue(),
			"Market" -> d.getAs[String]("Market").get,
			"Date" -> DateUtils.Timestamp2yyyyMM(d.getAs[Number]("Date").get.longValue())
		)
	}
}