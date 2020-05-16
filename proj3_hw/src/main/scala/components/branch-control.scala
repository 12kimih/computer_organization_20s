// Control logic for whether branches are taken or not

package dinocpu

import chisel3._
import chisel3.util._

/**
 * Controls whether or not branches are taken.
 *
 * Input:  branch, true if we are looking at a branch
 * Input:  funct3, the middle three bits of the instruction (12-14). Specifies the
 *         type of branch
 * Input:  inputx, first value (e.g., reg1)
 * Input:  inputy, second value (e.g., reg2)
 * Output: taken, true if the branch is taken.
 */
class BranchControl extends Module {
  val io = IO(new Bundle {
    val branch = Input(Bool())
    val funct3 = Input(UInt(3.W))
    val inputx = Input(UInt(32.W))
    val inputy = Input(UInt(32.W))

    val taken  = Output(Bool())
  })
  io.taken := DontCare
  when(io.branch) {
    switch(io.funct3) {
      // BEQ
      is("b000".U) {io.taken := (io.inputx === io.inputy)}
      // BNE
      is("b001".U) {io.taken := (io.inputx != io.inputy)}
      // BLT
      is("b100".U) {io.taken := (io.inputx.asSInt < io.inputy.asSInt)}
      // BGE
      is("b101".U) {io.taken := (io.inputx.asSInt >= io.inputy.asSInt)}
      // BLTU
      is("b110".U) {io.taken := (io.inputx < io.inputy)}
      // BGEU
      is("b111".U) {io.taken := (io.inputx >= io.inputy)}
    }
  } .otherwise {
    io.taken := false.B
  }
}
