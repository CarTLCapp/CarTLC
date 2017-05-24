
package views.html

import play.twirl.api._
import play.twirl.api.TemplateMagic._


     object main_Scope0 {
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import java.lang._
import java.util._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import play.core.j.PlayMagicForJava._
import play.mvc._
import play.data._
import play.api.data.Field
import play.mvc.Http.Context.Implicit._

class main extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template2[String,Html,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(header: String)(content: Html):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.33*/("""

"""),format.raw/*3.1*/("""<!DOCTYPE html>
<html>
    <head>
        <title>FleetTLC</title>
        """),format.raw/*13.47*/("""
        """),format.raw/*14.9*/("""<link rel="stylesheet" type="text/css" media="screen" href=""""),_display_(/*14.70*/routes/*14.76*/.Assets.at("stylesheets/bootstrap.min.css")),format.raw/*14.119*/("""">
        
        <link rel="stylesheet" media="screen" href=""""),_display_(/*16.54*/routes/*16.60*/.Assets.at("stylesheets/main.css")),format.raw/*16.94*/(""""/> 
    </head>
    <body>
        
        <header class="topbar">
            <h1 class="fill">
                <h1>"""),_display_(/*22.22*/header),format.raw/*22.28*/("""</h1>
            </h1>
        </header>
        
        <section id="main">
            """),_display_(/*27.14*/content),format.raw/*27.21*/("""
        """),format.raw/*28.9*/("""</section>
        
    </body>
</html>
"""))
      }
    }
  }

  def render(header:String,content:Html): play.twirl.api.HtmlFormat.Appendable = apply(header)(content)

  def f:((String) => (Html) => play.twirl.api.HtmlFormat.Appendable) = (header) => (content) => apply(header)(content)

  def ref: this.type = this

}


}

/**/
object main extends main_Scope0.main
              /*
                  -- GENERATED --
                  DATE: Wed May 24 17:18:37 CDT 2017
                  SOURCE: /media/vokal/cartlc/CarTLC/server/play-ebean/app/views/main.scala.html
                  HASH: 5a50068d578430cbf892b3c6274a48ccc91c10a1
                  MATRIX: 748->1|874->32|902->34|1004->607|1040->616|1128->677|1143->683|1208->726|1300->791|1315->797|1370->831|1517->951|1544->957|1663->1049|1691->1056|1727->1065
                  LINES: 27->1|32->1|34->3|38->13|39->14|39->14|39->14|39->14|41->16|41->16|41->16|47->22|47->22|52->27|52->27|53->28
                  -- GENERATED --
              */
          