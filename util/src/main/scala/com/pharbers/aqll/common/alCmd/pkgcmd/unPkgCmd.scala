package com.pharbers.aqll.common.alCmd.pkgcmd

import com.pharbers.aqll.common.alCmd.ShellOtherCmdExce

/**
  * Created by Alfred on 10/03/2017.
  */
case class unPkgCmd(val compress_file : String, val des_dir : String) extends ShellOtherCmdExce {
   override val cmd = s"tar -xzvf ${compress_file}.tar.gz -C ${des_dir}"
}