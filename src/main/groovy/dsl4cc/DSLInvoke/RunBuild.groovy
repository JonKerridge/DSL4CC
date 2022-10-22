package dsl4cc.DSLInvoke

import dsl4cc.DSLbuild.DSLBuilder
import jcsp.userIO.Ask

String dslStructPath, dslStructName, dslStructFile, emitPackageName
dslStructPath = Ask.string(" DSL4CC Build: Please specify the path to parsed application : " )
dslStructName = Ask.string(" DSL4CC Build: Please specify the name of the parsed application : ")
dslStructFile = dslStructPath + "/" + dslStructName + ".dsl4ccstruct"
emitPackageName = Ask.string("DSL4CC Build: What is the package.ClassName for the emit class object? : ")
DSLBuilder build = new DSLBuilder(dslStructFile, emitPackageName)
assert build.builder():"DSL4CC Build: Build process has failed"

