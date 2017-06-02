package controllers;

import javax.inject.*;
import play.*;
import play.mvc.*;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import play.db.ebean.Transactional;
import models.Client;
import models.Version;
import java.lang.System;
import java.util.Date;

@Singleton
public class PostController extends Controller
{
	@Inject
	public PostController() {
	}

	@BodyParser.Of(BodyParser.Json.class)
	public Result register() {
		JsonNode json = request().body().asJson();
		ArrayList<String> missing = new ArrayList<String>();
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
		String device_id = json.findPath("device_id").textValue();
		if (device_id == null)
		{
			missing.add("device_id");
		}
		if (missing.size() > 0) {
			return missingRequest(missing);
		}
        Transaction txn = Ebean.beginTransaction();
        Result res;
		try {
			Client client = Client.findByDeviceId(device_id);
			if (client == null) {
                // Locate by pure name
                client = Client.findByName(first_name, last_name);
                if (client == null) {
                    client = new Client();
                }
			}
            client.first_name = first_name;
            client.last_name = last_name;
            client.device_id = device_id;
            client.save();
            txn.commit();
            res = ok(Long.toString(client.id));
        } catch (Exception ex) {
            res = badRequest(ex.getMessage());
        } finally {
            txn.end();
        }
		return res;
	}

	@Transactional
	@BodyParser.Of(BodyParser.Json.class)
	public Result ping() {
		ArrayList<String> missing = new ArrayList<String>();
		JsonNode json = request().body().asJson();
		JsonNode node = json.findValue("device_id");
		String deviceId;
		if (node == null) {
			missing.add("device_id");
			deviceId = ""; // to get rid of warning
		} else {
			deviceId = node.textValue();
		}
		node = json.findValue("tech_id");
		int tech_id;
		if (node == null) {
			missing.add("tech_id");
			tech_id = 0; // to get rid of warning
		} else {
			tech_id = node.intValue();
		}
		if (missing.size() > 0) {
			return missingRequest(missing);
		}
		ObjectNode result = Json.newObject();
		result.put(Version.VERSION_PROJECT, Version.get(Version.VERSION_PROJECT));
		result.put(Version.VERSION_COMPANY, Version.get(Version.VERSION_COMPANY));
		result.put(Version.VERSION_EQUIPMENT, Version.get(Version.VERSION_EQUIPMENT));
		result.put(Version.VERSION_NOTE, Version.get(Version.VERSION_NOTE));

		Client client = Client.find.byId((long) tech_id);
		if (client.reset_upload) {
			result.put("reset_upload", true);
			client.reset_upload = false;
		}
		client.last_ping = new Date(System.currentTimeMillis());
		client.save();

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
