package perfios.rbacs.Repository.PermissionRepository;

import org.springframework.stereotype.Service;
import perfios.rbacs.Model.Permission.Permission;

import java.util.List;

@Service
public interface PermissionService {
    List<Permission> getAllPermissions();
    String addPermission(Permission permission);
    String deletePermission(int id);
}
