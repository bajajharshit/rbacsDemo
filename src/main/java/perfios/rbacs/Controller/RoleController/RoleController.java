package perfios.rbacs.Controller.RoleController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import perfios.rbacs.Model.Permission.Permission;
import perfios.rbacs.Model.RoleToPermissionType.RoleToPermissionType;
import perfios.rbacs.Model.Role_to_permission.RoleToPermission;
import perfios.rbacs.Model.Roles.Role;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.Redis.Access;
import perfios.rbacs.Repository.Redis.RedisDataService;
import perfios.rbacs.Repository.RoleRepository.RoleService;

import java.sql.SQLException;
import java.util.List;

@CrossOrigin
@RestController
public class RoleController {

    @Autowired
    private RoleService roleService;


    @Autowired
    private RedisDataService redisDataService;


    @PostMapping("/newpermission")
    public String addNewPermissionTypeToExistingRole(@RequestBody RoleToPermissionType newPermission) throws SQLException {

        RbacsApplication.printString(newPermission.toString());
        Boolean check = roleService.addNewPermissionToExistingRole(newPermission);
        if(check) {
            String permissionId = redisDataService.getPermissionId(newPermission.getPermissionName());
            Access access = new Access();
            access.setCanView(newPermission.getCanView());
            access.setCanEdit(newPermission.getCanEdit());
            redisDataService.savePermissionAccessToRedis(newPermission.getRoleId(),permissionId,access);
            return "successfully added";
        }
        else return "400badrequest";


    }

    @DeleteMapping("/deletepermission/{permission_name}/forrole/{role_id}")
    public String deletePermissionForRole(@PathVariable String permission_name, @PathVariable int role_id){
        Boolean check = roleService.unassignPermissionToExistingRole(role_id,permission_name);
        if(check) {
            redisDataService.deletePermissionForRole(String.valueOf(role_id), redisDataService.getPermissionId(permission_name));
            return "success";

        }
        return "bad request";
    }









//    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    @GetMapping("/permission-type-access/{roleId}")
    public List<RoleToPermissionType> getAllPermissionAccessForRole(@PathVariable String roleId){
        return roleService.getAllPermissionTypeAccessForParticularRole(roleId);
    }

//    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    @PutMapping("permission/{permissionName}/role/{roleId}/view/{canView}")
    public String updateViewAccessForPermissionType(@PathVariable String permissionName, @PathVariable int roleId, @PathVariable Boolean canView){
        String pId = redisDataService.getPermissionId(permissionName);
        if(pId == null) return "Invalid permission passed !!! " ;
        int permissionId = Integer.valueOf(pId);
        Boolean check = roleService.UpdateViewAccessForRoleAndPermission(roleId,permissionId,canView);
        if(check){
            Access access = redisDataService.getPermissionAccessFromRedis(String.valueOf(roleId),pId);
            access.setCanView(canView);
            redisDataService.savePermissionAccessToRedis(String.valueOf(roleId),pId,access);
            return "Success";
        }
        return "Failed to Update";
    }

//    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    @PutMapping("permission/{permissionName}/role/{roleId}/edit/{canEdit}")
    public String updateEditAccessForPermissionType(@PathVariable String permissionName, @PathVariable int roleId, @PathVariable Boolean canEdit){
        String pId = redisDataService.getPermissionId(permissionName);
        if(pId == null) return "invalid permission passed";
        int permissionId = Integer.valueOf(pId);
        Boolean check = roleService.UpdateEditAccessForRoleAndPermission(roleId,permissionId,canEdit);
        if(check){
            Access access = redisDataService.getPermissionAccessFromRedis(String.valueOf(roleId),pId);
            access.setCanView(canEdit);
            redisDataService.savePermissionAccessToRedis(String.valueOf(roleId),pId,access);
            return "Success";
        }
        return "Failed to Update";
    }




















//--------------------------------------below methords are for previous table layout----------------------------


    //THIS IS A CHECK FUNCTIN WHETHER CODE IS WORKING ON LOCALHOST:8080
    @RequestMapping("/home")
    public String home(){
        return "this is home page";
//        RbacsApplication.check1();

    }

    //this is to display role details (role_id, role_name, role_status)
    @GetMapping("/roles")
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





}
