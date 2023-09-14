package sh.yannick.rail.cli;

import java.io.File;
import java.util.Comparator;
import java.util.Map;

public class ResourcePriorityComparator implements Comparator<Map.Entry<File, String>> {
    private static final String[] PRIORITIES = new String[]{
        "Configuration",
        "Raspberry",
        "Signal",
        "Switch",
        "Block",
        "Graph",
        "Allocation"
    };

    @Override
    public int compare(Map.Entry<File, String> o1, Map.Entry<File, String> o2) {
        String kind1 = o1.getValue();
        String kind2 = o2.getValue();

        for (String priority : PRIORITIES) {
            if (priority.equals(kind1)) {
                return -1;
            } else if (priority.equals(kind2)) {
                return 1;
            }
        }

        return 0;
    }
}
