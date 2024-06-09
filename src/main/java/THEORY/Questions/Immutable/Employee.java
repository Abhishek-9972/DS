package THEORY.Questions.Immutable;

public final class Employee
{
    private final String pancardNumber;
    public Employee(String pancardNumber)
    {
        this.pancardNumber=pancardNumber;
    }
    public String getPancardNumber(){
        return pancardNumber;
    }
}
