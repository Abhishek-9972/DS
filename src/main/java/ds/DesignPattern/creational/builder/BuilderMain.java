package ds.DesignPattern.creational.builder;

public class BuilderMain {
    public static void main(String[] args) {
        Account account = new Account.Builder().id(1).name("Account1").address("Addr1").email("A@a.com").build();
        System.out.println(account.getEmail());
    }
}
