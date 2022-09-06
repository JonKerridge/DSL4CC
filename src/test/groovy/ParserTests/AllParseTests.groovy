package ParserTests

import org.junit.runner.JUnitCore
// runs as a groovy script and provides a sequence of JUnit tests

result = JUnitCore.runClasses (
    ParseTest1,
    ParseTest2,

)

String message = "Ran: " + result.getRunCount() +
    ", Ignored: " + result.getIgnoreCount() +
    ", Failed: " + result.getFailureCount()
if (result.wasSuccessful()) {
  println "\nSUCCESS! " + message
} else {
  println "\nFAILURE! " + message + "\n"
  result.getFailures().each {
    println it.toString()
  }
}

