package controllers;

import javax.inject.*;
import play.*;
import play.mvc.*;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;

import services.Counter;

@Singleton
public class RegisterController extends Controller {

    @Inject
    public RegisterController() {
    }

    public Result register() {
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Expecting Json data");
        } else {
            ArrayList<String> missing = new ArrayList();
            String first_name = json.findPath("first_name").textValue();
            if(first_name == null) {
                missing.add("first_name");
            }
            String last_name = json.findPath("first_name").textValue();
            if (last_name == null) {
                missing.add("first_name");
            }
            String imei = json.findPath("imei").textValue();
            if (imei == null) {
                missing.add("imei");
            }
            if (missing.size()  > 0) {
                StringBuilder sbuf = new StringBuilder();
                sbuf.append("Missing fields:" );
                for (String field : missing) {
                    sbuf.append(" ");
                    sbuf.append(field);
                }
                return badRequest(sbuf.toString());
            } else {
                return ok("Hello " + first_name + " " + last_name + " ON " + imei);
            }
        }
    }

}
