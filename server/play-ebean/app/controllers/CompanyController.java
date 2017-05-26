package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;
import play.Logger;

import models.*;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import play.db.ebean.Transactional;

/**
 * Manage a database of companies and their addresses.
 */
public class CompanyController extends Controller {

    private static final int PAGE_SIZE = 30;
    private FormFactory formFactory;

    @Inject
    public CompanyController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    public Result list(int page, String sortBy, String order, String filter) {
        return ok(views.html.company_list.render(Company.list(page, PAGE_SIZE, sortBy, order, filter), sortBy, order, filter));
    }

    public Result list() {
        return list(0, "name", "asc", "");
    }

    public Result edit(Long id) {
        Form<Company> companyForm = formFactory.form(Company.class).fill(Company.find.byId(id));
        return ok(views.html.company_editForm.render(id, companyForm));
    }

    public Result update(Long id) throws PersistenceException {
        Form<Company> companyForm = formFactory.form(Company.class).bindFromRequest();
        if (companyForm.hasErrors()) {
            return badRequest(views.html.company_editForm.render(id, companyForm));
        }
        Transaction txn = Ebean.beginTransaction();
        try {
            Company savedCompany = Company.find.byId(id);
            if (savedCompany != null) {
                Company newCompanyData = companyForm.get();
                savedCompany.name = newCompanyData.name;
                savedCompany.street = newCompanyData.street;
                savedCompany.state = newCompanyData.state;
                savedCompany.city = newCompanyData.city;
                savedCompany.update();
                flash("success", "Company " + companyForm.get().name + " has been updated");
                txn.commit();

                Version.inc(Version.COMPANY);
            }
        } finally {
            txn.end();
        }
        return list();
    }

    public Result create() {
        Form<Company> companyForm = formFactory.form(Company.class);
        return ok(views.html.company_createForm.render(companyForm));
    }

    public Result save() {
        Form<Company> companyForm = formFactory.form(Company.class).bindFromRequest();
        if(companyForm.hasErrors()) {
            return badRequest(views.html.company_createForm.render(companyForm));
        }
        companyForm.get().save();
        flash("success", "Company " + companyForm.get().name + " has been created");
        return list();
    }

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
                    if (!Company.has(company)) {
                        company.save();
                    }
                }
            }
            Version.inc(Version.COMPANY);
        } catch (Exception ex) {
            linesForm.reject("lines", ex.getMessage());
            return badRequest(views.html.companies_createForm.render(linesForm));
        }
        return list();
    }

    public Result delete(Long id) {
        // TODO: If the client is in the database, mark it as disabled instead.
        Company.find.ref(id).delete();
        Version.inc(Version.COMPANY);
        flash("success", "Company has been deleted");
        return list();
    }


}

