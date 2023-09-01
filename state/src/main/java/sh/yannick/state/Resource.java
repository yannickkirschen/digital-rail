package sh.yannick.state;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Resource<T, S> {
    private String apiVersion;
    private String kind;
    private Metadata metadata;
    private T spec;
    private S status;
    private List<String> errors;

    public void addError(String error, Object... arguments) {
        if (errors == null) {
            errors = new LinkedList<>();
        }

        errors.add(error.formatted(arguments));
    }

    public void addErrors(List<String> errors) {
        if (this.errors == null) {
            this.errors = new LinkedList<>();
        }

        this.errors.addAll(errors);
    }

    public void addLabel(String key, String value) {
        metadata.getLabels().put(key, value);
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BaseClass {
        String apiVersion();

        String kind();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SpecDefinition {
        String apiVersion();

        String kind();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface StatusDefinition {
        String apiVersion();

        String kind();
    }
}
