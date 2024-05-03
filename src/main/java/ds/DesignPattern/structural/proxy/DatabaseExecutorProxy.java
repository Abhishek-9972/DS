package ds.DesignPattern.structural.proxy;

public class DatabaseExecutorProxy extends DatabaseExecuterImpl {
    boolean ifAdmin;
    DatabaseExecuterImpl dbExecuter;

    public DatabaseExecutorProxy(String name, String passwd) {
        if(name == "Admin" && passwd == "Admin@123") {
            ifAdmin = true;
        }
        dbExecuter = new DatabaseExecuterImpl();
    }

    @Override
    public void executeDatabase(String query) throws Exception {
        if(ifAdmin) {
            dbExecuter.executeDatabase(query);
        } else {
            if(query.equals("DELETE")) {
                throw new Exception("DELETE not allowed for non-admin user");
            } else {
                dbExecuter.executeDatabase(query);
            }
        }
    }
}
