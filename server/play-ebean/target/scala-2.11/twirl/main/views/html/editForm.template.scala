
package views.html

import play.twirl.api._
import play.twirl.api.TemplateMagic._


     object editForm_Scope0 {
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

class editForm extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template2[Long,Form[Computer],play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(id: Long, computerForm: Form[Computer]):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {
import helper._
import b3.vertical.fieldConstructor  // Declares a vertical field constructor as default

Seq[Any](format.raw/*1.42*/("""

"""),format.raw/*4.1*/("""
"""),format.raw/*6.1*/("""
"""),_display_(/*7.2*/main/*7.6*/ {_display_(Seq[Any](format.raw/*7.8*/("""
    
    """),format.raw/*9.5*/("""<h1>Edit computer</h1>
    
    """),_display_(/*11.6*/b3/*11.8*/.form(routes.HomeController.update(id))/*11.47*/ {_display_(Seq[Any](format.raw/*11.49*/("""
        
        """),format.raw/*13.9*/("""<fieldset>
        
            """),_display_(/*15.14*/b3/*15.16*/.text(computerForm("name"), '_label -> "Computer name", '_help -> "")),format.raw/*15.85*/("""
            """),_display_(/*16.14*/b3/*16.16*/.date(computerForm("introduced"), '_label -> "Introduced date", '_help -> "")),format.raw/*16.93*/("""
            """),_display_(/*17.14*/b3/*17.16*/.date(computerForm("discontinued"), '_label -> "Discontinued date", '_help -> "")),format.raw/*17.97*/("""
            
            """),_display_(/*19.14*/b3/*19.16*/.select(
                computerForm("company.id"), 
                options(Company.options), 
                '_label -> "Company", '_default -> "-- Choose a company --",
                '_showConstraints -> false
            )),format.raw/*24.14*/("""
        
        """),format.raw/*26.9*/("""</fieldset>
        
        <div class="actions">
            <input type="submit" value="Save this computer" class="btn primary"> or 
            <a href=""""),_display_(/*30.23*/routes/*30.29*/.HomeController.list()),format.raw/*30.51*/("""" class="btn">Cancel</a>
        </div>
        
    """)))}),format.raw/*33.6*/("""
    
    """),_display_(/*35.6*/b3/*35.8*/.form(routes.HomeController.delete(id), 'class -> "topRight")/*35.69*/ {_display_(Seq[Any](format.raw/*35.71*/("""
        """),format.raw/*36.9*/("""<input type="submit" value="Delete this computer" class="btn danger">
    """)))}),format.raw/*37.6*/("""
    
""")))}),format.raw/*39.2*/("""
"""))
      }
    }
  }

  def render(id:Long,computerForm:Form[Computer]): play.twirl.api.HtmlFormat.Appendable = apply(id,computerForm)

  def f:((Long,Form[Computer]) => play.twirl.api.HtmlFormat.Appendable) = (id,computerForm) => apply(id,computerForm)

  def ref: this.type = this

}


}

/**/
object editForm extends editForm_Scope0.editForm
              /*
                  -- GENERATED --
                  DATE: Mon May 22 22:16:19 CDT 2017
                  SOURCE: /home/dug/src/play/play-java-ebean-example/app/views/editForm.scala.html
                  HASH: fd278f35ecca7d5e915558077b0a83e16f64d132
                  MATRIX: 764->1|1003->41|1031->60|1058->151|1085->153|1096->157|1134->159|1170->169|1229->202|1239->204|1287->243|1327->245|1372->263|1432->296|1443->298|1533->367|1574->381|1585->383|1683->460|1724->474|1735->476|1837->557|1891->584|1902->586|2153->816|2198->834|2383->992|2398->998|2441->1020|2525->1074|2562->1085|2572->1087|2642->1148|2682->1150|2718->1159|2823->1234|2860->1241
                  LINES: 27->1|33->1|35->4|36->6|37->7|37->7|37->7|39->9|41->11|41->11|41->11|41->11|43->13|45->15|45->15|45->15|46->16|46->16|46->16|47->17|47->17|47->17|49->19|49->19|54->24|56->26|60->30|60->30|60->30|63->33|65->35|65->35|65->35|65->35|66->36|67->37|69->39
                  -- GENERATED --
              */
          