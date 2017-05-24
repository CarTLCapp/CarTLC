
// @GENERATOR:play-routes-compiler
// @SOURCE:/media/vokal/cartlc/CarTLC/server/play-ebean/conf/routes
// @DATE:Wed May 24 17:18:37 CDT 2017

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._

import _root_.controllers.Assets.Asset
import _root_.play.libs.F

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:7
  HomeController_1: controllers.HomeController,
  // @LINE:12
  ClientController_0: controllers.ClientController,
  // @LINE:92
  Assets_2: controllers.Assets,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:7
    HomeController_1: controllers.HomeController,
    // @LINE:12
    ClientController_0: controllers.ClientController,
    // @LINE:92
    Assets_2: controllers.Assets
  ) = this(errorHandler, HomeController_1, ClientController_0, Assets_2, "/")

  import ReverseRouteContext.empty

  def withPrefix(prefix: String): Routes = {
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, HomeController_1, ClientController_0, Assets_2, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix, """controllers.HomeController.index()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """client""", """controllers.ClientController.list()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """client/new""", """controllers.ClientController.create()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """client""", """controllers.ClientController.save()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """client/""" + "$" + """id<[^/]+>""", """controllers.ClientController.edit(id:Long)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """client/""" + "$" + """id<[^/]+>""", """controllers.ClientController.update(id:Long)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """client/""" + "$" + """id<[^/]+>/delete""", """controllers.ClientController.delete(id:Long)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """assets/""" + "$" + """file<.+>""", """controllers.Assets.at(path:String = "/public", file:String)"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:7
  private[this] lazy val controllers_HomeController_index0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val controllers_HomeController_index0_invoker = createInvoker(
    HomeController_1.index(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "index",
      Nil,
      "GET",
      """""",
      this.prefix + """"""
    )
  )

  // @LINE:12
  private[this] lazy val controllers_ClientController_list1_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("client")))
  )
  private[this] lazy val controllers_ClientController_list1_invoker = createInvoker(
    ClientController_0.list(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ClientController",
      "list",
      Nil,
      "GET",
      """ Users list""",
      this.prefix + """client"""
    )
  )

  // @LINE:15
  private[this] lazy val controllers_ClientController_create2_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("client/new")))
  )
  private[this] lazy val controllers_ClientController_create2_invoker = createInvoker(
    ClientController_0.create(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ClientController",
      "create",
      Nil,
      "GET",
      """ Add User""",
      this.prefix + """client/new"""
    )
  )

  // @LINE:16
  private[this] lazy val controllers_ClientController_save3_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("client")))
  )
  private[this] lazy val controllers_ClientController_save3_invoker = createInvoker(
    ClientController_0.save(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ClientController",
      "save",
      Nil,
      "POST",
      """""",
      this.prefix + """client"""
    )
  )

  // @LINE:19
  private[this] lazy val controllers_ClientController_edit4_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("client/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ClientController_edit4_invoker = createInvoker(
    ClientController_0.edit(fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ClientController",
      "edit",
      Seq(classOf[Long]),
      "GET",
      """ Edit existing user""",
      this.prefix + """client/""" + "$" + """id<[^/]+>"""
    )
  )

  // @LINE:20
  private[this] lazy val controllers_ClientController_update5_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("client/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_ClientController_update5_invoker = createInvoker(
    ClientController_0.update(fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ClientController",
      "update",
      Seq(classOf[Long]),
      "POST",
      """""",
      this.prefix + """client/""" + "$" + """id<[^/]+>"""
    )
  )

  // @LINE:23
  private[this] lazy val controllers_ClientController_delete6_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("client/"), DynamicPart("id", """[^/]+""",true), StaticPart("/delete")))
  )
  private[this] lazy val controllers_ClientController_delete6_invoker = createInvoker(
    ClientController_0.delete(fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ClientController",
      "delete",
      Seq(classOf[Long]),
      "POST",
      """ Delete a user""",
      this.prefix + """client/""" + "$" + """id<[^/]+>/delete"""
    )
  )

  // @LINE:92
  private[this] lazy val controllers_Assets_at7_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("assets/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_at7_invoker = createInvoker(
    Assets_2.at(fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Assets",
      "at",
      Seq(classOf[String], classOf[String]),
      "GET",
      """ Map static resources from the /public folder to the /assets URL path""",
      this.prefix + """assets/""" + "$" + """file<.+>"""
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:7
    case controllers_HomeController_index0_route(params) =>
      call { 
        controllers_HomeController_index0_invoker.call(HomeController_1.index())
      }
  
    // @LINE:12
    case controllers_ClientController_list1_route(params) =>
      call { 
        controllers_ClientController_list1_invoker.call(ClientController_0.list())
      }
  
    // @LINE:15
    case controllers_ClientController_create2_route(params) =>
      call { 
        controllers_ClientController_create2_invoker.call(ClientController_0.create())
      }
  
    // @LINE:16
    case controllers_ClientController_save3_route(params) =>
      call { 
        controllers_ClientController_save3_invoker.call(ClientController_0.save())
      }
  
    // @LINE:19
    case controllers_ClientController_edit4_route(params) =>
      call(params.fromPath[Long]("id", None)) { (id) =>
        controllers_ClientController_edit4_invoker.call(ClientController_0.edit(id))
      }
  
    // @LINE:20
    case controllers_ClientController_update5_route(params) =>
      call(params.fromPath[Long]("id", None)) { (id) =>
        controllers_ClientController_update5_invoker.call(ClientController_0.update(id))
      }
  
    // @LINE:23
    case controllers_ClientController_delete6_route(params) =>
      call(params.fromPath[Long]("id", None)) { (id) =>
        controllers_ClientController_delete6_invoker.call(ClientController_0.delete(id))
      }
  
    // @LINE:92
    case controllers_Assets_at7_route(params) =>
      call(Param[String]("path", Right("/public")), params.fromPath[String]("file", None)) { (path, file) =>
        controllers_Assets_at7_invoker.call(Assets_2.at(path, file))
      }
  }
}
