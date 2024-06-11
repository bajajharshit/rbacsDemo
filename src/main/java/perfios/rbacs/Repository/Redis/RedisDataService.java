package perfios.rbacs.Repository.Redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisDataService {
    @Autowired
    @Qualifier("redisTemplate1")
    private RedisTemplate<String, Access> redisTemplateForPermissionAccess;

    @Autowired
    @Qualifier("redisTemplate2")
    private RedisTemplate<String ,String> redisTemplateForPermissionType;

    public void savePermissionAccessToRedis(String roleId , String permissinId, Access access) {
        String key = roleId + "_" + permissinId;
        redisTemplateForPermissionAccess.opsForValue().set(key, access);
    }

    public Access getPermissionAccessFromRedis(String roleId , String permissinId) {
        String key = roleId + "_" + permissinId;
        return redisTemplateForPermissionAccess.opsForValue().get(key);
    }

    public void savePermissionTypeToRedis(String permissionType, String permissionId){
        redisTemplateForPermissionType.opsForValue().set(permissionType,permissionId);
    }

    public String getPermissionId(String permissionType){
        return redisTemplateForPermissionType.opsForValue().get(permissionType);
    }
}
