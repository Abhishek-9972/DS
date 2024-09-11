package LLD.Chess;

import java.util.*;

/**
 * S = 1 3 5 7 9
 * E = 2 4 6 8 10
 *
 * arr = {{1,2},{3,4},{5,6}}
 *
 */

/**
 * Tesco has around 3200 stores and more than 10% of stores have 800 colleagues each
 * In a store a coleague can work in multiple department. Here are shifts of a colleague in various departments:
 *     In Bakery Department from 8 to 10
 *     In Checkouts department from 10 to 12
 *     In Diary department from 14 to 19
 *
 * Given the above split of shifts provide an API or method to return different shifts of a colleague for the day after merging contigous shifts.
 * This will be exposed to the colleague in different UI and help them plan their day accordingly.
 *
 * His shifts in this case is 8 to 12 and 14 to 19
 * has context menu
 */
public class ShiftManager {

    /**
     * ManageShifts of the employee
     * @param shifts Shift Timing of an Employee
     * @return Merged Shift Timings of the employee
     */
    public List<Pair> manageShifts(List<Pair> shifts){

        if(shifts.size() == 0){
            return null;
        }

        Collections.sort(shifts, (a, b) -> Integer.compare(a.getStartTime(),b.getStartTime()));
        Stack<Pair> shiftStack = new Stack<>();

        shiftStack.push(shifts.get(0));

        for(int i=1; i<shifts.size(); i++){
            Pair currentInterval = shifts.get(i);
            Pair topInterval = shiftStack.peek();

            if(currentInterval.getStartTime() <= topInterval.getEndTime()){
                topInterval.setEndTime(Math.max(topInterval.getEndTime(), currentInterval.getEndTime()));
            }
            else{
                shiftStack.push(currentInterval);
            }
        }

        List<Pair> mergedShifts = new ArrayList<>();

        for(int i = shiftStack.size()-1; i>=0; i--){
            mergedShifts.add(shiftStack.pop());
        }

        return mergedShifts;
    }

    public static void main(String[] args) {
        ShiftManager shiftManager = new ShiftManager();

        //Scenario
        List<Pair> shifts = new ArrayList<>();
        shifts.add(new Pair(8,10));
        shifts.add(new Pair(10,12));
        shifts.add(new Pair(14,15));
        List<Pair> mergedPairs = shiftManager.manageShifts(shifts);

        //Merge Scenario 9-11, 10-12, 13-15,14-16

        List<Pair> shifts1 = new ArrayList<>();
        shifts1.add(new Pair(10,12));
        shifts1.add(new Pair(13,15));
        shifts1.add(new Pair(9,11));
        shifts1.add(new Pair(14,16));
        shiftManager.manageShifts(shifts1);

    }




}
