package org.jenkinsci.plugins.version;

import hudson.Launcher;
import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author Patrik Boström
 */
public class GenerateVersionBuilder extends Builder {

    private final String template;
    private boolean setDisplayName = false;

    @DataBoundConstructor
    public GenerateVersionBuilder(String template, boolean setDisplayName) {
        this.template = template;
        this.setDisplayName = setDisplayName;
    }

    public boolean getSetDisplayName() {
        return setDisplayName;
    }

    public String getTemplate() {
        return template;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        try {
            String version = TokenMacro.expand(build, listener, template);
            ParametersAction action = new ParametersAction(new StringParameterValue("BUILD_VERSION", version));
            build.addAction(action);
            listener.getLogger().println("Setting build version to: " + version);
            if (setDisplayName) {
                build.setDisplayName(version);
            }
            return true;
        } catch (MacroEvaluationException e) {
            listener.getLogger().println(e.getMessage());
            return false;
        } catch (IOException e) {
            listener.getLogger().println(e.getMessage());
            return false;
        } catch (InterruptedException e) {
            listener.getLogger().println(e.getMessage());
            return false;
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Generate version";
        }

    }
}

