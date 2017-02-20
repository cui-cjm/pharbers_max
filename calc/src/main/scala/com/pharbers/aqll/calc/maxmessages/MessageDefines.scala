package com.pharbers.aqll.calc.maxmessages

import com.pharbers.aqll.calc.split.JobDefines

trait MaxMessageTrait

abstract class CommonMessage extends MaxMessageTrait

case class excelJobStart(map: Map[String, Any])
case class excelJobEnd(filename : String)

case class startReadExcel(map: Map[String, Any])

case class checkResult(msg: String)

case class timeout()
case class cancel()
case class end()

case class error()

case class registerMaster()
case class freeMaster()

abstract class signJobsResult
case class canHandling() extends signJobsResult
case class masterBusy() extends signJobsResult
