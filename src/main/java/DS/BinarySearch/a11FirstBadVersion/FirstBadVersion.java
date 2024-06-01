package DS.BinarySearch.a11FirstBadVersion;

public class FirstBadVersion {

        public int firstBadVersion(int n) {
            int start=1;
            int end=n;
            int ans=0;
            while(start<=end){
                int mid = start+(end-start)/2;
                boolean x = isBadVersion(mid);
                if(x){
                    ans = mid;
                    end = mid-1;
                }
                else{
                    start = mid+1;
                }
            }
            return ans;
        }

    private boolean isBadVersion(int mid) {
        return false;
    }
}

