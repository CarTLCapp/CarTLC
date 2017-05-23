
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/dug/src/play/play-java-ebean-example/conf/routes
// @DATE:Mon May 22 22:16:19 CDT 2017


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
