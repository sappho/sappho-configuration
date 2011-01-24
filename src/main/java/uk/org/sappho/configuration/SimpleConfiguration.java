package uk.org.sappho.configuration;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.google.inject.Module;
import com.google.inject.Singleton;

@Singleton
public class SimpleConfiguration implements Configuration {

    private final Properties properties = new Properties(System.getProperties());
    private Properties snapshotProperties = null;
    private final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(this.getClass().getClassLoader());
    private static final Logger log = Logger.getLogger(SimpleConfiguration.class);

    public SimpleConfiguration() {

        log.info("Using standard Java style plain text property file configuration");
    }

    public String getProperty(String name) throws ConfigurationException {

        String value = properties.getProperty(name);
        if (value == null) {
            throw new ConfigurationException("Configuration parameter " + name + " is missing");
        }
        return value;
    }

    public String getProperty(String name, String defaultValue) {

        return properties.getProperty(name, defaultValue);
    }

    public List<String> getPropertyList(String name) throws ConfigurationException {

        List<String> list = getPropertyList(name, null);
        if (list == null) {
            throw new ConfigurationException("Configuration parameter " + name + ".1 is missing");
        }
        return list;
    }

    public List<String> getPropertyList(String name, List<String> defaultValue) {

        List<String> list = new Vector<String>();
        int index = 1;
        while (true) {
            String value = getProperty(name + "." + index++, null);
            if (value == null) {
                break;
            }
            list.add(value);
        }
        if (list.size() == 0) {
            list = defaultValue;
        }
        return list;
    }

    public Module getGuiceModule(String name) throws ConfigurationException {

        String className = getProperty(name);
        Module module;
        try {
            Class<?> moduleClass = Class.forName(className);
            module = (Module) moduleClass.newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Unable to load module " + className
                        + " specified in configuration parameter " + name, e);
        }
        return module;
    }

    public Object getGroovyScriptObject(String name) throws ConfigurationException {

        Object object = null;
        String filename = getProperty(name);
        File file = new File(filename);
        try {
            object = groovyClassLoader.parseClass(file).newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Unable to load Groovy script from " + filename, e);
        }
        return object;
    }

    public void setProperty(String name, String value) {

        properties.setProperty(name, value);
    }

    public void load(String filename) throws ConfigurationException {

        log.info("Loading configuration from " + filename);
        try {
            Reader reader = new FileReader(filename);
            properties.load(reader);
            reader.close();
        } catch (IOException e) {
            throw new ConfigurationException("Unable to load configuration from " + filename, e);
        }
    }

    public void takeSnapshot() {

        snapshotProperties = new Properties();
        for (Object nameObj : properties.keySet()) {
            String name = (String) nameObj;
            String value = properties.getProperty(name);
            snapshotProperties.setProperty(name, value);
        }
    }

    public void saveChanged(String filenameKey) throws ConfigurationException {

        String filename = getProperty(filenameKey, null);
        if (filename != null) {
            if (snapshotProperties != null) {
                Properties changedProperties = new Properties();
                for (Object nameObj : properties.keySet()) {
                    String name = (String) nameObj;
                    String value = properties.getProperty(name);
                    String originalValue = snapshotProperties.getProperty(name);
                    if (originalValue == null || !originalValue.equals(value)) {
                        changedProperties.setProperty(name, value);
                    }
                }
                if (changedProperties.size() != 0) {
                    log.info("Saving changed configuration items to " + filename);
                    try {
                        Writer writer = new FileWriter(filename);
                        changedProperties.store(writer, null);
                        writer.close();
                    } catch (IOException e) {
                        throw new ConfigurationException("Unable to save changed configuration to " + filename, e);
                    }
                }
                snapshotProperties = null;
            } else {
                log.debug("Attempt to save property changes without taking a snapshot first");
            }
        } else {
            log.info("configuration property " + filenameKey + " not specified so not saving property updates");
        }
    }
}
