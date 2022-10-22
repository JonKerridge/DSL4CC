package dsl4cc.DSLprocesses

import dsl4cc.DSLrecords.Acknowledgement
import dsl4cc.DSLrecords.EmittedObject
import dsl4cc.DSLrecords.RequestSend
import dsl4cc.DSLrecords.TerminalIndex
import groovy_jcsp.ChannelOutputList
import jcsp.lang.CSProcess
import jcsp.lang.ChannelOutput
import jcsp.net2.Any2NetChannel
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
  String className
  List <String> parameters // specific to an emitter if there is more than 1

  @Override
  void run() {
    println "Emitter worker $workerID , $parameters"

    Acknowledgement ack
    ack = new Acknowledgement(6, "Emitter-$workerID")
    toHost.write(ack )
    ack = fromHost.read() as Acknowledgement
    assert ack.ackValue == 6 :"Emitter-$workerID expected ack = 6 got ${ack.ackValue}"
    println "Emitter $workerID running "

    Class EmitClass = Class.forName(className)
    List parameterValues = ExtractParameters.extractParams(parameters)
//    println "Emit $workerID has params = $parameterValues"
    Object emitClass = EmitClass.getDeclaredConstructor().newInstance(parameterValues)
//    println "Emit $workerID class created"

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
    TerminalIndex terminalIndex = new TerminalIndex(workerID)
    workerToNode.write(terminalIndex)
    println "Emitter $workerID has terminated"
  }
}
