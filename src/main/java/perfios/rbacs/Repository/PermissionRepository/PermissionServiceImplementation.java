package perfios.rbacs.Repository.PermissionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import perfios.rbacs.Model.Permission.Permission;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class PermissionServiceImplementation implements PermissionService{

    private final DataSource dataSource;

    PermissionServiceImplementation(DataSource dataSource){
        this.dataSource = dataSource;
    }

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    //SQL QUERIES

    private static String getPermission= "select permission_id, permission_type from permission_type;";
    private static String addPermission= "insert into permission_type(permission_id,permission_type) values(?,?);";
    private static String deletePermission = "delete from permission_type where permission_id = ?;";


    @Override
    public List<Permission> getAllPermissions() {
        try {
            Connection connection = dataSource.getConnection();
            try {
                PreparedStatement statement = connection.prepareStatement(getPermission);
                ResultSet resultSet = statement.executeQuery();

                List<Permission> permissionList = new ArrayList<>();
                while (resultSet.next()) {
                    Permission permission = new Permission();
                    permission.setPermissionId(resultSet.getInt("permission_id"));
                    permission.setPermissionType(resultSet.getString("permission_type"));
                    permissionList.add(permission);
                }
                return permissionList;
            }
           catch (SQLException e){
                    System.err.println(e.getMessage());
                }

            }catch (SQLException e){
                System.err.println(e.getMessage());
            }
        return null;
        }

    @Override
    public String addPermission(Permission permission) {
        try{
            Connection connection = dataSource.getConnection();
            try {
                PreparedStatement statement = connection.prepareStatement(addPermission);
                statement.setInt(1,permission.getPermissionId());
                statement.setString(2,permission.getPermissionType());
                int rowsAffected = statement.executeUpdate();
                if(rowsAffected>0) return permission.getPermissionId() + " "+ permission.getPermissionType() + " saved successfully";
                else return "Permission didnot saved";
            }
            catch (SQLException e){
                System.err.println(e.getMessage());
                return "query is incorrect";
            }

        }catch (SQLException e){
            System.err.println(e.getMessage());
            return "connection failed to establish";
        }
    }

    @Override
    public String deletePermission(int id) {
        try{
            Connection connection = dataSource.getConnection();
            try{
                PreparedStatement statement = connection.prepareStatement(deletePermission);
                statement.setInt(1,id);
                int rowsAffected = statement.executeUpdate();
                if(rowsAffected>0) return "Permission deleted successfully";
                else return "Permission with id = " + id + " does not exist";
            }catch (SQLException e){
                System.err.println(e.getMessage());
            }
        }catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return "Permission not deleted";
    }
}
