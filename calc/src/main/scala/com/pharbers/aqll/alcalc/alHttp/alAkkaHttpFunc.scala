package com.pharbers.aqll.alcalc.alHttp

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import akka.util.Timeout
import com.google.gson.Gson
import com.pharbers.aqll.alcalc.alcmd.pyshell.pyShell
import com.pharbers.aqll.alcalc.alemchat.sendMessage
import com.pharbers.aqll.alcalc.alemchat.alIMUser
import com.pharbers.aqll.alcalc.alfinaldataprocess.alFileExport.alFileExport
import com.pharbers.aqll.alcalc.aljobs.aljobtrigger.alJobTrigger.{check_excel_jobs, commit_finalresult_jobs, filter_excel_jobs}
import com.pharbers.aqll.alcalc.almaxdefines.alCalcParmary
import com.pharbers.aqll.util.GetProperties._
import spray.json.DefaultJsonProtocol
import com.pharbers.aqll.alcalc.alfinaldataprocess.{alFilesExport, alSampleCheck}

import scala.concurrent.ExecutionContext

/**
  * Created by qianpeng on 2017/3/26.
  */

class alAkkaHttpFuncApi(system: ActorSystem, timeout: Timeout) extends alAkkaHttpFunc {
	implicit val requestTimeout = timeout

	implicit def executionContext = system.dispatcher
}

case class alUpBeforeItem(company: String)
case class alUploadItem(company: String,yms: String)
case class alCheckItem(company: String,filename: String)
case class alCalcItem(filename: String, company: String, uname: String)
case class alCommitItem(company: String)
case class alExportItem(datatype: String,market : List[String],staend : List[String],company : String,filetype : String, uname: String)
case class alHttpCreateIMUser(name: String, pwd: String)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
	implicit val itemFormatUpBefore = jsonFormat1(alUpBeforeItem)
	implicit val itemFormatUpload = jsonFormat2(alUploadItem)
	implicit val itemFormatCheck = jsonFormat2(alCheckItem)
	implicit val itemFormatCalc = jsonFormat3(alCalcItem)
	implicit val itemFormatCommit = jsonFormat1(alCommitItem)
	implicit val itemFormatExport = jsonFormat6(alExportItem)
	implicit val itemFormatUser = jsonFormat2(alHttpCreateIMUser)
}

trait alAkkaHttpFunc extends Directives with JsonSupport{

	implicit def executionContext: ExecutionContext

	implicit def requestTimeout: Timeout

	val routes = alTest ~ alSampleCheckDataFunc ~ alCalcDataFunc ~ alModelOperationCommitFunc ~ alFileUploadPythonFunc ~ alResultFileExportFunc ~ alFileUploadPyBefore

	def alTest = post {
		path("test") {
			complete("""{"result" : "Ok"}""")
		}
	}

	def alFileUploadPyBefore = post {
		path("uploadbefore") {
			entity(as[alUpBeforeItem]) { item =>
				println(s"company=${item.company}")
				sendMessage.send("", "", 10, "test")
				val result = pyShell(item.company,"MaximumLikelyMonth.py","").excute
				sendMessage.send("", "", 100, "test")
				val gson: Gson = new Gson()
				println(s"result=${gson.toJson(result)}")
				complete("""{"result":""" + gson.toJson(result) +"""}""")
			}
		}
	}

	def alFileUploadPythonFunc = post {
		path("uploadfile") {
			entity(as[alUploadItem]) { item =>
				println(s"company=${item.company}")
				sendMessage.send("", "", 10, "test")
				val result = pyShell(item.company,"GeneratePanelFile.py",item.yms).excute
				sendMessage.send("", "", 100, "test")
				val gson: Gson = new Gson()
				println(s"result=${gson.toJson(result)}")
				complete("""{"result":""" + gson.toJson(result) +"""}""")
			}
		}
	}

	def alSampleCheckDataFunc = post {
		path("samplecheck") {
			entity(as[alCheckItem]) {item =>
				//println(s"company = ${item.company} filename = ${item.filename}")
				//val a = alAkkaSystemGloble.system.actorSelection(singletonPaht)
				//a ! check_excel_jobs(item.company,item.filename)
				println(s"company=${item.company} filename=${item.filename}")
				sendMessage.send("", "", 10, "test")
				alSampleCheck(item.company,item.filename)
				sendMessage.send("", "", 100, "test")
				complete("""{"result" : "Ok"}""")
			}
		}
	}

	def alCalcDataFunc = post {
		path("modelcalc") {
			entity(as[alCalcItem]) {item =>
				println(s"item = ${item.company}")
				println(s"item = ${item.filename}")
				println(s"path = ${fileBase + item.company + outPut + item.filename}")
				val a = alAkkaSystemGloble.system.actorSelection(singletonPaht)
				val path = fileBase + item.company + outPut + item.filename
				a ! filter_excel_jobs(path, new alCalcParmary(item.company, item.uname), a)
				complete("""{"resule" : "Ok"}""")
			}
		}
	}
	import com.pharbers.aqll.alcalc.alfinaldataprocess.alSampleCheckCommit
	def alModelOperationCommitFunc = post {
		path("datacommit") {
			entity(as[alCommitItem]) { item =>
				println(s"item=${item.company}")
				val a = alAkkaSystemGloble.system.actorSelection(singletonPaht)
				a ! commit_finalresult_jobs(item.company)
				alSampleCheckCommit(item.company)
				complete("""{"result":"Ok"}""")
			}
		}
	}

	def alResultFileExportFunc = post {
		path("dataexport") {
			entity(as[alExportItem]) { item =>
				val alExport = new alFilesExport(item.datatype,
												item.market,
												item.staend,
												item.company,
												item.filetype,
												item.uname)
				val result = alFileExport(alExport)
				sendMessage.send("", "", 100, item.uname)
				val gson : Gson = new Gson()
				println(s"result=${gson.toJson(result)}")
				complete("""{"result":"""+gson.toJson(result)+"""}""")
			}
		}
	}

	def alCreateIMUserFunc = post {
		path("createimuser") {
			entity(as[alHttpCreateIMUser]) { item =>
				alIMUser.createUser(item.name, item.pwd)
				complete("""{"result": "OK"}""")
			}
		}
	}
}
