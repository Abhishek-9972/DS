package Workspace.InMemoryDatastore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implement in-memory document permissions with VIEW, EDIT, and OWN. OWN implies EDIT and VIEW. EDIT implies VIEW. Access defaults to false.
 * <p>
 * Support the operations below and treat revoking an absent grant as a no-op.
 * - grant(user, doc, permission)
 * - revoke(user, doc, permission)
 * - canAccess(user, doc, permission) -> boolean
 * <p>
 * Work through the rules in order. After each rule, the code should compile and be covered by passing tests.
 */
public class InMemoryDocumentPermissions {
    private final Map<String, Map<String, Set<Permissions>>> inMemoryDocumentMap = new HashMap<>();

    public void grant(String userId, String documentId, Permissions permission) {
        if (inMemoryDocumentMap.containsKey(documentId)) {
            Map<String, Set<Permissions>> userPermissionsMap = inMemoryDocumentMap.get(documentId);
            if (userPermissionsMap.containsKey(userId)) {
                userPermissionsMap.get(userId).add(permission);
            } else {
                Map<String, Set<Permissions>> userPermissionMap = new HashMap<>();
                Set<Permissions> permissions = new HashSet<>();
                permissions.add(permission);
                userPermissionMap.put(userId, permissions);
                inMemoryDocumentMap.put(documentId, userPermissionMap);
            }

        } else {
            Map<String, Set<Permissions>> userPermissionMap = new HashMap<>();
            Set<Permissions> permissions = new HashSet<>();
            permissions.add(permission);
            userPermissionMap.put(userId, permissions);
            inMemoryDocumentMap.put(documentId, userPermissionMap);
        }
    }

    public void revoke(String userId, String documentId, Permissions permission) {
        if (inMemoryDocumentMap.containsKey(documentId)) {
            Map<String, Set<Permissions>> userPermissionsMap = inMemoryDocumentMap.get(documentId);
            if (userPermissionsMap.containsKey(userId)) {
                userPermissionsMap.get(userId).remove(permission);
            }
        }
    }

    public boolean canAccess(String userId, String documentId, Permissions permission) {
        if (inMemoryDocumentMap.containsKey(documentId)) {
            Map<String, Set<Permissions>> userPermissionsMap = inMemoryDocumentMap.get(documentId);
            if (userPermissionsMap.containsKey(userId)) {
                if (userPermissionsMap.get(userId).contains(Permissions.OWN)) {
                    return true;
                } else if (userPermissionsMap.get(userId).contains(Permissions.EDIT) &&
                        (permission.equals(Permissions.EDIT) || permission.equals(Permissions.VIEW))) {
                    return true;
                } else if (userPermissionsMap.get(userId).contains(Permissions.VIEW) &&
                        permission.equals(Permissions.VIEW)) {
                    return true;
                }
            }
        }
        return false;
    }
}
