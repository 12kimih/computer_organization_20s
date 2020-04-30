circuit ALUControl :
  module ALUControl :
    input clock : Clock
    input reset : UInt<1>
    input io_add : UInt<1>
    input io_immediate : UInt<1>
    input io_funct7 : UInt<7>
    input io_funct3 : UInt<3>
    output io_operation : UInt<4>
  
    node _T = eq(UInt<1>("h0"), io_funct7) @[Conditional.scala 37:30]
    node _T_1 = eq(UInt<1>("h0"), io_funct3) @[Conditional.scala 37:30]
    node _T_2 = eq(UInt<1>("h1"), io_funct3) @[Conditional.scala 37:30]
    node _T_3 = eq(UInt<2>("h2"), io_funct3) @[Conditional.scala 37:30]
    node _T_4 = eq(UInt<2>("h3"), io_funct3) @[Conditional.scala 37:30]
    node _T_5 = eq(UInt<3>("h4"), io_funct3) @[Conditional.scala 37:30]
    node _T_6 = eq(UInt<3>("h5"), io_funct3) @[Conditional.scala 37:30]
    node _T_7 = eq(UInt<3>("h6"), io_funct3) @[Conditional.scala 37:30]
    node _T_8 = eq(UInt<3>("h7"), io_funct3) @[Conditional.scala 37:30]
    node _GEN_0 = mux(_T_8, UInt<1>("h0"), UInt<4>("hf")) @[Conditional.scala 39:67]
    node _GEN_1 = mux(_T_7, UInt<1>("h1"), _GEN_0) @[Conditional.scala 39:67]
    node _GEN_2 = mux(_T_6, UInt<3>("h7"), _GEN_1) @[Conditional.scala 39:67]
    node _GEN_3 = mux(_T_5, UInt<4>("h9"), _GEN_2) @[Conditional.scala 39:67]
    node _GEN_4 = mux(_T_4, UInt<3>("h5"), _GEN_3) @[Conditional.scala 39:67]
    node _GEN_5 = mux(_T_3, UInt<3>("h4"), _GEN_4) @[Conditional.scala 39:67]
    node _GEN_6 = mux(_T_2, UInt<3>("h6"), _GEN_5) @[Conditional.scala 39:67]
    node _GEN_7 = mux(_T_1, UInt<2>("h2"), _GEN_6) @[Conditional.scala 40:58]
    node _T_9 = eq(UInt<6>("h20"), io_funct7) @[Conditional.scala 37:30]
    node _T_10 = eq(UInt<1>("h0"), io_funct3) @[Conditional.scala 37:30]
    node _T_11 = eq(UInt<3>("h5"), io_funct3) @[Conditional.scala 37:30]
    node _GEN_8 = mux(_T_11, UInt<4>("h8"), UInt<4>("hf")) @[Conditional.scala 39:67]
    node _GEN_9 = mux(_T_10, UInt<2>("h3"), _GEN_8) @[Conditional.scala 40:58]
    node _GEN_10 = mux(_T_9, _GEN_9, UInt<4>("hf")) @[Conditional.scala 39:67]
    node _GEN_11 = mux(_T, _GEN_7, _GEN_10) @[Conditional.scala 40:58]
    io_operation <= _GEN_11 @[alucontrol.scala 31:16 alucontrol.scala 39:24 alucontrol.scala 43:24 alucontrol.scala 47:24 alucontrol.scala 51:24 alucontrol.scala 55:24 alucontrol.scala 59:24 alucontrol.scala 63:24 alucontrol.scala 67:24 alucontrol.scala 75:24 alucontrol.scala 79:24]
