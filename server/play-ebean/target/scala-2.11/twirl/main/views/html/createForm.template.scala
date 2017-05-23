
package views.html

import play.twirl.api._
import play.twirl.api.TemplateMagic._


     object createForm_Scope0 {
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

class createForm extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template1[Form[Computer],play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(computerForm: Form[Computer]):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {
import helper._
import b3.vertical.fieldConstructor

Seq[Any](format.raw/*1.32*/("""

"""),format.raw/*4.1*/("""
"""),format.raw/*6.1*/("""
"""),_display_(/*7.2*/main/*7.6*/ {_display_(Seq[Any](format.raw/*7.8*/("""
    
    """),format.raw/*9.5*/("""<h1>Add a computer</h1>
    
    """),_display_(/*11.6*/b3/*11.8*/.form(routes.HomeController.save())/*11.43*/ {_display_(Seq[Any](format.raw/*11.45*/("""
        
        """),format.raw/*13.9*/("""<fieldset>
            """),_display_(/*14.14*/b3/*14.16*/.text(computerForm("name"), '_label -> "Computer name", '_help -> "")),format.raw/*14.85*/("""
            """),_display_(/*15.14*/b3/*15.16*/.date(computerForm("introduced"), '_label -> "Introduced date", '_help -> "")),format.raw/*15.93*/("""
            """),_display_(/*16.14*/b3/*16.16*/.date(computerForm("discontinued"), '_label -> "Discontinued date", '_help -> "")),format.raw/*16.97*/("""

            """),_display_(/*18.14*/b3/*18.16*/.select(
                computerForm("company.id"), 
                options(Company.options), 
                '_label -> "Company", '_default -> "-- Choose a company --",
                '_showConstraints -> false
            )),format.raw/*23.14*/("""
        """),format.raw/*24.9*/("""</fieldset>
        
        <div class="actions">
            <input type="submit" value="Create this computer" class="btn primary"> or 
            <a href=""""),_display_(/*28.23*/routes/*28.29*/.HomeController.list()),format.raw/*28.51*/("""" class="btn">Cancel</a>
        </div>
        
    """)))}),format.raw/*31.6*/("""
    
""")))}))
      }
    }
  }

  def render(computerForm:Form[Computer]): play.twirl.api.HtmlFormat.Appendable = apply(computerForm)

  def f:((Form[Computer]) => play.twirl.api.HtmlFormat.Appendable) = (computerForm) => apply(computerForm)

  def ref: this.type = this

}


}

/**/
object createForm extends createForm_Scope0.createForm
              /*
                  -- GENERATED --
                  DATE: Tue May 23 11:40:00 CDT 2017
                  SOURCE: /media/vokal/cartlc/CarTLC/server/play-ebean/app/views/createForm.scala.html
                  HASH: 0719e5d404071075d9fab77ca35055f987395014
                  MATRIX: 763->1|939->31|967->50|994->88|1021->90|1032->94|1070->96|1106->106|1166->140|1176->142|1220->177|1260->179|1305->197|1356->221|1367->223|1457->292|1498->306|1509->308|1607->385|1648->399|1659->401|1761->482|1803->497|1814->499|2065->729|2101->738|2288->898|2303->904|2346->926|2430->980
                  LINES: 27->1|33->1|35->4|36->6|37->7|37->7|37->7|39->9|41->11|41->11|41->11|41->11|43->13|44->14|44->14|44->14|45->15|45->15|45->15|46->16|46->16|46->16|48->18|48->18|53->23|54->24|58->28|58->28|58->28|61->31
                  -- GENERATED --
              */
          