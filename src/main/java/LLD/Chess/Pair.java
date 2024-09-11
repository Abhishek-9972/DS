package LLD.Chess;

/**
 * Pair to store the startTime and EndTime of the colleague's shifts
 */
public class Pair {
    private int startTime;
    private int endTime;

    public Pair(int startTime, int endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }
}
