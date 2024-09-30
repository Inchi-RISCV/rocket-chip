package smartVector

import chisel3._
import chisel3.util._
import darecreek.exu.vfu._
import org.chipsalliance.cde.config
import org.chipsalliance.cde.config.{Config, Field, Parameters}
import firrtl.Utils
import darecreek.exu.vfu.div.VDiv
import xiangshan.Redirect
import darecreek.exu.vfu.perm.Permutation


class VPermWrapper (implicit p : Parameters) extends Module {

  val io = IO(new Bundle {
    val in = Input(new VPermInput)
    val redirect = Input(ValidIO(new Redirect))
    val out = Output(new VPermOutput)
  })

  val vPerm = Module(new Permutation)
  vPerm.io.in := io.in
  vPerm.io.redirect := io.redirect
  io.out := vPerm.io.out

}
