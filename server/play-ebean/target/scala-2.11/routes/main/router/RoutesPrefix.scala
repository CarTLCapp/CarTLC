
// @GENERATOR:play-routes-compiler
// @SOURCE:/media/vokal/cartlc/CarTLC/server/play-ebean/conf/routes
// @DATE:Wed May 24 17:18:37 CDT 2017


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
