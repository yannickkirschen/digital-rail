package sh.yannick.rail.interlocking.track;

public class AllocationException extends Exception {
    public AllocationException(Throwable cause) {
        super(cause);
    }

    public AllocationException(String message, Object... args) {
        super(message.formatted(args));
    }
}
