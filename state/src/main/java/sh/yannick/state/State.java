package sh.yannick.state;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Slf4j
public class State implements Closeable {
    private final Map<ResourceKey, Resource<?, ?>> resources = new HashMap<>();
    private final Map<SpecKey, Class<? extends Resource<?, ?>>> baseClasses = new HashMap<>();
    private final Map<SpecKey, Class<?>> specDefinitions = new HashMap<>();
    private final Map<SpecKey, Class<?>> statusDefinitions = new HashMap<>();
    private final Map<SpecKey, ResourceListener<?, ?, ?>> listeners = new HashMap<>();

    private String name = "unnamed";

    private State() {
    }

    public static State getEmpty() { // TODO: use builder pattern
        return new State();
    }

    public State withName(String name) {
        this.name = name;
        return this;
    }

    @SuppressWarnings("unchecked")
    public State withPackages(String... packagePrefixes) {
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

    public State initializeListeners() {
        listeners.values().forEach(listener -> listener.onInit(this));
        return this;
    }

    public <T, S, C extends Resource<T, S>> C addResource(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Resource<T, S> resource = addResource(mapper, mapper.readTree(file));

        SpecKey key = new SpecKey(resource.getApiVersion(), resource.getKind());
        Class<C> baseClass = (Class<C>) baseClasses.get(key);
        if (baseClass == null) {
            throw new IllegalArgumentException("No base class found for apiVersion " + resource.getApiVersion() + " and kind " + resource.getKind());
        }
        return baseClass.cast(resource);
    }

    public <T, S, C extends Resource<T, S>> C addResource(File file, Class<C> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return clazz.cast(addResource(mapper, mapper.readTree(file)));
    }

    public <T, S, C extends Resource<T, S>> C addResource(File file, JsonFactory factory, Class<C> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper(factory);
        return clazz.cast(addResource(mapper, mapper.readTree(file)));
    }

    public <T, S, C extends Resource<T, S>> C addResource(InputStream in, JsonFactory factory) throws IOException {
        ObjectMapper mapper = new ObjectMapper(factory);
        return addResource(mapper, mapper.readTree(in));
    }

    public <T, S, C extends Resource<T, S>> C addResource(String json) throws IOException {
        return addResource(json, true);
    }

    public <T, S, C extends Resource<T, S>> C addResource(String json, boolean triggerUpdate) throws IOException {
        return addResource(parseResource(json), triggerUpdate);
    }

    public <T, S, C extends Resource<T, S>> C addResource(ObjectMapper mapper, JsonNode node) throws IOException {
        return addResource(parseResource(mapper, node));
    }

    public <T, S, C extends Resource<T, S>> C addResource(C resource) {
        return addResource(resource, true);
    }

    public <T, S, C extends Resource<T, S>> C addResource(C resource, boolean triggerUpdates) {
        log.info("Received resource {}/{}/{}", resource.getApiVersion(), resource.getKind(), resource.getMetadata().getName());

        ResourceKey key = new ResourceKey(resource.getApiVersion(), resource.getKind(), resource.getMetadata().getName());
        if (triggerUpdates) {
            triggerUpdates(resource, key);
        } else {
            resources.put(key, resource);
        }

        return resource;
    }

    @SuppressWarnings("unchecked")
    public <T, S, C extends Resource<T, S>> C deleteResource(String apiVersion, String kind, String name, Class<C> clazz) {
        ResourceListener<T, S, C> listener = (ResourceListener<T, S, C>) listeners.get(new SpecKey(apiVersion, kind)); // Unchecked, but we know it's safe
        if (listener == null) {
            throw new IllegalArgumentException("No listener found for apiVersion " + apiVersion + " and kind " + kind);
        }

        C resource = getResource(apiVersion, kind, name, clazz).orElseThrow(() -> new IllegalArgumentException("No resource found for apiVersion " + apiVersion + ", kind " + kind + " and name " + name));
        listener.onDelete(resource);
        resources.remove(new ResourceKey(apiVersion, kind, name));
        return resource;
    }

    public <T, S, C extends Resource<T, S>> C parseResource(String yaml) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return parseResource(mapper, mapper.readTree(yaml));
    }

    public <T, S, C extends Resource<T, S>> C parseResource(InputStream in) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return parseResource(mapper, mapper.readTree(in));
    }

    public <T, S, C extends Resource<T, S>> C parseResource(ObjectMapper mapper, JsonNode node) throws IOException {
        JsonNode apiVersionNode = node.at("/apiVersion");
        JsonNode kindNode = node.at("/kind");

        if (apiVersionNode.isMissingNode() || kindNode.isMissingNode()) {
            throw new IllegalArgumentException("Missing resource apiVersion or kind");
        }

        SpecKey key = new SpecKey(apiVersionNode.asText(), kindNode.asText());
        Class<?> resourceClass = Objects.requireNonNullElse(baseClasses.get(key), Resource.class);
        Class<?> specClass = specDefinitions.get(key);
        Class<?> statusClass = statusDefinitions.get(key);
        if (specClass == null || statusClass == null) {
            throw new IllegalArgumentException("No spec or status class found for apiVersion " + apiVersionNode.asText() + " and kind " + kindNode.asText());
        }

        JavaType javaType;
        if (resourceClass == Resource.class) {
            javaType = mapper.getTypeFactory().constructParametricType(resourceClass, specClass, statusClass);
        } else {
            javaType = mapper.getTypeFactory().constructType(resourceClass);
        }

        return mapper.treeToValue(node, javaType);
    }

    public <T, S, C extends Resource<T, S>> Optional<C> getResource(String apiVersion, String kind, String name, Class<C> clazz) {
        Resource<?, ?> resource = resources.get(new ResourceKey(apiVersion, kind, name));
        if (resource == null) {
            return Optional.empty();
        }

        Class<?> specClass = specDefinitions.get(new SpecKey(apiVersion, kind));
        Class<?> statusClass = statusDefinitions.get(new SpecKey(apiVersion, kind));
        if (specClass == null || statusClass == null) {
            throw new IllegalArgumentException("No spec or status class found for apiVersion " + apiVersion + " and kind " + kind);
        }

        return Optional.of(clazz.cast(resource));
    }

    private <T, S, C extends Resource<T, S>> void triggerUpdates(C resource, ResourceKey key) {
        ResourceListener<T, S, C> listener = (ResourceListener<T, S, C>) listeners.get(new SpecKey(resource.getApiVersion(), resource.getKind())); // Unchecked, but we know it's safe
        if (listener == null) {
            resources.put(key, resource); // Just store the resource and do nothing
        } else {
            // TODO: call those in threads
            try {
                if (!resources.containsKey(key)) {
                    resources.put(key, resource);
                    listener.onCreate(resource);
                } else {
                    resources.put(key, resource);
                    listener.onUpdate(resource);
                }
            } catch (Exception e) { // TODO: maybe too broad?
                log.error("Error while processing resource {}/{}/{}", resource.getApiVersion(), resource.getKind(), resource.getMetadata().getName(), e);
                resource.addError(e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        log.info("Shutdown hook received for state {}: destroying all resources and listeners.", name);
        listeners.values().forEach(ResourceListener::onDestroy);

        resources.clear();
        listeners.clear();
        baseClasses.clear();
        specDefinitions.clear();
        statusDefinitions.clear();

        log.info("State {} closed.", name);
    }

    @Data
    @RequiredArgsConstructor
    public static class ResourceKey {
        private final String apiVersion;
        private final String kind;
        private final String name;
    }

    @Data
    @RequiredArgsConstructor
    public static class SpecKey {
        private final String apiVersion;
        private final String kind;
    }
}
