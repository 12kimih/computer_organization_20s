;buildInfoPackage: chisel3, version: 3.2.5, scalaVersion: 2.12.10, sbtVersion: 1.3.2
circuit SimpleSystem : 
  module SimpleAdder : 
    input clock : Clock
    input reset : Reset
    output io : {flip inputx : UInt<32>, flip inputy : UInt<32>, result : UInt<32>}
    
    node _T = add(io.inputx, io.inputy) @[simple.scala 11:28]
    node _T_1 = tail(_T, 1) @[simple.scala 11:28]
    io.result <= _T_1 @[simple.scala 11:15]
    
  module SimpleAdder_1 : 
    input clock : Clock
    input reset : Reset
    output io : {flip inputx : UInt<32>, flip inputy : UInt<32>, result : UInt<32>}
    
    node _T = add(io.inputx, io.inputy) @[simple.scala 11:28]
    node _T_1 = tail(_T, 1) @[simple.scala 11:28]
    io.result <= _T_1 @[simple.scala 11:15]
    
  module SimpleSystem : 
    input clock : Clock
    input reset : UInt<1>
    output io : {success : UInt<1>}
    
    inst adder1 of SimpleAdder @[simple.scala 18:24]
    adder1.clock <= clock
    adder1.reset <= reset
    inst adder2 of SimpleAdder_1 @[simple.scala 19:24]
    adder2.clock <= clock
    adder2.reset <= reset
    reg reg1 : UInt, clock with : (reset => (reset, UInt<1>("h00"))) @[simple.scala 21:23]
    reg reg2 : UInt, clock with : (reset => (reset, UInt<1>("h01"))) @[simple.scala 22:23]
    adder1.io.inputx <= reg1 @[simple.scala 24:22]
    adder1.io.inputy <= reg2 @[simple.scala 25:22]
    adder2.io.inputx <= adder1.io.result @[simple.scala 26:22]
    adder2.io.inputy <= UInt<2>("h03") @[simple.scala 27:22]
    reg1 <= adder1.io.result @[simple.scala 29:10]
    reg2 <= adder2.io.result @[simple.scala 30:10]
    node _T = eq(adder2.io.result, UInt<8>("h080")) @[simple.scala 32:40]
    node _T_1 = mux(_T, UInt<1>("h01"), UInt<1>("h00")) @[simple.scala 32:22]
    io.success <= _T_1 @[simple.scala 32:16]
    
