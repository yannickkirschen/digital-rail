package sh.yannick.state;

public interface ResourceListener<T, S, C extends Resource<T, S>> {
    default void onInit(State state) {
    }

    default void onDestroy() {
    }

    default void onCreate(C resource) {
    }

    default void onUpdate(C resource) {
    }

    default void onDelete(C resource) {
    }
}
