package dinocpu
import chisel3._
import chisel3.util._

class SimpleAdder extends Module {
    val io = IO(new Bundle{
        val inputx = Input(UInt(32.W))
        val inputy = Input(UInt(32.W))
        val result = Output(UInt(32.W))
    })
    io.result := io.inputx + io.inputy
}

class SimpleSystem extends Module {
    val io = IO(new Bundle{
        val success = Output(Bool())
    })
    val adder1 = Module(new SimpleAdder)
    val adder2 = Module(new SimpleAdder)

    val reg1 = RegInit(0.U)
    val reg2 = RegInit(1.U)

    adder1.io.inputx := reg1
    adder1.io.inputy := reg2
    adder2.io.inputx := adder1.io.result
    adder2.io.inputy := 3.U

    reg1 := adder1.io.result
    reg2 := adder2.io.result

    io.success := Mux(adder2.io.result === 128.U, true.B, false.B)
}
