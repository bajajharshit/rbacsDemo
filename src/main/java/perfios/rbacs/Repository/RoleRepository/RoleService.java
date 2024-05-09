package perfios.rbacs.Repository.RoleRepository;

import org.springframework.stereotype.Service;
import perfios.rbacs.Model.Role_to_permission.RoleToPermission;
import perfios.rbacs.Model.Roles.Role;

import java.util.List;

@Service
public interface RoleService {
    List<Role> getAllRoles();
    String saveRole(Role role);
    String deleteRoleWithId(int id);
    List<RoleToPermission> getRolePermissionList();
    String saveRolePermission(int role_id, int permission_id);
    String deleteRolePermission(int role_id, int permission_id);
    List<String> getPermissionForParticularRole(int role_id);

}
