package com.pharbers.aqll.excel.exception

import scala.Exception

import com.pharbers.aqll.util.errorcode.ErrorCode._
import com.pharbers.aqll.util.errorcode.ErrorTrait

//java.lang.Exception
case class ReadFileException(name: String) extends Exception

case class ExcelDataException(msg: ErrorTrait) extends Exception
