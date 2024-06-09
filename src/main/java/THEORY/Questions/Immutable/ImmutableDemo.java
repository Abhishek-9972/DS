package THEORY.Questions.Immutable;

/**
 * once the object of the class is created its fields cannot be modified or changed.
 */
public class ImmutableDemo
{
    public static void main(String ar[])
    {
        Employee e = new Employee("ABC123");
        String s1 = e.getPancardNumber();
        System.out.println("Pancard Number: " + s1);
    }
}