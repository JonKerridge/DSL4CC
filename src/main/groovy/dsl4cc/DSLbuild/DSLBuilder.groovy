package dsl4cc.DSLbuild

import dsl4cc.DSLprocesses.DSL4CC_Host
import dsl4cc.DSLrecords.ParseRecord
import groovy_jcsp.PAR
import jcsp.userIO.Ask

class DSLBuilder {
  String structureFile, emitPackageName

  DSLBuilder(String structureFileName, String emitPackageName){
    structureFile = structureFileName
    this.emitPackageName = emitPackageName
  }

  DSLBuilder(){
    String dslStructPath, dslStructName, dslStructFile, emitPackageName
    dslStructPath = Ask.string(" DSL4CC Build: Please specify the path to parsed application : " )
    dslStructName = Ask.string(" DSL4CC Build: Please specify the name of the parsed application : ")
    structureFile = dslStructPath + "/" + dslStructName + ".dsl4ccstruct"
    this.emitPackageName = Ask.string("DSL4CC Build: What is the package.ClassName for the emit class object? : ")
  }

  boolean builder(){
    File objFile = new File(structureFile)
    List<ParseRecord> structure = []
    objFile.withObjectInputStream { inStream ->
      inStream.eachObject { structure << (it as ParseRecord) } }
    int requiredManagers = structure.size() - 2
    int totalNodes, totalWorkers
    totalNodes = 0
    totalWorkers = 0
    structure.each {
      totalNodes = totalNodes + it.nodes
      totalWorkers = totalWorkers + (it.nodes * it.workers)
    }
    String hostIP = structure[0].hostAddress
    Class emitClass = Class.forName(emitPackageName)
    def emitObject = emitClass.getDeclaredConstructor().newInstance()
    println "DSL4CC: Build system has created a null EmitObject instance"
    def host = new DSL4CC_Host(hostIP, requiredManagers, totalNodes, totalWorkers, structure, emitObject)
    new PAR([host]).run()
    println "DSL4CC: Build system has finished"
    return true
  }


  static void main(String[] args) {
    String dslStructPath, dslStructName, dslStructFile, emitPackageName
    if (args.size() == 0) {
      dslStructPath = Ask.string(" DSL4CC Build: Please specify the path to parsed application : ")
      dslStructName = Ask.string(" DSL4CC Build: Please specify the name of the parsed application : ")
      dslStructFile = dslStructPath + "/" + dslStructName + ".dsl4ccstruct"
      emitPackageName = Ask.string("DSL4CC Build: What is the package.ClassName for the emit class object? : ")
    }
    else {
      dslStructPath = args[0]
      dslStructName = args[1]
      dslStructFile = dslStructPath + "/" + dslStructName + ".dsl4ccstruct"
      emitPackageName = args[2]
   }
    DSLBuilder build = new DSLBuilder(dslStructFile, emitPackageName)
    assert build.builder():"DSL4CC Build: Build process has failed"
  }
}
