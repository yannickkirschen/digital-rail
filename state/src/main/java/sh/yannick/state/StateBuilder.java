package sh.yannick.state;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class StateBuilder {
    private final Map<ResourceKey, Resource<?, ?>> resources = new HashMap<>();
    private final Map<SpecKey, Class<? extends Resource<?, ?>>> baseClasses = new HashMap<>();
    private final Map<SpecKey, Class<?>> specDefinitions = new HashMap<>();
    private final Map<SpecKey, Class<?>> statusDefinitions = new HashMap<>();
    private final Map<SpecKey, ResourceListener<?, ?, ?>> listeners = new HashMap<>();

    private String name = "unnamed";
    private boolean restricted = false;

    public StateBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public StateBuilder restricted() {
        restricted = false;
        return this;
    }

    public StateBuilder restricted(boolean restricted) {
        this.restricted = restricted;
        return this;
    }

    @SuppressWarnings("unchecked")
    public StateBuilder withPackages(String... packagePrefixes) {
        log.info("Scanning packages {} for bases classes, spec/status definitions and listeners.", Arrays.toString(packagePrefixes));

        ConfigurationBuilder builder = new ConfigurationBuilder();
        for (String packagePrefix : packagePrefixes) {
            builder.addUrls(ClasspathHelper.forPackage(packagePrefix, State.class.getClassLoader()));
        }
        Reflections reflections = new Reflections(builder.setScanners(Scanners.TypesAnnotated));

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(Resource.BaseClass.class)) {
            Resource.BaseClass baseClass = clazz.getAnnotation(Resource.BaseClass.class);
            if (Resource.class.isAssignableFrom(clazz)) {
                baseClasses.put(new SpecKey(baseClass.apiVersion(), baseClass.kind()), (Class<? extends Resource<?, ?>>) clazz); // Unchecked, but we know it's safe
                log.debug("Registered base class {} for {}/{}", clazz.getName(), baseClass.apiVersion(), baseClass.kind());
            } else {
                throw new IllegalArgumentException("Class " + clazz.getName() + " is annotated with @BaseClass but does not extend Resource<?, ?>");
            }
        }

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(Resource.SpecDefinition.class)) {
            Resource.SpecDefinition specDefinition = clazz.getAnnotation(Resource.SpecDefinition.class);
            specDefinitions.put(new SpecKey(specDefinition.apiVersion(), specDefinition.kind()), clazz);
            log.debug("Registered spec definition {} for {}/{}", clazz.getName(), specDefinition.apiVersion(), specDefinition.kind());
        }

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(Resource.StatusDefinition.class)) {
            Resource.StatusDefinition statusDefinition = clazz.getAnnotation(Resource.StatusDefinition.class);
            statusDefinitions.put(new SpecKey(statusDefinition.apiVersion(), statusDefinition.kind()), clazz);
            log.debug("Registered status definition {} for {}/{}", clazz.getName(), statusDefinition.apiVersion(), statusDefinition.kind());
        }

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(Listener.class)) {
            if (ResourceListener.class.isAssignableFrom(clazz)) {
                try {
                    Listener listenerDefinition = clazz.getAnnotation(Listener.class);
                    ResourceListener<?, ?, ?> listener = ((Class<ResourceListener<?, ?, ?>>) clazz).getConstructor().newInstance();// Unchecked, but we know it's safe
                    listeners.put(new SpecKey(listenerDefinition.apiVersion(), listenerDefinition.kind()), listener);
                    log.debug("Registered listener {} for {}/{}", clazz.getName(), listenerDefinition.apiVersion(), listenerDefinition.kind());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new IllegalArgumentException(e);
                }

            } else {
                throw new IllegalArgumentException("Class " + clazz.getName() + " is annotated with @Listener but does not implement ResourceListener<?, ?>");
            }
        }

        log.info("Registered {} base classes, {} spec definitions, {} status definitions and {} listeners", baseClasses.size(), specDefinitions.size(), statusDefinitions.size(), listeners.size());

        return this;
    }

    public State build() {
        return new State(name, restricted, resources, baseClasses, specDefinitions, statusDefinitions, listeners);
    }
}
