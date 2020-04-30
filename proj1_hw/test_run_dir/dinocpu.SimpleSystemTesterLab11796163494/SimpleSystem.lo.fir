circuit SimpleSystem :
  module SimpleAdder :
    input clock : Clock
    input reset : UInt<1>
    input io_inputx : UInt<32>
    input io_inputy : UInt<32>
    output io_result : UInt<32>
  
    node _T = add(io_inputx, io_inputy) @[simple.scala 11:28]
    node _T_1 = tail(_T, 1) @[simple.scala 11:28]
    io_result <= _T_1 @[simple.scala 11:15]

  module SimpleSystem :
    input clock : Clock
    input reset : UInt<1>
    output io_success : UInt<1>
  
    inst adder1 of SimpleAdder @[simple.scala 18:24]
    inst adder2 of SimpleAdder @[simple.scala 19:24]
    reg reg1 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), reg1) @[simple.scala 21:23]
    reg reg2 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), reg2) @[simple.scala 22:23]
    node _T = eq(adder2.io_result, UInt<8>("h80")) @[simple.scala 32:40]
    node _T_1 = mux(_T, UInt<1>("h1"), UInt<1>("h0")) @[simple.scala 32:22]
    io_success <= _T_1 @[simple.scala 32:16]
    adder1.clock <= clock
    adder1.reset <= reset
    adder1.io_inputx <= reg1 @[simple.scala 24:22]
    adder1.io_inputy <= reg2 @[simple.scala 25:22]
    adder2.clock <= clock
    adder2.reset <= reset
    adder2.io_inputx <= adder1.io_result @[simple.scala 26:22]
    adder2.io_inputy <= UInt<2>("h3") @[simple.scala 27:22]
    reg1 <= mux(reset, UInt<1>("h0"), adder1.io_result) @[simple.scala 29:10]
    reg2 <= mux(reset, UInt<1>("h1"), adder2.io_result) @[simple.scala 30:10]
