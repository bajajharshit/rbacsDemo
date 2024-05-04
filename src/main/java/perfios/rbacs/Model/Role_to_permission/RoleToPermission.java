package perfios.rbacs.Model.Role_to_permission;

import lombok.Data;

@Data
public class RoleToPermission {
    private int roleId;
    private int permissionId;
    private String roleName;
    private String permissionName;
}
