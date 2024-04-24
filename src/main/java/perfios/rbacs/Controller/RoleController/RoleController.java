package perfios.rbacs.Controller.RoleController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import perfios.rbacs.Model.Role_to_permission.RoleToPermission;
import perfios.rbacs.Model.Roles.Role;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.RoleRepository.RoleService;

import java.util.List;

@RestController
public class RoleController {

    @Autowired
    private RoleService roleService;


    @RequestMapping("/home")
    public String home(){
        RbacsApplication.check1();
        return "this is home page";
//        RbacsApplication.check1();

    }

    @GetMapping("roles")
    public List<Role> getAllRoles(){
        return roleService.getAllRoles();
    }

    @PostMapping("roles")
    public String saveRole(@RequestBody Role role){
        return roleService.saveRole(role);
    }

    @DeleteMapping("roles/{id}")
    public String deleteRole(@PathVariable int id ){
        return roleService.deleteRoleWithId(id);
    }

    @GetMapping("rolepermission")
    public List<RoleToPermission> getRolePermissionList(){
        return roleService.getRolePermissionList();
    }

    @PostMapping("rolepermission")
    public String saveRolePermission(@RequestBody RoleToPermission roleToPermission){
        return roleService.saveRolePermission(roleToPermission);
    }

    @DeleteMapping("role/{role_id}/permission/{permission_id}")
    public String deleteRolePermission(@PathVariable int role_id,@PathVariable int permission_id){
        return roleService.deleteRolePermission(role_id,permission_id);
    }
}
