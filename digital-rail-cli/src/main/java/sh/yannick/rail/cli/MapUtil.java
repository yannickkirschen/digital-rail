package sh.yannick.rail.cli;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MapUtil {
    public static <K, V extends Comparable<? super V>> void sort(Map<K, V> map, Comparator<? super Map.Entry<K, V>> comparator) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(comparator);

        for (Map.Entry<K, V> entry : list) {
            map.put(entry.getKey(), entry.getValue());
        }
    }
}
