package com.pharbers.aqll.calc.maxmessages

import com.pharbers.aqll.calc.split.JobDefines

trait MaxMessageTrait

abstract class CommonMessage extends MaxMessageTrait

case class excelJobStart(map: Map[String, Any], d: (String, List[String]))
case class excelJobEnd(filename : String)

case class excelSplitStart(map: Map[String, Any])

case class startReadExcel(map: Map[String, Any])

case class checkExcelJobStart(map: Map[String, Any])
case class checkExcelJobEnd(filename : String)

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

// TODO: 注意重构
case class requestMasterAverage_sub(sum : List[(String, (Double, Double, Double))])
case class requestMasterAverage(fileName : String, subFileName : String, sum : List[(String, (Double, Double, Double))])
case class responseMasterAverage(fileName : String, sum : List[(String, Double, Double)])
