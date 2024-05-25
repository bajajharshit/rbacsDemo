package perfios.rbacs.Controller.RoleController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import perfios.rbacs.Model.Role_to_permission.RoleToPermission;
import perfios.rbacs.Model.Roles.Role;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.RoleRepository.RoleService;

import java.util.List;

@CrossOrigin
@RestController
public class RoleController {

    @Autowired
    private RoleService roleService;


    //THIS IS A CHECK FUNCTIN WHETHER CODE IS WORKING ON LOCALHOST:8080
    @RequestMapping("/home")
    public String home(){
        return "this is home page";
//        RbacsApplication.check1();

    }

    //this is to display role details (role_id, role_name, role_status)
    @GetMapping("roles")
    public List<Role> getAllRoles(){
        return roleService.getAllRoles();
    }

    //this is to add a new role into role_details table
    @PostMapping("roles")
    public String saveRole(@RequestBody Role role){
        return roleService.saveRole(role);
    }

    //this is to delete a role in role_detiails table
    @DeleteMapping("roles/{id}")
    public String deleteRole(@PathVariable int id ){
        return roleService.deleteRoleWithId(id);
    }

    //--------------------BELOW DECLARATIONS ARE FOR ROLE_TO_PERMISSION TABLE :----------------------------

    //this is to get list of which role has which permissions(in terms of role_id and permission_id)
    @GetMapping("rolepermission")
    public List<RoleToPermission> getRolePermissionList(){
        return roleService.getRolePermissionList();
    }

    //this is for adding a new permission to existing role (in terms of role_id and permission _id)
    @PostMapping("role/{role_id}/permission/{permission_id}")
    public String saveRolePermission(@PathVariable int role_id, @PathVariable int permission_id){
        return roleService.saveRolePermission(role_id, permission_id);
    }

    //this is for deleting permission for a role in terms of role_id and permissoin_id in role_to_permisson
    @DeleteMapping("role/{role_id}/permission/{permission_id}")
    public String deleteRolePermission(@PathVariable int role_id,@PathVariable int permission_id){
        return roleService.deleteRolePermission(role_id,permission_id);
    }


    @GetMapping("rolepermission/{role_id}")
    public List<String> getPermissionsForParticularRole(@PathVariable int role_id){
        return roleService.getPermissionForParticularRole(role_id);
    }
}
