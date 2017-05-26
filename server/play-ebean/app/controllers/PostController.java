package controllers;

import javax.inject.*;
import play.*;
import play.mvc.*;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import play.db.ebean.Transactional;
import models.Client;
import models.Version;

@Singleton
public class PostController extends Controller
{
	@Inject
	public PostController() {
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
		if (missing.size() > 0) {
			return missingRequest(missing);
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

	@BodyParser.Of(BodyParser.Json.class)
	public Result ping() {
		ArrayList<String> missing = new ArrayList();
		JsonNode json = request().body().asJson();
		String imei = json.findPath("imei").textValue();
		if (imei == null)
		{
			missing.add("imei");
		}
		if (missing.size() > 0) {
			return missingRequest(missing);
		}
		ObjectNode result = Json.newObject();
		result.put(Version.PROJECT, Version.get(Version.PROJECT));
		result.put(Version.COMPANY, Version.get(Version.COMPANY));
		result.put(Version.EQUIPMENT, Version.get(Version.EQUIPMENT));
		result.put(Version.NOTE, Version.get(Version.NOTE));
		return ok(result);
	}

	Result missingRequest(ArrayList<String> missing) {
		StringBuilder sbuf = new StringBuilder();
		sbuf.append("Missing fields:");
		for (String field : missing)
		{
			sbuf.append(" ");
			sbuf.append(field);
		}
		sbuf.append("\n");
		return badRequest(sbuf.toString());
	}
}
