package perfios.rbacs.Model.RoleToPermissionType;

import lombok.Data;

@Data
public class RoleToPermissionType {
    String permissionName;
    String permissionId;
    String roleId;
    Boolean canView;
    Boolean canEdit;
}
