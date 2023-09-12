package sh.yannick.rail.interlocking.listener;

import sh.yannick.rail.api.resource.Configuration;
import sh.yannick.state.Listener;
import sh.yannick.state.ResourceListener;

@Listener(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Configuration")
public class ConfigurationListener implements ResourceListener<Configuration.Spec, Configuration.Status, Configuration> {
    @Override
    public void onCreate(Configuration resource) {
        if (!resource.getMetadata().getName().equalsIgnoreCase("configuration")) {
            resource.addError("Configuration resource must be named 'configuration'.");
        }
    }
}
