package com.pharbers.aqll.calc.maxmessages

import com.pharbers.aqll.calc.split.JobDefines

trait MaxMessageTrait

abstract class CommonMessage extends MaxMessageTrait

case class excelJobStart(filename : String, cat : JobDefines, company: String, n: Int)
case class excelJobEnd(filename : String)

case class startReadExcel(filename : String, cat : JobDefines, company: String, n: Int)

case class cancel()
case class end()

case class error()