package dsl4cc.DSLrecords

class ExtractVersion {
  static boolean extractVersion(String version ) {
    String userHome = System.getProperty("user.home")
    String jarLocation = "${userHome}\\.m2\\repository\\jonkerridge\\DSL4CC"
    String gradleLocation = "${userHome}\\.gradle\\caches\\modules-2\\files-2.1\\jonkerridge\\DSL4CC"
    String folder = gradleLocation + "\\$version"
    if (new File(folder).isDirectory()) return true
      else {
        folder = jarLocation + "\\$version"
        if (new File(folder).isDirectory()) return true
        else return false
    }
  }

  static void main(String[] args) {
    String version = "1.1.1a"
    if (!extractVersion(version)) println "DSL4CC:Version $version needs to downloaded, please modify the gradle.build file"
    else println "Correct version is available: $version"
  }
}
