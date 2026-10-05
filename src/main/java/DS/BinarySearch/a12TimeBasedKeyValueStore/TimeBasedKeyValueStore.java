package DS.BinarySearch.a12TimeBasedKeyValueStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * https://www.youtube.com/watch?v=RRCMJ9YtjlM
 */
class TimeBasedKeyValueStore {

    Map<String, List<Data>> map;

    public TimeBasedKeyValueStore() {
        map = new HashMap<>();
    }

    public void set(String key, String value, int timestamp) {

        if (map.containsKey(key)) {
            map.get(key).add(new Data(value, timestamp));
        } else {
            ArrayList<Data> arr = new ArrayList<>();
            arr.add(new Data(value, timestamp));
            map.put(key, arr);
        }
    }

    public String get(String key, int timestamp) {

        String result = "";

        if (map.containsKey(key)) {

            List<Data> arr = map.get(key);

            int start = 0;
            int end = arr.size() - 1;

            while (start <= end) {

                int mid = start + (end - start) / 2;

                int time = arr.get(mid).timeStamp;

                if (time == timestamp) {
                    return arr.get(mid).val;

                } else if (timestamp > time) {
                    // Valid candidate → find a later timestamp
                    result = arr.get(mid).val;
                    start = mid + 1;

                } else {
                    // Timestamp is too large → go left
                    end = mid - 1;
                }
            }
        }

        return result;
    }
}

class Data {

    String val;
    int timeStamp;

    public Data(String val, int timeStamp) {
        this.val = val;
        this.timeStamp = timeStamp;
    }
}