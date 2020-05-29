// This file is where all of the CPU components are assembled into the whole CPU

package dinocpu

import chisel3._
import chisel3.util._

/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.6 of Patterson and Hennessy
 * This follows figure 4.49
 */
class PipelinedCPU(implicit val conf: CPUConfig) extends Module {
  val io = IO(new CoreIO)

  // Bundles defining the pipeline registers and control structures

  // Everything in the register between IF and ID stages
  class IFIDBundle extends Bundle {
    val instruction = UInt(32.W)
    val pc          = UInt(32.W)
    val pcplusfour  = UInt(32.W)
  }

  // Control signals used in EX stage
  class EXControl extends Bundle {
    val add       = Bool()
    val immediate = Bool()
    val alusrc1   = UInt(2.W)
    val branch    = Bool()
    val jump      = UInt(2.W)
  }

  // Control signals used in MEM stage
  class MControl extends Bundle {
    val memread = Bool()
    val memwrite = Bool()
  }

  // Control signals used in EB stage
  class WBControl extends Bundle {
    val wen = Bool()
    val toreg = UInt(2.W)
  }

  // Everything in the register between ID and EX stages
  class IDEXBundle extends Bundle {
    val pcplusfour = UInt(32.W)
    val pc = UInt(32.W)
    val sextimm = UInt(32.W)
    val funct7 = UInt(7.W)
    val funct3 = UInt(3.W)
    val readdata1 = UInt(32.W)
    val readdata2 = UInt(32.W)
    val rs1 = UInt(5.W)
    val rs2 = UInt(5.W)
    val rd = UInt(5.W)

    val excontrol = new EXControl
    val mcontrol  = new MControl
    val wbcontrol = new WBControl
  }

  // Everything in the register between ID and EX stages
  class EXMEMBundle extends Bundle {
    val next_pc = UInt(32.W)
    val pcplusfour = UInt(32.W)
    val taken = Bool()
    val aluresult = UInt(32.W)
    val readdata2 = UInt(32.W)
    val funct3 = UInt(3.W)
    val rd = UInt(5.W)

    val mcontrol  = new MControl
    val wbcontrol = new WBControl
  }

  // Everything in the register between ID and EX stages
  class MEMWBBundle extends Bundle {
    val pcplusfour = UInt(32.W)
    val readdata = UInt(32.W)
    val aluresult = UInt(32.W)
    val rd = UInt(5.W)

    val wbcontrol = new WBControl
  }

  // All of the structures required
  val pc         = RegInit(0.U)
  val control    = Module(new Control())
  val branchCtrl = Module(new BranchControl())
  val registers  = Module(new RegisterFile())
  val aluControl = Module(new ALUControl())
  val alu        = Module(new ALU())
  val immGen     = Module(new ImmediateGenerator())
  val pcPlusFour = Module(new Adder())
  val branchAdd  = Module(new Adder())
  val forwarding = Module(new ForwardingUnit())  //pipelined only
  val hazard     = Module(new HazardUnit())      //pipelined only
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  val if_id      = RegInit(0.U.asTypeOf(new IFIDBundle))
  val id_ex      = RegInit(0.U.asTypeOf(new IDEXBundle))
  val ex_mem     = RegInit(0.U.asTypeOf(new EXMEMBundle))
  val mem_wb     = RegInit(0.U.asTypeOf(new MEMWBBundle))

  printf("Cycle=%d ", cycleCount)

  // Forward declaration of wires that connect different stages

  // For wb back to other stages
  val write_data = Wire(UInt(32.W))

  /////////////////////////////////////////////////////////////////////////////
  // FETCH STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Note: This comes from the memory stage!
  // Only update the pc if the pcwrite flag is enabled
  when(hazard.io.pcwrite === 0.U) {
    pc := pcPlusFour.io.result
  } .elsewhen(hazard.io.pcwrite === 1.U) {
    pc := ex_mem.next_pc
  } .elsewhen(hazard.io.pcwrite === 2.U) {
    pc := pc
  } .otherwise {
    pc := DontCare
  }

  // Send the PC to the instruction memory port to get the instruction
  io.imem.address := pc

  // Get the PC + 4
  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  // Fill the IF/ID register if we are not bubbling IF/ID
  // otherwise, leave the IF/ID register *unchanged*
  when(hazard.io.ifid_flush) {
    if_id := 0.U.asTypeOf(new IFIDBundle)
  } .elsewhen(hazard.io.ifid_bubble) {
    if_id := if_id
  } .otherwise {
    if_id.instruction := io.imem.instruction
    if_id.pc          := pc
    if_id.pcplusfour  := pcPlusFour.io.result
  }

  printf(p"IF/ID: $if_id\n")

  /////////////////////////////////////////////////////////////////////////////
  // ID STAGE
  /////////////////////////////////////////////////////////////////////////////

  val rs1 = if_id.instruction(19,15)
  val rs2 = if_id.instruction(24,20)

  // Send input from this stage to hazard detection unit
  hazard.io.rs1 := rs1
  hazard.io.rs2 := rs2

  // Send opcode to control
  control.io.opcode := if_id.instruction(6,0)

  // Send register numbers to the register file
  registers.io.readreg1 := rs1
  registers.io.readreg2 := rs2

  // Send the instruction to the immediate generator
  immGen.io.instruction := if_id.instruction

  // FIll the id_ex register
  when(hazard.io.idex_bubble) {
    id_ex := 0.U.asTypeOf(new IDEXBundle)
  } .otherwise {
    id_ex.pcplusfour := if_id.pcplusfour
    id_ex.pc := if_id.pc
    id_ex.sextimm := immGen.io.sextImm
    id_ex.funct7 := if_id.instruction(31,25)
    id_ex.funct3 := if_id.instruction(14,12)
    id_ex.readdata1 := registers.io.readdata1
    id_ex.readdata2 := registers.io.readdata2
    id_ex.rs1 := rs1
    id_ex.rs2 := rs2
    id_ex.rd := if_id.instruction(11,7)

    id_ex.excontrol.add := control.io.add
    id_ex.excontrol.immediate := control.io.immediate
    id_ex.excontrol.alusrc1 := control.io.alusrc1
    id_ex.excontrol.branch := control.io.branch
    id_ex.excontrol.jump := control.io.jump
    id_ex.mcontrol.memread := control.io.memread
    id_ex.mcontrol.memwrite := control.io.memwrite
    id_ex.wbcontrol.wen := control.io.regwrite
    id_ex.wbcontrol.toreg := control.io.toreg
  }

  printf("DASM(%x)\n", if_id.instruction)
  printf(p"ID/EX: $id_ex\n")

  /////////////////////////////////////////////////////////////////////////////
  // EX STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set the inputs to the hazard detection unit from this stage (SKIP FOR PART I)
  hazard.io.idex_memread := id_ex.mcontrol.memread
  hazard.io.idex_rd := id_ex.rd

  // Set the input to the forwarding unit from this stage (SKIP FOR PART I)
  forwarding.io.rs1 := id_ex.rs1
  forwarding.io.rs2 := id_ex.rs2

  // Connect the ALU control wires (line 45 of single-cycle/cpu.scala)
  aluControl.io.add := id_ex.excontrol.add
  aluControl.io.immediate := id_ex.excontrol.immediate
  aluControl.io.funct7 := id_ex.funct7
  aluControl.io.funct3 := id_ex.funct3

  // Insert the forward inputx mux here (SKIP FOR PART I)
  val forward_inputx = Wire(UInt(32.W))
  when(forwarding.io.forwardA === 0.U) {
    forward_inputx := id_ex.readdata1
  } .elsewhen(forwarding.io.forwardA === 1.U) {
    forward_inputx := ex_mem.aluresult
  } .elsewhen(forwarding.io.forwardA === 2.U) {
    forward_inputx := write_data
  } .otherwise {
    forward_inputx := DontCare
  }

  // Insert the ALU inpux mux here (line 59 of single-cycle/cpu.scala)
  when(id_ex.excontrol.alusrc1 === 0.U) {
    alu.io.inputx := forward_inputx
  } .elsewhen(id_ex.excontrol.alusrc1 === 1.U) {
    alu.io.inputx := 0.U
  } .elsewhen(id_ex.excontrol.alusrc1 === 2.U) {
    alu.io.inputx := id_ex.pc
  } .otherwise {
    alu.io.inputx := DontCare
  }

  // Insert forward inputy mux here (SKIP FOR PART I)
  val forward_inputy = Wire(UInt(32.W))
  when(forwarding.io.forwardB === 0.U) {
    forward_inputy := id_ex.readdata2
  } .elsewhen(forwarding.io.forwardB === 1.U) {
    forward_inputy := ex_mem.aluresult
  } .elsewhen(forwarding.io.forwardB === 2.U) {
    forward_inputy := write_data
  } .otherwise {
    forward_inputy := DontCare
  }

  // Input y mux (line 66 of single-cycle/cpu.scala)
  alu.io.inputy := Mux(id_ex.excontrol.immediate, id_ex.sextimm, forward_inputy)

  // Connect the branch control wire (line 54 of single-cycle/cpu.scala)
  branchCtrl.io.branch := id_ex.excontrol.branch
  branchCtrl.io.funct3 := id_ex.funct3
  branchCtrl.io.inputx := forward_inputx
  branchCtrl.io.inputy := forward_inputy

  // Set the ALU operation
  alu.io.operation := aluControl.io.operation

  // Connect the branchAdd unit
  branchAdd.io.inputx := id_ex.pc
  branchAdd.io.inputy := id_ex.sextimm

  // Set the EX/MEM register values
  when(hazard.io.exmem_bubble) {
    ex_mem := 0.U.asTypeOf(new EXMEMBundle)
  } .otherwise {
    ex_mem.next_pc := Mux(~id_ex.excontrol.jump(0), branchAdd.io.result, Cat(alu.io.result(31,1), 0.U))
    ex_mem.pcplusfour := id_ex.pcplusfour
    ex_mem.taken := branchCtrl.io.taken || id_ex.excontrol.jump(1)
    ex_mem.aluresult := alu.io.result
    ex_mem.readdata2 := forward_inputy
    ex_mem.funct3 := id_ex.funct3
    ex_mem.rd := id_ex.rd

    ex_mem.mcontrol := id_ex.mcontrol
    ex_mem.wbcontrol := id_ex.wbcontrol
  }

  printf(p"EX/MEM: $ex_mem\n")

  /////////////////////////////////////////////////////////////////////////////
  // MEM STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set data memory IO (line 71 of single-cycle/cpu.scala)
  io.dmem.address := ex_mem.aluresult
  io.dmem.writedata := ex_mem.readdata2
  io.dmem.memread := ex_mem.mcontrol.memread
  io.dmem.memwrite := ex_mem.mcontrol.memwrite
  io.dmem.maskmode := ex_mem.funct3(1,0)
  io.dmem.sext := ~ex_mem.funct3(2)

  // Send input signals to the hazard detection unit (SKIP FOR PART I)
  hazard.io.exmem_taken := ex_mem.taken

  // Send input signals to the forwarding unit (SKIP FOR PART I)
  forwarding.io.exmemrd := ex_mem.rd
  forwarding.io.exmemrw := ex_mem.wbcontrol.wen

  // Wire the MEM/WB register
  mem_wb.pcplusfour := ex_mem.pcplusfour
  mem_wb.readdata := io.dmem.readdata
  mem_wb.aluresult := ex_mem.aluresult
  mem_wb.rd := ex_mem.rd
  mem_wb.wbcontrol := ex_mem.wbcontrol

  printf(p"MEM/WB: $mem_wb\n")

  /////////////////////////////////////////////////////////////////////////////
  // WB STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set the writeback data mux (line 78 single-cycle/cpu.scala)
  when(mem_wb.wbcontrol.toreg === 0.U) {
    write_data := mem_wb.aluresult
  } .elsewhen(mem_wb.wbcontrol.toreg === 1.U) {
    write_data := mem_wb.readdata
  } .elsewhen(mem_wb.wbcontrol.toreg === 2.U) {
    write_data := mem_wb.pcplusfour
  } .otherwise {
    write_data := DontCare
  }

  // Write the data to the register file
  registers.io.writereg := mem_wb.rd
  registers.io.wen := Mux(mem_wb.rd =/= 0.U, mem_wb.wbcontrol.wen, false.B)
  registers.io.writedata := write_data

  // Set the input signals for the forwarding unit (SKIP FOR PART I)
  forwarding.io.memwbrd := mem_wb.rd
  forwarding.io.memwbrw := mem_wb.wbcontrol.wen

  printf("---------------------------------------------\n")
}
