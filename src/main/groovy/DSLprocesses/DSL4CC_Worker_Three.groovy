package DSLprocesses

import DSLrecords.RequestSend
import DSLrecords.TerminalIndex
import groovy_jcsp.ChannelOutputList
import jcsp.lang.CSProcess
import jcsp.lang.ChannelOutput
import jcsp.net2.NetAltingChannelInput
import jcsp.net2.NetChannelOutput
import jcsp.net2.NetSharedChannelInput

class DSL4CC_Worker_Three implements CSProcess{

  // net channel connections to and from Host
  NetChannelOutput toHost     // writes to Host: fromNodes
  NetSharedChannelInput fromHost  // reads from Host: hostToNodes[i] vcn = 1

  // net channels
  ChannelOutputList outputWork
  NetChannelOutput requestWork, requestIndex
  NetAltingChannelInput inputWork, useIndex
  // internal channels to Node
  ChannelOutput workerToNode
  int workerID    // relative to the cluster not the node
  String methodName
  List <String> parameters    // String representations of the parameter values

  @Override
  void run() {
    println "Worker $workerID, $methodName, $parameters "
//    String s =  " "
//    for ( w in 0 ..< outputWork.size())
//      s = s + "\now[$w] = ${outputWork[w].getLocation()}"
//    println "Worker $workerID, $methodName, $parameters, " +
//        "\nrw = ${requestWork.getLocation()}, " +
//        "\niw = ${inputWork.getLocation()}," +
//        "\nri = ${requestIndex.getLocation()}" +
//        "\nui = ${useIndex.getLocation()}," +
//        "\n$s"

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
    List parameterValues = extractParams(parameters)
//    println "Worker $methodName $workerID has started with params $parameterValues"

//    CSTimer timer = new CSTimer()
    def inData
    RequestSend workRequest, indexRequest
    workRequest = new RequestSend(workerID)
    indexRequest = new RequestSend(workerID)
//    println "Worker $methodName $workerID sending work request $workRequest"
    requestWork.write(workRequest)
//    println "Worker $methodName $workerID has sent workRequest $workRequest"
    inData = inputWork.read()
//    println "Worker $methodName $workerID has read $inData"
    while (!(inData instanceof TerminalIndex)){
      inData.&"$methodName"(parameterValues)
//      timer.sleep(10)
      requestIndex.write(indexRequest)
      RequestSend use = (useIndex.read() as RequestSend)
      outputWork[use.index].write(inData)
      requestWork.write(workRequest)
      inData = inputWork.read()
    }
    // a termination has been read, tell the node
//    println "Worker $methodName $workerID has read termination"
    TerminalIndex terminalIndex = new TerminalIndex(workerID)
    workerToNode.write(terminalIndex)
//    println "Worker $workerID with $methodName has terminated"
  }
}
