package controllers;

import javax.inject.*;
import play.*;
import play.mvc.*;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;

import play.db.ebean.Transactional;
import models.Client;

@Singleton
public class RegisterController extends Controller
{
	@Inject
	public RegisterController() {
	}

	@Transactional
	@BodyParser.Of(BodyParser.Json.class)
	public Result register() {
		JsonNode json = request().body().asJson();
		ArrayList<String> missing = new ArrayList();
		String first_name = json.findPath("first_name").textValue();
		if (first_name == null)
		{
			missing.add("first_name");
		}
		String last_name = json.findPath("last_name").textValue();
		if (last_name == null)
		{
			missing.add("last_name");
		}
		String imei = json.findPath("imei").textValue();
		if (imei == null)
		{
			missing.add("imei");
		}
		StringBuilder sbuf = new StringBuilder();

		if (missing.size() > 0) {
			sbuf.append("Missing fields:");
			for (String field : missing)
			{
				sbuf.append(" ");
				sbuf.append(field);
			}
			sbuf.append("\n");
			return badRequest(sbuf.toString());
		}
		Client client;
		try {
			client = Client.findByImei(imei);
			if (client == null) {
				client = new Client();
			}
		} catch (Exception ex) {
			return badRequest(ex.getMessage());
		}
		client.first_name = first_name;
		client.last_name = last_name;
		client.imei = imei;
		client.save();

		return ok(Long.toString(client.id));
	}

}
