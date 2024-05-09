package perfios.rbacs.Repository.RoleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import perfios.rbacs.Model.Role_to_permission.RoleToPermission;
import perfios.rbacs.Model.Roles.Role;
import perfios.rbacs.RbacsApplication;

import java.sql.*;
import java.util.*;
//import perfios.rbacs.Config.JDBCConfig;

import javax.sql.DataSource;

@Service
public class RoleServiceImplementation implements RoleService{

    private final DataSource dataSource;

    @Autowired
    public RoleServiceImplementation(DataSource dataSource){
        this.dataSource=dataSource;
    }



    //declare queries here
    private static String getStatement = "select role_id,role_name,status from role_details;";
    private static String saveStatement ="insert into role_details (role_id, role_name, status) values (?,?,?);";
    private static String deleteStatememnt = "delete from role_details where role_id = ?;";
    private static String getRolePermissionQuery = "select permission_id,role_id from role_to_permission;";
    private static String saveRolePermissionQuery = "insert into role_to_permission(role_id,permission_id) values(?,?);";
    private static String deleteRolePermissionQuery= "delete from role_to_permission where role_id = ? and permission_id = ?;";
    private static String getPermissionIdPermissionNameQuery = "select permission_id,permission_name from permission;";
    private static String getPermissionForParticularRoleQuery = "SELECT permission.permission_id, permission.permission_name FROM permission WHERE permission.permission_id IN (SELECT permission_id FROM role_to_permission WHERE role_id = ?);";
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
    public List<String> getPermissionForParticularRole(int role_id){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(getPermissionForParticularRoleQuery);
            statement.setInt(1,role_id);
            ResultSet permissionListResultSet = statement.executeQuery();
            List<String> permissionList = new ArrayList<>();
            while(permissionListResultSet.next()){
                permissionList.add(permissionListResultSet.getString("permission_name"));
            }
            return permissionList;
        }
        catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return null;
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
