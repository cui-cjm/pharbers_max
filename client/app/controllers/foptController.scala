package controllers

import play.api._
import play.api.mvc._

import controllers.common.requestArgsQuery.uploadRequestArgs
import controllers.common.default_error_handler.f
import com.pharbers.aqll.module.fopModule.fop

object foptController extends Controller {
	def uploadFile = Action { request => 
		uploadRequestArgs(request)(fop.uploadFile) 
	}
	def downloadFile(name : String) = Action (Ok(fop.downloadFile(name)).as("excel/xlsx"))

	def uploadHospitalDataFile = Action { request =>
		uploadRequestArgs(request)(fop.uploadHospitalDataFile)
	}

	def uploadProductMatchFile = Action { request =>
		uploadRequestArgs(request)(fop.uploadProductMatchFile)
	}

	def uploadMarketMatchFile = Action { request =>
		uploadRequestArgs(request)(fop.uploadMarketMatchFile)
	}

	def uploadHospitalMatchFile = Action { request =>
		uploadRequestArgs(request)(fop.uploadHospitalMatchFile)
	}
}