package dsl4cc.DSLprocesses

import dsl4cc.DSLrecords.Acknowledgement
import dsl4cc.DSLrecords.EmitInterface
import dsl4cc.DSLrecords.RequestSend
import dsl4cc.DSLrecords.TerminalIndex
import groovy_jcsp.ChannelOutputList
import jcsp.lang.CSProcess
import jcsp.lang.ChannelOutput
import jcsp.net2.NetAltingChannelInput
import jcsp.net2.NetChannelOutput
import jcsp.net2.NetSharedChannelInput
import dsl4cc.DSLrecords.ExtractParameters
import jcsp.net2.NetSharedChannelOutput

class DSL4CC_Emitter implements CSProcess{

  // net channel connections to and from Host
  NetSharedChannelOutput toHost     // writes to Host: fromNodes
  NetSharedChannelInput fromHost  // reads from Host: hostToNodes[i] vcn = 1

  // net channels
  NetChannelOutput requestIndex
  NetAltingChannelInput useIndex
  ChannelOutputList outputWork
  // internal channels to Node
  ChannelOutput workerToNode
  int workerID    // relative to the cluster
//  String className
  Class<?> classDef
  List <String> parameters // specific to an emitter if there is more than 1

  @Override
  void run() {
    long startTime, elapsed
    Acknowledgement ack
    ack = new Acknowledgement(6, "Emitter-$workerID")
    toHost.write(ack )
    startTime = System.currentTimeMillis()
    ack = fromHost.read() as Acknowledgement
    assert ack.ackValue == 6 :"Emitter-$workerID expected ack = 6 got ${ack.ackValue}"
//    println "Emitter $workerID running "

//    Class EmitClass = Class.forName(className)
    List parameterValues = ExtractParameters.extractParams(parameters)
    println "Emit $workerID has params = $parameterValues"
    Class[] cArg = new Class[1]
    cArg[0] = List.class
    Object emitClass = classDef.getDeclaredConstructor(cArg).newInstance(parameterValues)
//    println "Emit $workerID class created"

    def ec = (emitClass as EmitInterface).create()
    RequestSend rsIndex = new RequestSend(workerID)
//    println "Emitting $ec"
    while (ec != null) {
//      println "Emit sending index request $rsIndex"
      requestIndex.write(rsIndex)
//      println "Emitter has requested an index for $workerID"
      RequestSend indexToUse = useIndex.read() as RequestSend
//      println "Emit has read index to use $indexToUse"
      int use = indexToUse.index
//      println "emit will use index $use"
      outputWork[use].write(ec)
//      println "Emit $workerID (64) written ${ec} to input worker $use via $rsIndex"
//      timer.sleep(10)
      ec = emitClass.create()
    }
    // emitter terminating
    elapsed = System.currentTimeMillis() - startTime
    TerminalIndex terminalIndex = new TerminalIndex("Emitter",workerID, elapsed)
    workerToNode.write(terminalIndex)
//    println "Emitter $workerID has terminated after $elapsed millisecs"
  }
}
