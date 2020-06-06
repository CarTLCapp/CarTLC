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

    public int line_num;
    public String prompt;
    public String type;
    public String numImages;
    public boolean hasPrompt;

    public InputFlowElement() {
    }

    public InputFlowElement(int line_num) {
        this.line_num = line_num;
    }

    public InputFlowElement(FlowElement flowElement) {
        hasPrompt = flowElement.hasPrompt();
        prompt = flowElement.prompt;
        type = String.valueOf(flowElement.getPromptType().getCodeString());
        numImages = Integer.toString(flowElement.request_image);
        line_num = flowElement.line_num;
    }

    public PromptType getPromptType() {
        return PromptType.from(type);
    }
}

