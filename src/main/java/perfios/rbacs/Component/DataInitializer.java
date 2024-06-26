package perfios.rbacs.Component;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import perfios.rbacs.Model.Permission.Permission;
import perfios.rbacs.Model.RoleToPermissionType.RoleToPermissionType;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.PermissionRepository.PermissionService;
import perfios.rbacs.Repository.Redis.Access;
import perfios.rbacs.Repository.Redis.RedisDataService;
import perfios.rbacs.Repository.RoleRepository.RoleService;
import perfios.rbacs.Repository.UserRepository.UserService;

import java.util.List;

@Component
public class DataInitializer {




    @Autowired
    private RedisDataService redisDataService;

    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Autowired
    PermissionService permissionService;

    @PostConstruct
    public void initializeData() {
        userService.fillRoleDetails();
        List<RoleToPermissionType> allPermissionAccess = roleService.getAllPermissionsAccess();
        for(RoleToPermissionType eachPermissionAccess : allPermissionAccess){
            Access access = new Access();
            access.setCanView(eachPermissionAccess.getCanView());
            access.setCanEdit(eachPermissionAccess.getCanEdit());
        redisDataService.savePermissionAccessToRedis(eachPermissionAccess.getRoleId(),eachPermissionAccess.getPermissionId(),access);
        }

        List<Permission> permissionType = permissionService.getAllPermissions();
        for(Permission permission : permissionType){
            redisDataService.savePermissionTypeToRedis(permission.getPermissionType(), String.valueOf(permission.getPermissionId()));
        }
        RbacsApplication.printString("-------------Redis server is Running successfully---------");
        userService.printRoleDetails();
    }



}