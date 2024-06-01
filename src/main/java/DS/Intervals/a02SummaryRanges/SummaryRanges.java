package DS.Intervals.a02SummaryRanges;

import java.util.ArrayList;
import java.util.List;

public class SummaryRanges {
    public List<String> summaryRanges(int[] nums) {
        List<String> result = new ArrayList<>();
        int n = nums.length;
        for(int i=0; i<n; i++){
            int start = nums[i];
            while(i+1<n && nums[i]+1 == nums[i+1]){
                i++;
            }

            if(nums[i]!=start){
                result.add(start+"->"+nums[i]);
            } else{
                result.add(""+start);
            }
        }
        return result;
    }
}