package perfios.rbacs.Repository.RoleRepository;

import org.springframework.stereotype.Service;
import perfios.rbacs.Model.RoleToPermissionType.RoleToPermissionType;
import perfios.rbacs.Model.Role_to_permission.RoleToPermission;
import perfios.rbacs.Model.Roles.Role;

import java.sql.SQLException;
import java.util.List;

@Service
public interface RoleService {
    List<Role> getAllRoles();
    String saveRole(Role role);
    String deleteRoleWithId(int id);
    List<RoleToPermission> getRolePermissionList();
    String saveRolePermission(int role_id, int permission_id);
    String deleteRolePermission(int role_id, int permission_id);
    List<RoleToPermissionType> getAllPermissionsAccess();





    List<RoleToPermissionType> getAllPermissionTypeAccessForParticularRole(String roleId);
    Boolean UpdateViewAccessForRoleAndPermission(int role_id, int permission_id, Boolean allow);
    Boolean UpdateEditAccessForRoleAndPermission(int role_id, int permission_id, Boolean allow);

    Boolean addNewPermissionToExistingRole(RoleToPermissionType newPermission) throws SQLException;
    Boolean unassignPermissionToExistingRole(int roleId, String permissionType);
}
