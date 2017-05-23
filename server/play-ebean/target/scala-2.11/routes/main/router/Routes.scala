
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/dug/src/play/play-java-ebean-example/conf/routes
// @DATE:Mon May 22 22:16:19 CDT 2017

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._

import _root_.controllers.Assets.Asset
import _root_.play.libs.F

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:6
  HomeController_0: controllers.HomeController,
  // @LINE:23
  Assets_1: controllers.Assets,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:6
    HomeController_0: controllers.HomeController,
    // @LINE:23
    Assets_1: controllers.Assets
  ) = this(errorHandler, HomeController_0, Assets_1, "/")

  import ReverseRouteContext.empty

  def withPrefix(prefix: String): Routes = {
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, HomeController_0, Assets_1, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix, """controllers.HomeController.index()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """computers""", """controllers.HomeController.list(p:Int ?= 0, s:String ?= "name", o:String ?= "asc", f:String ?= "")"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """computers/new""", """controllers.HomeController.create()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """computers""", """controllers.HomeController.save()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """computers/""" + "$" + """id<[^/]+>""", """controllers.HomeController.edit(id:Long)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """computers/""" + "$" + """id<[^/]+>""", """controllers.HomeController.update(id:Long)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """computers/""" + "$" + """id<[^/]+>/delete""", """controllers.HomeController.delete(id:Long)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """assets/""" + "$" + """file<.+>""", """controllers.Assets.at(path:String = "/public", file:String)"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:6
  private[this] lazy val controllers_HomeController_index0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val controllers_HomeController_index0_invoker = createInvoker(
    HomeController_0.index(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "index",
      Nil,
      "GET",
      """ Default path will just redirect to the computer list""",
      this.prefix + """"""
    )
  )

  // @LINE:9
  private[this] lazy val controllers_HomeController_list1_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("computers")))
  )
  private[this] lazy val controllers_HomeController_list1_invoker = createInvoker(
    HomeController_0.list(fakeValue[Int], fakeValue[String], fakeValue[String], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "list",
      Seq(classOf[Int], classOf[String], classOf[String], classOf[String]),
      "GET",
      """ Computers list (look at the default values for pagination parameters)""",
      this.prefix + """computers"""
    )
  )

  // @LINE:12
  private[this] lazy val controllers_HomeController_create2_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("computers/new")))
  )
  private[this] lazy val controllers_HomeController_create2_invoker = createInvoker(
    HomeController_0.create(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "create",
      Nil,
      "GET",
      """ Add computer""",
      this.prefix + """computers/new"""
    )
  )

  // @LINE:13
  private[this] lazy val controllers_HomeController_save3_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("computers")))
  )
  private[this] lazy val controllers_HomeController_save3_invoker = createInvoker(
    HomeController_0.save(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "save",
      Nil,
      "POST",
      """""",
      this.prefix + """computers"""
    )
  )

  // @LINE:16
  private[this] lazy val controllers_HomeController_edit4_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("computers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_HomeController_edit4_invoker = createInvoker(
    HomeController_0.edit(fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "edit",
      Seq(classOf[Long]),
      "GET",
      """ Edit existing computer""",
      this.prefix + """computers/""" + "$" + """id<[^/]+>"""
    )
  )

  // @LINE:17
  private[this] lazy val controllers_HomeController_update5_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("computers/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_HomeController_update5_invoker = createInvoker(
    HomeController_0.update(fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "update",
      Seq(classOf[Long]),
      "POST",
      """""",
      this.prefix + """computers/""" + "$" + """id<[^/]+>"""
    )
  )

  // @LINE:20
  private[this] lazy val controllers_HomeController_delete6_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("computers/"), DynamicPart("id", """[^/]+""",true), StaticPart("/delete")))
  )
  private[this] lazy val controllers_HomeController_delete6_invoker = createInvoker(
    HomeController_0.delete(fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "delete",
      Seq(classOf[Long]),
      "POST",
      """ Delete a computer""",
      this.prefix + """computers/""" + "$" + """id<[^/]+>/delete"""
    )
  )

  // @LINE:23
  private[this] lazy val controllers_Assets_at7_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("assets/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_at7_invoker = createInvoker(
    Assets_1.at(fakeValue[String], fakeValue[String]),
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
  
    // @LINE:6
    case controllers_HomeController_index0_route(params) =>
      call { 
        controllers_HomeController_index0_invoker.call(HomeController_0.index())
      }
  
    // @LINE:9
    case controllers_HomeController_list1_route(params) =>
      call(params.fromQuery[Int]("p", Some(0)), params.fromQuery[String]("s", Some("name")), params.fromQuery[String]("o", Some("asc")), params.fromQuery[String]("f", Some(""))) { (p, s, o, f) =>
        controllers_HomeController_list1_invoker.call(HomeController_0.list(p, s, o, f))
      }
  
    // @LINE:12
    case controllers_HomeController_create2_route(params) =>
      call { 
        controllers_HomeController_create2_invoker.call(HomeController_0.create())
      }
  
    // @LINE:13
    case controllers_HomeController_save3_route(params) =>
      call { 
        controllers_HomeController_save3_invoker.call(HomeController_0.save())
      }
  
    // @LINE:16
    case controllers_HomeController_edit4_route(params) =>
      call(params.fromPath[Long]("id", None)) { (id) =>
        controllers_HomeController_edit4_invoker.call(HomeController_0.edit(id))
      }
  
    // @LINE:17
    case controllers_HomeController_update5_route(params) =>
      call(params.fromPath[Long]("id", None)) { (id) =>
        controllers_HomeController_update5_invoker.call(HomeController_0.update(id))
      }
  
    // @LINE:20
    case controllers_HomeController_delete6_route(params) =>
      call(params.fromPath[Long]("id", None)) { (id) =>
        controllers_HomeController_delete6_invoker.call(HomeController_0.delete(id))
      }
  
    // @LINE:23
    case controllers_Assets_at7_route(params) =>
      call(Param[String]("path", Right("/public")), params.fromPath[String]("file", None)) { (path, file) =>
        controllers_Assets_at7_invoker.call(Assets_1.at(path, file))
      }
  }
}
