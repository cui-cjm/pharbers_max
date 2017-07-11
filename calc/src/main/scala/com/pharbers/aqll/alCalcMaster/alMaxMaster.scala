package com.pharbers.aqll.alCalcMaster

import akka.actor.{Actor, ActorLogging, Props}
import com.pharbers.aqll.alCalcMaster.alMasterTrait.alFilterExcelTrait
import com.pharbers.aqll.alCalcMemory.aljobs.aljobtrigger.alJobTrigger.{filter_excel_job_2, push_max_job}

/**
  * Created by alfredyang on 11/07/2017.
  */
object alMaxMaster {
    def props = Props[alMaxMaster]
    def name = "driver-actor"
}

class alMaxMaster extends Actor
                    with ActorLogging
                    with alFilterExcelTrait {

    override def receive: Receive = {
        case filter_excel_job_2(file, parmary) => filterExcel(file, parmary)
    }
}