package main;

import java.util.*;

public class ListSubstitution {

    public static void main(String[] args) {
        List<Integer> inputList = Arrays.asList(new Integer[] {1,3,1,2,1,3,4});
        Set<Integer> valuesToSubstitute = new HashSet<>(Arrays.asList(new Integer[] {1,2,8}));
        System.out.println(replace(inputList, valuesToSubstitute, 6));
        System.out.println(inputList);
    }


    public static Map<Integer, Double> replace(List<Integer> input, Set<Integer> toSubstitute, Integer substitute) {
        Map<Integer, Double> map = new HashMap<>();
        for (Integer number : toSubstitute) {
            if (input.contains(number)) {
                double count = Collections.frequency(input, number);
                map.put(number, count);
                Collections.replaceAll(input, number, substitute);
            }
        }
        return map;
    }
}
