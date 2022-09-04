package DSLprocesses

import DSLrecords.EmittedObject
import DSLrecords.RequestSend
import DSLrecords.TerminalIndex
import groovy_jcsp.ChannelOutputList
import jcsp.lang.CSProcess
import jcsp.lang.CSTimer
import jcsp.lang.ChannelOutput
import jcsp.net2.NetAltingChannelInput
import jcsp.net2.NetChannelOutput
import jcsp.net2.NetSharedChannelInput

class DSL4CC_Emitter implements CSProcess{

  // net channel connections to and from Host
  NetChannelOutput toHost     // writes to Host: fromNodes
  NetSharedChannelInput fromHost  // reads from Host: hostToNodes[i] vcn = 1

  // net channels
  NetChannelOutput requestIndex
  NetAltingChannelInput useIndex
  ChannelOutputList outputWork
  // internal channels to Node
  ChannelOutput workerToNode
  int workerID    // relative to the cluster
  String className
  List <String> parameters

  @Override
  void run() {
    def extractParams = { List pList ->
//      println "params to be processed = $pList"
      List params = []
      int pointer
      pointer = 0
      int pSize = pList.size()   // each param spec comprises type-specification value
      while( pointer < pSize ){
        String pType = pList[pointer]
        pointer++
        String pString = pList[pointer]
        pointer++
//        println "param tokens = $pType :: $pString"
        switch (pType){
          case 'int':
            params << Integer.parseInt(pString)
            break
          case 'float':
            params << Float.parseFloat(pString)
            break
          case 'String':
            params << pString
            break
          case 'double':
            params << Double.parseDouble(pString)
            break
          case 'long':
            params << Long.parseLong(pString)
            break
          case 'boolean':
            params << Boolean.parseBoolean(pString)
            break
          default:
            println "Processing parameter string unexpectedly found type = $pType, value = $pString]"
            break
        } // end switch
      } // while
//      println "returned params = $params"
      return params
    } // extract params
//    String s =  " "
//    for ( w in 0 ..< outputWork.size())
//      s = s + "ow[$w] = ${outputWork[w].getLocation()}"
    println "Emitter worker $workerID , $parameters"
//        "\nri = ${requestIndex.getLocation()}" +
//        "\nui = ${useIndex.getLocation()}," +
//        "\n$s"


//    CSTimer timer = new CSTimer()

    Class EmitClass = Class.forName(className)
    List parameterValues = extractParams(parameters)
//    println "Emit params = $parameterValues"
    Object emitClass = EmitClass.newInstance(parameterValues)
//    println "emit class created"

    def ec = emitClass.create() as EmittedObject
    RequestSend rsIndex = new RequestSend(workerID)
//    println "Emitting $ec"
    while (ec.valid) {
//      println "Emit sending index request $rsIndex"
      requestIndex.write(rsIndex)
//      println "Emitter has requested an index for $workerID"
      RequestSend indexToUse = useIndex.read() as RequestSend
//      println "Emit has read index to use $indexToUse"
      int use = indexToUse.index
//      println "emit will use index $use"
      outputWork[use].write(ec.emittedObject)
//      println "sending ${ec.emittedObject} to input worker $use"
//      timer.sleep(10)
      ec = emitClass.create()
    }
    // emitter terminating
//    println "Emit $workerID has read termination"
    TerminalIndex terminalIndex = new TerminalIndex(workerID)
    workerToNode.write(terminalIndex)
    println "Emitter $workerID has terminated"
  }
}
