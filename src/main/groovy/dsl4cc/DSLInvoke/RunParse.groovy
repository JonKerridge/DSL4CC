package dsl4cc.DSLInvoke

import dsl4cc.DSLparse.DSLParser
import jcsp.userIO.Ask

String dslFilePath, dslName
dslFilePath = Ask.string("DSL4CC parser: Please specify the path to the ?.dsl4cc file : ")
dslName = Ask.string("DSL4CC parser: Please specify the dsl4cc file name omitting extension : ")
String inFilePath = dslFilePath + "/" + dslName + ".dsl4cc"
DSLParser parser = new DSLParser(inFilePath)
assert parser.parse():"Parsing of $inFilePath failed"


