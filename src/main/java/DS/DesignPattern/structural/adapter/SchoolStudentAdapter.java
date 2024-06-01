package DS.DesignPattern.structural.adapter;

public class SchoolStudentAdapter implements Student {

    private SchoolStudent schoolStudent;

    public SchoolStudentAdapter(SchoolStudent schoolStudent){
        this.schoolStudent = schoolStudent;
    }


    @Override
    public String getName() {
        return this.schoolStudent.getName();
    }

    @Override
    public String getSurname() {
        return this.schoolStudent.getSurName();
    }

    @Override
    public String getEmail() {
        return this.schoolStudent.getEmail();
    }
}
