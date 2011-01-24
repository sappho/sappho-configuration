package uk.org.sappho.configuration;

import com.google.inject.AbstractModule;

public class SimpleConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Configuration.class).to(SimpleConfiguration.class);
    }
}
