package perfios.rbacs.Controller.PermissionController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import perfios.rbacs.Model.Permission.Permission;
import perfios.rbacs.Repository.PermissionRepository.PermissionService;

import java.util.List;

@RestController
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @RequestMapping("homepermission")
    public String homep(){
        return "permission controller working";
    }

    @GetMapping("permission")
    public List<Permission> getAllPermission(){
        return permissionService.getAllPermissions();
    }

    @PostMapping("permission")
    public String savePermission(@RequestBody Permission permission){
        return permissionService.addPermission(permission);
    }

    @DeleteMapping("permission/{id}")
    public String deletePermission(@PathVariable int id){
        return permissionService.deletePermission(id);
    }




}
