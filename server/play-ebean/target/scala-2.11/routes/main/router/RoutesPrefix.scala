
// @GENERATOR:play-routes-compiler
// @SOURCE:/media/vokal/cartlc/CarTLC/server/play-ebean/conf/routes
// @DATE:Tue May 23 11:40:00 CDT 2017


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
