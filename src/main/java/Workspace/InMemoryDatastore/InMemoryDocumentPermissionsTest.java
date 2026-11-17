package Workspace.InMemoryDatastore;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InMemoryDocumentPermissionsTest {

    //View access
    @Test
    public void canAccessTest(){
        InMemoryDocumentPermissions inMemoryDocumentPermissions = new InMemoryDocumentPermissions();
        inMemoryDocumentPermissions.grant("user1","document1",Permissions.VIEW);
        Assertions.assertTrue(inMemoryDocumentPermissions.canAccess("user1", "document1", Permissions.VIEW));
    }

    //User has View access and trying to edit
    @Test
    public void canAccessTest2(){
        InMemoryDocumentPermissions inMemoryDocumentPermissions = new InMemoryDocumentPermissions();
        inMemoryDocumentPermissions.grant("user1","document1",Permissions.VIEW);
        Assertions.assertFalse(inMemoryDocumentPermissions.canAccess("user1", "document1", Permissions.EDIT));
    }

    //
}
