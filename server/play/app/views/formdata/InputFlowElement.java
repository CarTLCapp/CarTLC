/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package views.formdata;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

import models.flow.FlowElement;
import models.flow.PromptType;

public class InputFlowElement extends Model {
    private static final long serialVersionUID = 1L;

    public String prompt;
    public String type;
    public boolean hasPrompt;
    public boolean hasImage;
    public boolean hasGenericNote;

    public InputFlowElement() {
    }

    public InputFlowElement(FlowElement flowElement) {
        hasPrompt = flowElement.hasPrompt();
        hasImage = flowElement.request_image;
        hasGenericNote = flowElement.generic_note;
        prompt = flowElement.prompt;
        type = String.valueOf(flowElement.getPromptType().getCodeString());
    }

    public PromptType getPromptType() {
        return PromptType.from(type);
    }
}

