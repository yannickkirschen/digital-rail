package sh.yannick.rail.cli;

public class Main {
    private static final String USAGE = """
        Usage: java -jar digital-rail-cli.jar <command>
                Commands:
                  apply
            """;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(USAGE);
            System.exit(1);
        }

        switch (args[0]) {
            case "apply" -> new ApplyHandler().apply(args);
            default -> {
                System.out.println(USAGE);
                System.exit(1);
            }
        }
    }
}
