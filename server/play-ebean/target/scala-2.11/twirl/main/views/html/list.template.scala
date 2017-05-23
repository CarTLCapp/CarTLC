
package views.html

import play.twirl.api._
import play.twirl.api.TemplateMagic._


     object list_Scope0 {
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

class list extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template4[com.avaje.ebean.PagedList[Computer],String,String,String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(currentPage: com.avaje.ebean.PagedList[Computer], currentSortBy: String, currentOrder: String, currentFilter: String):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {

def /*32.2*/header/*32.8*/(key:String, title:String):play.twirl.api.HtmlFormat.Appendable = {_display_(

Seq[Any](format.raw/*32.38*/("""
    """),format.raw/*33.5*/("""<th class=""""),_display_(/*33.17*/key/*33.20*/.replace(".","_")),format.raw/*33.37*/(""" """),format.raw/*33.38*/("""header """),_display_(/*33.46*/if(currentSortBy == key){/*33.72*/{if(currentOrder == "asc") "headerSortDown" else "headerSortUp"}}),format.raw/*33.136*/("""">
        <a href=""""),_display_(/*34.19*/link(0, key)),format.raw/*34.31*/("""">"""),_display_(/*34.34*/title),format.raw/*34.39*/("""</a>
    </th>
""")))};def /*6.2*/link/*6.6*/(newPage:Int, newSortBy:String) = {{
    
    var sortBy = currentSortBy
    var order = currentOrder
    
    if(newSortBy != null) {
        sortBy = newSortBy
        if(currentSortBy == newSortBy) {
            if(currentOrder == "asc") {
                order = "desc"
            } else {
                order = "asc"
            }
        } else {
            order = "asc"
        }
    }
    
    // Generate the link
    routes.HomeController.list(newPage, sortBy, order, currentFilter)
    
}};
Seq[Any](format.raw/*1.120*/("""

"""),format.raw/*5.42*/("""
"""),format.raw/*27.2*/("""

"""),format.raw/*31.37*/("""
"""),format.raw/*36.2*/("""

"""),_display_(/*38.2*/main/*38.6*/ {_display_(Seq[Any](format.raw/*38.8*/("""
    
    """),format.raw/*40.5*/("""<h1 id="homeTitle">"""),_display_(/*40.25*/Messages("computers.list.title", currentPage.getTotalRowCount)),format.raw/*40.87*/("""</h1>

    """),_display_(/*42.6*/if(flash.containsKey("success"))/*42.38*/ {_display_(Seq[Any](format.raw/*42.40*/("""
        """),format.raw/*43.9*/("""<div class="alert-message warning">
            <strong>Done!</strong> """),_display_(/*44.37*/flash/*44.42*/.get("success")),format.raw/*44.57*/("""
        """),format.raw/*45.9*/("""</div>
    """)))}),format.raw/*46.6*/(""" 

    """),format.raw/*48.5*/("""<div id="actions">
        
        <form action=""""),_display_(/*50.24*/link(0, "name")),format.raw/*50.39*/("""" method="GET">
            <input type="search" id="searchbox" name="f" value=""""),_display_(/*51.66*/currentFilter),format.raw/*51.79*/("""" placeholder="Filter by computer name...">
            <input type="submit" id="searchsubmit" value="Filter by name" class="btn primary">
        </form>
        
        <a class="btn success" id="add" href=""""),_display_(/*55.48*/routes/*55.54*/.HomeController.create()),format.raw/*55.78*/("""">Add a new computer</a>
        
    </div>
    
    """),_display_(/*59.6*/if(currentPage.getTotalRowCount == 0)/*59.43*/ {_display_(Seq[Any](format.raw/*59.45*/("""
        
        """),format.raw/*61.9*/("""<div class="well">
            <em>Nothing to display</em>
        </div>
        
    """)))}/*65.7*/else/*65.12*/{_display_(Seq[Any](format.raw/*65.13*/("""
        
        """),format.raw/*67.9*/("""<table class="computers zebra-striped">
            <thead>
                <tr>
                    """),_display_(/*70.22*/header("name", "Computer name")),format.raw/*70.53*/("""
                    """),_display_(/*71.22*/header("introduced", "Introduced")),format.raw/*71.56*/("""
                    """),_display_(/*72.22*/header("discontinued", "Discontinued")),format.raw/*72.60*/("""
                    """),_display_(/*73.22*/header("company.name", "Company")),format.raw/*73.55*/("""
                """),format.raw/*74.17*/("""</tr>
            </thead>
            <tbody>

                """),_display_(/*78.18*/for(computer <- currentPage.getList) yield /*78.54*/ {_display_(Seq[Any](format.raw/*78.56*/("""
                    """),format.raw/*79.21*/("""<tr>
                        <td><a href=""""),_display_(/*80.39*/routes/*80.45*/.HomeController.edit(computer.id)),format.raw/*80.78*/("""">"""),_display_(/*80.81*/computer/*80.89*/.name),format.raw/*80.94*/("""</a></td>
                        <td>
                            """),_display_(/*82.30*/if(computer.introduced == null)/*82.61*/ {_display_(Seq[Any](format.raw/*82.63*/("""
                                """),format.raw/*83.33*/("""<em>-</em>
                            """)))}/*84.31*/else/*84.36*/{_display_(Seq[Any](format.raw/*84.37*/("""
                                """),_display_(/*85.34*/computer/*85.42*/.introduced.format("dd MMM yyyy")),format.raw/*85.75*/("""
                            """)))}),format.raw/*86.30*/("""
                        """),format.raw/*87.25*/("""</td>
                        <td>
                            """),_display_(/*89.30*/if(computer.discontinued == null)/*89.63*/ {_display_(Seq[Any](format.raw/*89.65*/("""
                                """),format.raw/*90.33*/("""<em>-</em>
                            """)))}/*91.31*/else/*91.36*/{_display_(Seq[Any](format.raw/*91.37*/("""
                                """),_display_(/*92.34*/computer/*92.42*/.discontinued.format("dd MMM yyyy")),format.raw/*92.77*/("""
                            """)))}),format.raw/*93.30*/("""
                        """),format.raw/*94.25*/("""</td>
                        <td>
                            """),_display_(/*96.30*/if(computer.company == null)/*96.58*/ {_display_(Seq[Any](format.raw/*96.60*/("""
                                """),format.raw/*97.33*/("""<em>-</em>
                            """)))}/*98.31*/else/*98.36*/{_display_(Seq[Any](format.raw/*98.37*/("""
                                """),_display_(/*99.34*/computer/*99.42*/.company.name),format.raw/*99.55*/("""
                            """)))}),format.raw/*100.30*/("""
                        """),format.raw/*101.25*/("""</td>
                    </tr>
                """)))}),format.raw/*103.18*/("""

            """),format.raw/*105.13*/("""</tbody>
        </table>

        <div id="pagination" class="pagination">
            <ul>
                """),_display_(/*110.18*/if(currentPage.hasPrev)/*110.41*/ {_display_(Seq[Any](format.raw/*110.43*/("""
                    """),format.raw/*111.21*/("""<li class="prev">
                        <a href=""""),_display_(/*112.35*/link(currentPage.getPageIndex - 1, null)),format.raw/*112.75*/("""">&larr; Previous</a>
                    </li>
                """)))}/*114.19*/else/*114.24*/{_display_(Seq[Any](format.raw/*114.25*/("""
                    """),format.raw/*115.21*/("""<li class="prev disabled">
                        <a>&larr; Previous</a>
                    </li>
                """)))}),format.raw/*118.18*/("""
                """),format.raw/*119.17*/("""<li class="current">
                    <a>Displaying """),_display_(/*120.36*/currentPage/*120.47*/.getDisplayXtoYofZ(" to "," of ")),format.raw/*120.80*/("""</a>
                </li>
                """),_display_(/*122.18*/if(currentPage.hasNext)/*122.41*/ {_display_(Seq[Any](format.raw/*122.43*/("""
                    """),format.raw/*123.21*/("""<li class="next">
                        <a href=""""),_display_(/*124.35*/link(currentPage.getPageIndex + 1, null)),format.raw/*124.75*/("""">Next &rarr;</a>
                    </li>
                """)))}/*126.19*/else/*126.24*/{_display_(Seq[Any](format.raw/*126.25*/("""
                    """),format.raw/*127.21*/("""<li class="next disabled">
                        <a>Next &rarr;</a>
                    </li>
                """)))}),format.raw/*130.18*/("""
            """),format.raw/*131.13*/("""</ul>
        </div>
        
    """)))}),format.raw/*134.6*/("""
        
""")))}),format.raw/*136.2*/("""

            """))
      }
    }
  }

  def render(currentPage:com.avaje.ebean.PagedList[Computer],currentSortBy:String,currentOrder:String,currentFilter:String): play.twirl.api.HtmlFormat.Appendable = apply(currentPage,currentSortBy,currentOrder,currentFilter)

  def f:((com.avaje.ebean.PagedList[Computer],String,String,String) => play.twirl.api.HtmlFormat.Appendable) = (currentPage,currentSortBy,currentOrder,currentFilter) => apply(currentPage,currentSortBy,currentOrder,currentFilter)

  def ref: this.type = this

}


}

/**/
object list extends list_Scope0.list
              /*
                  -- GENERATED --
                  DATE: Tue May 23 11:40:00 CDT 2017
                  SOURCE: /media/vokal/cartlc/CarTLC/server/play-ebean/app/views/list.scala.html
                  HASH: 81a6499f4b61ecac9df893b1e436eb72cc956eb7
                  MATRIX: 793->1|990->868|1004->874|1111->904|1143->909|1182->921|1194->924|1232->941|1261->942|1296->950|1330->976|1417->1040|1465->1061|1498->1073|1528->1076|1554->1081|1592->248|1603->252|2138->119|2167->246|2195->756|2225->866|2253->1097|2282->1100|2294->1104|2333->1106|2370->1116|2417->1136|2500->1198|2538->1210|2579->1242|2619->1244|2655->1253|2754->1325|2768->1330|2804->1345|2840->1354|2882->1366|2916->1373|2994->1424|3030->1439|3138->1520|3172->1533|3410->1744|3425->1750|3470->1774|3551->1829|3597->1866|3637->1868|3682->1886|3788->1975|3801->1980|3840->1981|3885->1999|4014->2101|4066->2132|4115->2154|4170->2188|4219->2210|4278->2248|4327->2270|4381->2303|4426->2320|4518->2385|4570->2421|4610->2423|4659->2444|4729->2487|4744->2493|4798->2526|4828->2529|4845->2537|4871->2542|4966->2610|5006->2641|5046->2643|5107->2676|5166->2717|5179->2722|5218->2723|5279->2757|5296->2765|5350->2798|5411->2828|5464->2853|5555->2917|5597->2950|5637->2952|5698->2985|5757->3026|5770->3031|5809->3032|5870->3066|5887->3074|5943->3109|6004->3139|6057->3164|6148->3228|6185->3256|6225->3258|6286->3291|6345->3332|6358->3337|6397->3338|6458->3372|6475->3380|6509->3393|6571->3423|6625->3448|6706->3497|6749->3511|6887->3621|6920->3644|6961->3646|7011->3667|7091->3719|7153->3759|7238->3825|7252->3830|7292->3831|7342->3852|7491->3969|7537->3986|7621->4042|7642->4053|7697->4086|7769->4130|7802->4153|7843->4155|7893->4176|7973->4228|8035->4268|8116->4330|8130->4335|8170->4336|8220->4357|8365->4470|8407->4483|8473->4518|8515->4529
                  LINES: 27->1|31->32|31->32|33->32|34->33|34->33|34->33|34->33|34->33|34->33|34->33|34->33|35->34|35->34|35->34|35->34|37->6|37->6|59->1|61->5|62->27|64->31|65->36|67->38|67->38|67->38|69->40|69->40|69->40|71->42|71->42|71->42|72->43|73->44|73->44|73->44|74->45|75->46|77->48|79->50|79->50|80->51|80->51|84->55|84->55|84->55|88->59|88->59|88->59|90->61|94->65|94->65|94->65|96->67|99->70|99->70|100->71|100->71|101->72|101->72|102->73|102->73|103->74|107->78|107->78|107->78|108->79|109->80|109->80|109->80|109->80|109->80|109->80|111->82|111->82|111->82|112->83|113->84|113->84|113->84|114->85|114->85|114->85|115->86|116->87|118->89|118->89|118->89|119->90|120->91|120->91|120->91|121->92|121->92|121->92|122->93|123->94|125->96|125->96|125->96|126->97|127->98|127->98|127->98|128->99|128->99|128->99|129->100|130->101|132->103|134->105|139->110|139->110|139->110|140->111|141->112|141->112|143->114|143->114|143->114|144->115|147->118|148->119|149->120|149->120|149->120|151->122|151->122|151->122|152->123|153->124|153->124|155->126|155->126|155->126|156->127|159->130|160->131|163->134|165->136
                  -- GENERATED --
              */
          