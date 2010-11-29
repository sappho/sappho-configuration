package uk.org.sappho.code.heatmap.app;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import uk.org.sappho.code.heatmap.config.Configuration;
import uk.org.sappho.code.heatmap.config.impl.ConfigurationFile;
import uk.org.sappho.code.heatmap.engine.CodeHeatMapEngine;
import uk.org.sappho.code.heatmap.issues.IssueManagement;
import uk.org.sappho.code.heatmap.report.Report;
import uk.org.sappho.code.heatmap.scm.SCM;
import uk.org.sappho.code.heatmap.warnings.Warnings;
import uk.org.sappho.code.heatmap.warnings.impl.WarningsList;

public class CodeHeatMapApp extends AbstractModule {

    private final String commonPropertiesFilename;
    private final String instancePropertiesFilename;
    private static final Logger LOG = Logger.getLogger(CodeHeatMapApp.class);

    public CodeHeatMapApp(String commonPropertiesFilename, String instancePropertiesFilename) {

        this.commonPropertiesFilename = commonPropertiesFilename;
        this.instancePropertiesFilename = instancePropertiesFilename;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {

        try {
            LOG.debug("Configuring plugins");
            Warnings warnings = new WarningsList();
            bind(Warnings.class).toInstance(warnings);
            ConfigurationFile config = new ConfigurationFile();
            config.load(commonPropertiesFilename);
            config.load(instancePropertiesFilename);
            bind(Configuration.class).toInstance(config);
            bind(SCM.class).to((Class<? extends SCM>) config.getPlugin("scm.plugin", "uk.org.sappho.code.heatmap.scm"));
            bind(Report.class).to(
                    (Class<? extends Report>) config.getPlugin("report.plugin", "uk.org.sappho.code.heatmap.report"));
            bind(IssueManagement.class).to(
                    (Class<? extends IssueManagement>) config.getPlugin("issues.plugin",
                            "uk.org.sappho.code.heatmap.issues"));
        } catch (Throwable t) {
            LOG.error("Unable to load plugins", t);
        }
    }

    public static void main(String[] args) {

        if (args.length == 2) {
            try {
                Injector injector = Guice.createInjector(new CodeHeatMapApp(args[0], args[1]));
                CodeHeatMapEngine engine = injector.getInstance(CodeHeatMapEngine.class);
                engine.writeReport();
            } catch (Throwable t) {
                LOG.error("Application error", t);
            }
        } else {
            LOG.info("Specify the names of a common and instance configuration file on the command line");
        }
    }
}
