package Workspace;

import java.util.*;
import java.util.stream.Collectors;

public class Work1 {
    public static void main(String[] args) {
        List<Integer> numbers =
                Arrays.asList(10, 20, 5, 30, 20, 40, 40);

        Integer secondHighest = numbers
                .stream()
                        .distinct()
                                .sorted(Collections.reverseOrder())
                                        .skip(1)
                                                .findFirst()
                                                        .orElse(null);

        Set<Integer> set = new HashSet<>();
        List<Integer> collect = numbers.stream().filter(num -> !set.add(num)).distinct().collect(Collectors.toList());
        System.out.printf(""+collect);

        Map<Integer, Long> collect3 = numbers.stream().collect(Collectors.groupingBy(num -> num, Collectors.counting()));

        List<Employee> employees = Arrays.asList(
                new Employee(1, "John", "IT", 90000),
                new Employee(2, "Alice", "HR", 60000),
                new Employee(3, "Bob", "IT", 100000),
                new Employee(4, "David", "HR", 70000),
                new Employee(5, "Steve", "Finance", 80000)
        );

        Map<String, Long> collect1 = employees.stream().collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));

        System.out.println(collect1);
        List<Integer> listOfIntegers = Arrays.asList(23,78,95,73,23,56,12,90,334,71,95,73,23,561,12,90,33);
        Map<Boolean, List<Integer>> collect2 = listOfIntegers.stream().collect(Collectors.partitioningBy(n -> n % 2 == 0));
        System.out.println(collect2);

        String inputString = "Java is an awesome programming language";
        List<String> list = Arrays.asList("Pen", "Ball", "Notebook", "Pen", "Eraser", "Stapler", "Notebook", "Sharpeners", "Stapler");
        Map<String, Long> collect4 = list.stream().collect(Collectors.groupingBy(num -> num, Collectors.counting()));
        System.out.println(collect4);

        Map<Boolean, List<Integer>> collect5 = listOfIntegers.stream().collect(Collectors.partitioningBy(n -> n % 2 == 0));
        System.out.println(collect5);

        List<Double> decimalList = Arrays.asList(12.45,9.81,45.9,12.7,89.90,34.56,11.3);
        List<Double> collect6 = decimalList.stream().sorted(Collections.reverseOrder()).collect(Collectors.toList());
        System.out.println(collect6);

        List<String> list7= Arrays.asList("Java", "Ruby", "C#", "Java", "Kotlin", "Ruby", "C++", "COBOL", "C#");
        List<String> collect7 = list7.stream().distinct().collect(Collectors.toList());
        System.out.println(collect7);

        List<String> list8 = Arrays.asList("Pen", "Ball", "Notebook", "Pen", "Eraser", "Stapler", "Notebook", "Sharpeners", "Stapler");
        Map<String, Long> collect8 = list8.stream().collect(Collectors.groupingBy(ele -> ele, Collectors.counting()));
        System.out.println(collect8);

        List<String> list9 = Arrays.asList("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten");
        String s = list9.stream().skip(list9.size()-1).findFirst().orElse(null);
        System.out.println(s);

        int i = 6786567;
        int sum = Arrays.stream(String.valueOf(i).split("")).mapToInt(Integer::parseInt).sum();
        System.out.println(sum);

        List<String> listOfStrings = Arrays.asList("One", "2wo", "3hree", "Four", "5ive", "6ix","45ro,'7ko");
        List<String> collect9 = listOfStrings.stream().filter(str -> Character.isDigit(str.charAt(0))).collect(Collectors.toList());
        System.out.println(collect9);

        List<Integer> listOfIntegers2 = Arrays.asList(89,212,11,10,21,12,34,90);
        Integer i1 = listOfIntegers2.stream().min(Comparator.naturalOrder()).orElse(null);
        Integer i2 = listOfIntegers2.stream().max(Comparator.naturalOrder()).orElse(null);




    }


}

 class Employee {
    private int id;
    private String name;
    private String department;
    private double salary;

    // Constructor, getters and setters

    public Employee(int id, String name, String department, double salary) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.salary = salary;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }
}