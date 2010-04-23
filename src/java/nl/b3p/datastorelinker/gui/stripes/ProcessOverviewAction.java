/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.datastorelinker.gui.stripes;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.ValidationErrors;
import nl.b3p.commons.stripes.Transactional;

/**
 *
 * @author Erik van de Pol
 */
public class ProcessOverviewAction extends DefaultAction {
    private final static Log log = Log.getInstance(ProcessOverviewAction.class);
    private final static String JSP = "/pages/processOverview.jsp";
    private final static String NEW_PROCESS_JSP = "/pages/newProcess.jsp";

    @DefaultHandler
    @Transactional
    public Resolution processOverview() {
        ValidationErrors errors = new ValidationErrors();

        log.info("ProcessOverviewAction test");
        // doe je ding...

        if (!errors.isEmpty()) {
            getContext().setValidationErrors(errors);
            return getContext().getSourcePageResolution();
        }

        return new ForwardResolution(JSP);
    }

    public Resolution newProcess() {
        return new ForwardResolution(NEW_PROCESS_JSP);
    }

    public Resolution newProcessCreated() {
        return new ForwardResolution(JSP);
    }

    public Resolution editProcess() {
        return new ForwardResolution(JSP);
    }

    public Resolution deleteProcess() {
        return new ForwardResolution(JSP);
    }
}
