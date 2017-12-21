package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;
import play.Logger;

import models.*;

import java.util.List;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import play.db.ebean.Transactional;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import modules.DataErrorException;

/**
 * Manage a database of companies and their addresses.
 */
public class CompanyController extends Controller {

    private FormFactory formFactory;

    @Inject
    public CompanyController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    @Security.Authenticated(Secured.class)
    public Result list(int page, String sortBy, String order, String filter, boolean disabled) {
        return ok(views.html.company_list.render(Company.list(page, sortBy, order, filter, disabled), sortBy, order, filter, Secured.getClient(ctx()), disabled));
    }

    public Result list() {
        return list(0, "name", "asc", "", false);
    }

    @Security.Authenticated(Secured.class)
    public Result edit(Long id) {
        Form<Company> companyForm = formFactory.form(Company.class).fill(Company.find.byId(id));
        return ok(views.html.company_editForm.render(id, companyForm, Secured.getClient(ctx())));
    }

    public Result update(Long id) throws PersistenceException {
        Form<Company> companyForm = formFactory.form(Company.class).bindFromRequest();
        if (companyForm.hasErrors()) {
            return badRequest(views.html.company_editForm.render(id, companyForm, Secured.getClient(ctx())));
        }
        Transaction txn = Ebean.beginTransaction();
        try {
            Company savedCompany = Company.get(id);
            if (savedCompany != null) {
                Company newCompanyData = companyForm.get();
                CompanyName.save(newCompanyData.name);
                savedCompany.name = newCompanyData.name;
                savedCompany.street = newCompanyData.street;
                savedCompany.state = newCompanyData.state;
                savedCompany.city = newCompanyData.city;
                savedCompany.zipcode = newCompanyData.zipcode;
                savedCompany.update();
                flash("success", "Company " + companyForm.get().name + " has been updated");
                txn.commit();
                Version.inc(Version.VERSION_COMPANY);
            }
        } finally {
            txn.end();
        }
        return list();
    }

    @Security.Authenticated(Secured.class)
    public Result create() {
        Form<Company> companyForm = formFactory.form(Company.class);
        return ok(views.html.company_createForm.render(companyForm));
    }

    @Security.Authenticated(Secured.class)
    public Result save() {
        Form<Company> companyForm = formFactory.form(Company.class).bindFromRequest();
        if(companyForm.hasErrors()) {
            return badRequest(views.html.company_createForm.render(companyForm));
        }
        Client client = Secured.getClient(ctx());
        Company company = companyForm.get();
        Company savedCompany = new Company();
        if (client != null && client.id > 0) {
            savedCompany.created_by = Long.valueOf(client.id).intValue();
            savedCompany.created_by_client = true;
        }
        CompanyName.save(company.name);
        savedCompany.name = company.name;
        savedCompany.street = company.street;
        savedCompany.state = company.state;
        savedCompany.city = company.city;
        savedCompany.zipcode = company.zipcode;
        savedCompany.save();
        flash("success", "Company " + companyForm.get().name + " has been created");
        return list();
    }

    @Security.Authenticated(Secured.class)
    public Result createMany() {
        Form<InputLines> linesForm = formFactory.form(InputLines.class);
        return ok(views.html.companies_createForm.render(linesForm));
    }

    @Transactional
    public Result saveMany() {
        Form<InputLines> linesForm = formFactory.form(InputLines.class).bindFromRequest();
        if (linesForm.hasErrors()) {
            return badRequest(views.html.companies_createForm.render(linesForm));
        }
        try {
            String[] lines = linesForm.get().getLines();
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    Company company = Company.parse(line);
                    if (Company.has(company) == null) {
                        company.created_by = 0;
                        company.save();
                    }
                }
            }
            Version.inc(Version.VERSION_COMPANY);
        } catch (DataErrorException ex) {
            linesForm.withError("lines", ex.getMessage());
            return badRequest(views.html.companies_createForm.render(linesForm));
        }
        return list();
    }

    public Result query() {
        JsonNode json = request().body().asJson();
        JsonNode value = json.findValue("tech_id");
        if (value == null) {
            return badRequest("missing field: tech_id");
        }
        int tech_id = value.intValue();
        ObjectNode top = Json.newObject();
        ArrayNode array = top.putArray("companies");
        for (Company item : Company.appList(tech_id)) {
            ObjectNode node = array.addObject();
            node.put("id", item.id);
            node.put("name", item.getName());
            if (item.street != null && !item.street.isEmpty()) {
                node.put("street", item.street);
            }
            if (item.city != null && !item.city.isEmpty()) {
                node.put("city", item.city);
            }
            if (item.state != null && !item.state.isEmpty()) {
                node.put("state", State.getFull(item.state));
            }
            if (item.zipcode != null && !item.zipcode.isEmpty()) {
                node.put("zipcode", item.zipcode);
            }
            if (item.created_by != 0) {
                node.put("is_local", true);
            }
        }
        return ok(top);
    }

    @Transactional
    public Result delete(Long id) {
        if (!Secured.isAdmin(ctx())) {
            return badRequest(views.html.home.render(Secured.getClient(ctx())));
        }
        if (Entry.hasEntryForCompany(id)) {
            Company company = Company.get(id);
            company.disabled = true;
            company.update();
            flash("success", "Company has been disabled: it had entries");
        } else {
            Company.delete(id);
            flash("success", "Company has been deleted");
        }
        Version.inc(Version.VERSION_COMPANY);
        return list();
    }

    @Security.Authenticated(Secured.class)
    @Transactional
    public Result enable(Long id) {
        if (!Secured.isAdmin(ctx())) {
            return badRequest(views.html.home.render(Secured.getClient(ctx())));
        }
        Company company = Company.get(id);
        company.disabled = false;
        company.update();
        Version.inc(Version.VERSION_COMPANY);
        return list();
    }
}

