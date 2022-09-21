package DSLprocesses

import DSLrecords.RequestSend
import DSLrecords.TerminalIndex
import jcsp.lang.CSProcess
import jcsp.lang.ChannelOutput
import jcsp.net2.NetAltingChannelInput
import jcsp.net2.NetChannelOutput
import jcsp.net2.NetSharedChannelInput

class DSL4CC_Collecter implements CSProcess{

  // net channel connections to and from Host
  NetChannelOutput toHost     // writes to Host: fromNodes
  NetSharedChannelInput fromHost  // reads from Host: hostToNodes[i] vcn = 1


  // net channels
  NetChannelOutput requestWork
  NetAltingChannelInput inputWork
  // internal channels to Node
  ChannelOutput workerToNode
  int workerID    // relative to the cluster
  //TODO collect now has collect and finalise methods both with their own parameter strings
  String methodName
  List <String> parameters
  String finaliseMethodName
  List <String> finaliseParameters

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
    println "Cluster worker $workerID, $parameters"
//        "\nrw = ${requestWork.getLocation()}, " +
//        "\niw = ${inputWork.getLocation()}"

    List parameterValues, finaliseParameterValues
    parameterValues = []
    if (parameters != null )  parameterValues = extractParams(parameters)
    if (finaliseParameters != null )  finaliseParameterValues = extractParams(finaliseParameters)
    println "Collecter $workerID, $methodName, $parameterValues, $finaliseMethodName, $finaliseParameterValues"

    Class CollectClass

    def inData
    RequestSend workRequest
    workRequest = new RequestSend(workerID)
//    println "Collect $workerID has created request $workRequest"
    requestWork.write(workRequest)
//    println "Collect $workerID has sent workRequest"
    inData = inputWork.read()
//    println "Collect $workerID has read $inData"
    // get its class name and create an instance
    String className = inData.getClass().getName()
    CollectClass = Class.forName(className)
    Object collectClass = CollectClass.newInstance()
    while (!(inData instanceof TerminalIndex)) {
      inData.&"$methodName"(parameterValues)
      requestWork.write(new RequestSend(workerID))
      inData = inputWork.read()
    }
    //call the finalise method if it exists
    if (finaliseMethodName != null) collectClass.&"$finaliseMethodName"(finaliseParameterValues)

    // a termination has been read, tell the node
//    println "Collect $workerID has read termination"
    TerminalIndex terminalIndex = new TerminalIndex(workerID)
    workerToNode.write(terminalIndex)
    println "Collector $workerID has terminated"
  }
}
