package perfios.rbacs.Controller.PermissionController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import perfios.rbacs.Model.Permission.Permission;
import perfios.rbacs.Repository.PermissionRepository.PermissionService;

import java.util.List;

@RestController
@CrossOrigin
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    //this is a checking methord.
    @RequestMapping("homepermission")
    public String homep(){
        return "permission controller working";
    }


    //this is for getting list of all permission as permission_id and permissoin_name
    @GetMapping("permission")
    public List<Permission> getAllPermission(){
        return permissionService.getAllPermissions();
    }

    //this is to add the permssion into permission table
    @PostMapping("permission")
    public String savePermission(@RequestBody Permission permission){
        return permissionService.addPermission(permission);
    }

    //this is to delete a permission with the help of it's permission_id
    @DeleteMapping("permission/{id}")
    public String deletePermission(@PathVariable int id){
        return permissionService.deletePermission(id);
    }




}
