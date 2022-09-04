package BuildTests

def directName = EmitTest1.getName()
println " Direct: $directName"
String className = 'EmitTest1'
def actualName = className.getName()
println "actual name = $actualName"
