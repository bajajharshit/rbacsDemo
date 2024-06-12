package perfios.rbacs.Repository.RoleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.SQLWarningException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import perfios.rbacs.Model.RoleToPermissionType.RoleToPermissionType;
import perfios.rbacs.Model.Role_to_permission.RoleToPermission;
import perfios.rbacs.Model.Roles.Role;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.Redis.RedisDataService;
import perfios.rbacs.Repository.UserRepository.UserService;

import java.sql.*;
import java.util.*;
//import perfios.rbacs.Config.JDBCConfig;

import javax.naming.CannotProceedException;
import javax.sql.DataSource;
import javax.sql.RowSetEvent;

@Service
public class RoleServiceImplementation implements RoleService{

    private final DataSource dataSource;

    @Autowired
    public RoleServiceImplementation(DataSource dataSource){
        this.dataSource=dataSource;
    }

    @Autowired
    RedisDataService redisDataService;

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;



    //declare queries here
    private static String getStatement = "select role_id,role_name,status from role_details;";
    private static String saveStatement ="insert into role_details (role_id, role_name, status) values (?,?,?);";
    private static String deleteStatememnt = "delete from role_details where role_id = ?;";
    private static String getRolePermissionQuery = "select permission_id,role_id from role_to_permission;";
    private static String saveRolePermissionQuery = "insert into role_to_permission(role_id,permission_id) values(?,?);";
    private static String deleteRolePermissionQuery= "delete from role_to_permission where role_id = ? and permission_id = ?;";
    private static String getPermissionIdPermissionNameQuery = "select permission_id,permission_name from permission;";




    private static String getAllPermissionAccessList = "select role_id, permission_id, can_view, can_edit from role_to_permission_type;";
    private static String updateViewAccessForRoleAndPermissionQuery = "update role_to_permission_type set can_view = ? where role_id = ? and permission_id = ?";
    private static String updateEditAccessForRoleAndPermissionQuery = "update role_to_permission_type set can_edit = ? where role_id = ? and permission_id = ?";
    private static final String getAllPermissionTypeAccessForParticularRoleQuery = "SELECT pt.permission_type, pt.permission_id, rtpt.can_view, rtpt.can_edit FROM permission_type pt INNER JOIN role_to_permission_type rtpt ON pt.permission_id = rtpt.permission_id WHERE role_id = ?;";
    private static final String addNewPermissionToExistingRoleQuery = "insert into role_to_permission_type(role_id,permission_id,can_view,can_edit) values(:role_id,:permission_id,:can_view,:can_edit);";
    private static final String deletePermissionForExistingRoleQuery = "delete from role_to_permission_type where role_id = :role_id and permission_id = :permission_id";



//-------------------------------------------new methords according to new design------------------------------

    @Override
    public List<RoleToPermissionType> getAllPermissionsAccess() {
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(getAllPermissionAccessList);
            ResultSet permissionList = statement.executeQuery();
            if(!permissionList.next()) return null;
            List<RoleToPermissionType> permissionTypeList  = new ArrayList<>();
            do{
                RoleToPermissionType permissionAccess = new RoleToPermissionType();
                permissionAccess.setPermissionId(String.valueOf(permissionList.getInt("permission_id")));
                permissionAccess.setRoleId(String.valueOf(permissionList.getInt("role_id")));
                permissionAccess.setCanEdit(permissionList.getBoolean("can_edit"));
                permissionAccess.setCanView(permissionList.getBoolean("can_view"));
                permissionTypeList.add(permissionAccess);
            }while (permissionList.next());
            return permissionTypeList;
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return null;
    }





    //methord to update view permission in mysql(permanent) and redis(in memory) together for a particualr role
    @Override
    public Boolean UpdateViewAccessForRoleAndPermission(int role_id, int permission_id, Boolean allow){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(updateViewAccessForRoleAndPermissionQuery);
            statement.setBoolean(1,allow);
            statement.setInt(2,role_id);
            statement.setInt(3,permission_id);
            int rowsAffected = statement.executeUpdate();
            if(rowsAffected == 0) return false;
            else return true;
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return null;
    }


    @Override
    public Boolean UpdateEditAccessForRoleAndPermission(int role_id, int permission_id, Boolean allow){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(updateEditAccessForRoleAndPermissionQuery);
            statement.setBoolean(1,allow);
            statement.setInt(2,role_id);
            statement.setInt(3,permission_id);
            int rowsAffected = statement.executeUpdate();
            if(rowsAffected == 0) return false;
            else return true;
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return null;
    }

    @Override
    public List<RoleToPermissionType> getAllPermissionTypeAccessForParticularRole(String roleId){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(getAllPermissionTypeAccessForParticularRoleQuery);
            statement.setInt(1,Integer.valueOf(roleId));
            ResultSet permissionAccess = statement.executeQuery();
            if(!permissionAccess.next()) return null;
            List<RoleToPermissionType> permissionTypeAccessList = new ArrayList<>();
            do{
                RoleToPermissionType roleToPermissionType = new RoleToPermissionType();
                roleToPermissionType.setPermissionName(permissionAccess.getString("permission_type"));
                roleToPermissionType.setPermissionId(String.valueOf(permissionAccess.getInt("permission_id")));
                roleToPermissionType.setCanView(permissionAccess.getBoolean("can_view"));
                roleToPermissionType.setCanEdit(permissionAccess.getBoolean("can_edit"));
                roleToPermissionType.setRoleId(roleId);
                permissionTypeAccessList.add(roleToPermissionType);
            }while (permissionAccess.next());
            return permissionTypeAccessList;
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return null;
    }

    @Override
    public Boolean addNewPermissionToExistingRole(RoleToPermissionType newPermission) {
            String permissionId = redisDataService.getPermissionId(newPermission.getPermissionName());
            String roleId = newPermission.getRoleId();
            Boolean canView = newPermission.getCanView();
            Boolean canEdit = newPermission.getCanEdit();
            Map<String,Object> params = new HashMap<>();
            params.put("role_id",roleId);
            params.put("permission_id",permissionId);
            params.put("can_view",canView);
            params.put("can_edit",canEdit);
         RbacsApplication.printString(params.toString());
            int rowsAffected = 0;
            try {
                rowsAffected = namedParameterJdbcTemplate.update(addNewPermissionToExistingRoleQuery, params);
                throw new SQLException();
            }catch (Exception e){
                System.err.println(e.getMessage());
            }
        if(rowsAffected > 0) return true;
            else return false;
    }

    @Override
    public Boolean unassignPermissionToExistingRole(int roleId, String permissionType) {
        String permissionId = redisDataService.getPermissionId(permissionType);
        RbacsApplication.printString("roleid = " + roleId + " permisison type = " + permissionType + "permisison id = " + permissionId);

        Map<String ,Object> params = new HashMap<>();
        params.put("role_id",roleId);
        params.put("permission_id",Integer.valueOf(permissionId));
        RbacsApplication.printString(params.toString());
        int rowsAffected = 0;
        try {
             rowsAffected = namedParameterJdbcTemplate.update(deletePermissionForExistingRoleQuery, params);
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
        if(rowsAffected > 0) return true;
        else return false;
    }


















    //---------------------------------------old methords BELOW-------------------------------------




    @Override
    public List<Role> getAllRoles() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(getStatement);
             ResultSet resultSet = statement.executeQuery()) {
            List<Role> roles = new ArrayList<>();
            while (resultSet.next()) {
                Role role = new Role();
                role.setRoleId(resultSet.getInt("role_id"));
                role.setRoleName(resultSet.getString("role_name"));
                role.setStatus(resultSet.getString("status"));
                roles.add(role);
            }
            return roles;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }



    @Override
    public String saveRole(Role role) {
        try{
            Connection connection = dataSource.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(saveStatement)) {
                statement.setInt(1, role.getRoleId());
                statement.setString(2, role.getRoleName());
                statement.setString(3, role.getStatus());

                //use name of position parameters


                int rowsAffected = statement.executeUpdate();
//                System.out.println("rows = "+ rowsAffected);
                if (rowsAffected > 0) {
                    return "Role saved successfully.";
                } else {
                    return "Failed to save role.";
                }
            }

        }catch (SQLException e){
//            System.out.println("insde this block");
            System.out.println(e.getMessage());
        }
        return null;
    }



    @Override
    public String deleteRoleWithId(int id){
        try{
            Connection connection = dataSource.getConnection();
            try {
                PreparedStatement statement = connection.prepareStatement(deleteStatememnt);
                statement.setInt(1, id);
                int rows = statement.executeUpdate();
                if (rows > 0) return "User with " + id + " deleted successfully";
                else return "User not found";
        }
            catch (SQLException e){
                System.err.println(e.getMessage());
                return "Query is incorrect";
            }
        }
        catch (SQLException e){
            System.err.println(e.getMessage());
            return "Connection did not established with DB";
        }

    }

    @Override
    public List<RoleToPermission> getRolePermissionList(){
        try{
            Connection connection = dataSource.getConnection();
            try {
                PreparedStatement statement = connection.prepareStatement(getRolePermissionQuery);
                ResultSet resultSet = statement.executeQuery();
                List<RoleToPermission> roleToPermissionList = new ArrayList<>();
                HashMap<Integer,String> roleIdRoleName = new HashMap<Integer, String>();
                HashMap<Integer,String> permissionIdPermissionName = new HashMap<Integer, String>();
                PreparedStatement roleIdRoleNameData = connection.prepareStatement(getStatement);
                ResultSet roleDetails = roleIdRoleNameData.executeQuery();
                while(roleDetails.next()){
                    roleIdRoleName.put(roleDetails.getInt("role_id"),roleDetails.getString("role_name"));
                }
                PreparedStatement permissionIdpermissionNameData = connection.prepareStatement(getPermissionIdPermissionNameQuery);
                ResultSet permissionDetails = permissionIdpermissionNameData.executeQuery();
                while(permissionDetails.next())
                {
                    permissionIdPermissionName.put(permissionDetails.getInt("permission_id"),permissionDetails.getString("permission_name"));
                }
                while(resultSet.next()){
                    RoleToPermission roleToPermission = new RoleToPermission();
                    roleToPermission.setPermissionId(resultSet.getInt("permission_id"));
                    roleToPermission.setRoleId(resultSet.getInt("role_id"));
                    roleToPermission.setPermissionName(permissionIdPermissionName.get(resultSet.getInt("permission_id")));
                    roleToPermission.setRoleName(roleIdRoleName.get(resultSet.getInt("role_id")));
                    roleToPermissionList.add(roleToPermission);
                }
                return roleToPermissionList;
            }catch (SQLException e){
                System.err.println(e.getMessage());
            }
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }

        return null;
    }

    @Override
    public String saveRolePermission(int role_id, int permission_id){
        try {
            Connection connection = dataSource.getConnection();
            try{
                PreparedStatement statement = connection.prepareStatement(saveRolePermissionQuery);
                statement.setInt(1,role_id);
                statement.setInt(2,permission_id);
                int rowsAffected = statement.executeUpdate();
                if(rowsAffected>0) return "saved successfully";
                else return "did not saved";
            }catch (SQLException e){
                System.err.println(e.getMessage());
            }
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return "didnot saved";
    }

    @Override
    public String deleteRolePermission(int role_id, int permission_id){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(deleteRolePermissionQuery);
            statement.setInt(1,role_id);
            statement.setInt(2,permission_id);
            int rowsAffected = statement.executeUpdate();
            if(rowsAffected > 0) return "Given permission has now removed from given role";
            else return "role with given permission not found";
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return null;
    }







}
