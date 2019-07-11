/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package views.formdata;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

import models.flow.FlowElement;

public class InputFlowElement extends Model {
    private static final long serialVersionUID = 1L;

    public String toastPrompt;
    public String dialogPrompt;
    public String confirmationPrompt;
    public boolean hasToast;
    public boolean hasDialog;
    public boolean hasConfirmation;
    public boolean hasImage;
    public boolean hasGenericNote;

    public InputFlowElement() {
    }


    public InputFlowElement(FlowElement flowElement) {
        hasToast = flowElement.hasToast();
        hasDialog = flowElement.hasDialog();
        hasConfirmation = flowElement.hasConfirmation();
        hasImage = flowElement.request_image;
        hasGenericNote = flowElement.generic_note;
        toastPrompt = flowElement.getToastValue();
        dialogPrompt = flowElement.getDialogValue();
        confirmationPrompt = flowElement.getConfirmationValue();
    }
}

