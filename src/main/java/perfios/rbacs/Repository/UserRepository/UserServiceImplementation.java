package perfios.rbacs.Repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import perfios.rbacs.Model.LoginPost.LoginPostOb;
import perfios.rbacs.Model.LoginResponse.LoginResponse;
import perfios.rbacs.Model.LoginResponse.LoginResponse2;
import perfios.rbacs.Model.Users.User;
import perfios.rbacs.Model.Users.UserDashboard;
import perfios.rbacs.Model.Users.UserSearch;
import perfios.rbacs.RbacsApplication;

import javax.print.DocFlavor;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

@Service
public class UserServiceImplementation implements UserService{


    private static final String userDashboardQuery = "SELECT ud.user_id, ud.user_email, utr.role_id from user_details ud, user_to_role utr WHERE ud.user_id = utr.user_id;";
    private static final String addUserDetailQuery = "INSERT INTO user_details (status, user_email, user_first_name, user_last_name, user_password, user_phone_number, enabled, is_super_admin, should_loan_auto_apply,alternate_username) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? , ?);";
    private static final String checkExistingUserQuery = "select user_id,enabled,user_password from user_details where user_email = ?;";
    private static final String addUserRoleQuery = "insert into user_to_role(user_id,role_id) values(?,?);";
    private static final String getAllUsersQuery = "select ud.user_id, ud.user_first_name, ud.user_last_name, ud.user_email, ud.user_password, ud.status, ud.user_phone_number, ud.enabled, ud.is_super_admin, ud.should_loan_auto_apply, ud.alternate_username, utr.role_id from user_details ud, user_to_role utr where ud.user_id = utr.user_id order by utr.role_id; ";
    private static final String deleteUserInUserDetailsQuery = "delete from user_details where user_id = ?; ";
    private static final String deleteUserInUserToRoleQuery =  "delete from user_to_role where user_id = ?;";
    private static final String deleteUserRoleQuery = "delete from user_to_role where user_id = ? and role_id = ?";
    private static final String addNewRoleToExistingUserQuery = "insert into user_to_role(user_id,role_id) values (?,?)";
    private static final String updateInUserDetailsQuery = "update user_details set status = ?, user_first_name = ?, user_last_name = ?, user_password = ?, user_phone_number = ? ,enabled = ?, is_super_admin = ?, should_loan_auto_apply = ? where user_id = ?;";
    private static final String checkNumberOfRolesAssociatedWithUserQuery = "select count(*) from user_to_role where user_id = ?;";
    private static final String fetchRoleIdAndRoleNameQuery = "select role_id,role_name from role_details";
    private static final String getExistingUserDetailsQuery = "SELECT ud.user_id, ud.user_first_name, ud.user_last_name, ud.user_email, ud.user_password, ud.status, ud.user_phone_number, ud.enabled, ud.is_super_admin, ud.should_loan_auto_apply, ud.alternate_username, utr.role_id FROM user_details ud, user_to_role utr WHERE ud.user_id = utr.user_id AND ud.user_id = ?;";
    private static final String fetchRoleNameFromRoleIdQuery = "select role_name from role_details where role_id = ?;";
    private static final String getAllRolesIdAssociatedWithUserQuery = "select role_id from user_to_role where user_id = ?;";
    private static final String updateRoleOfUserQuery = "update user_to_role set role_id = ? where user_id = ?;";
    private static final String getAllPermissionIdsForUserByIdQuery = "select rtp.permission_id, role_id from role_to_permission rtp where role_id = (select role_id from user_to_role where user_id = ?);";
    private static final String validateUserAgaistUserIdQuery = "select user_email,user_password,enabled from user_details where user_id = ?";
    private static final String fetchAdminPermissionsQuery = "select permission_id, can_view, can_edit from role_to_permission_type where role_id = 1";


    //datasource object for connection pooling with JDBC
    private final DataSource dataSource;

    //this constructor injects DatasSource object into above created datasource with help of springBoot IOC container.
    public UserServiceImplementation(DataSource dataSource){
        this.dataSource = dataSource;
    }

    @Autowired
    JdbcTemplate jdbcTemplate;


    //may be required later.
    private int sessionUserId =0;
    private int sessionRoleId =0;
    private Set<Integer> sessionPermissions = new HashSet<>();
    private LoginResponse loginResponse;
    int verifiedUserId = -1;


    private HashMap<Integer,String > roleDetails = new HashMap<>();
    private HashMap<String ,String > getRoleIdFromRole = new HashMap<>();





    @Override
    public int getVerifiedUserId(){
        int id = this.verifiedUserId;
        resetVerifiedUserId();
        return id;
    }

    @Override
    public void resetVerifiedUserId(){
        this.verifiedUserId = -1;
    }


    @Override
    public void printRoleDetails(){
        RbacsApplication.printString("ROLE DETAILS = ");
        RbacsApplication.printString(roleDetails.toString());
        RbacsApplication.printString("ROle IDs");
        RbacsApplication.printString(getRoleIdFromRole.toString());
    }



    @Override
    public String getRoleName(int roleId){
        RbacsApplication.printString("here and roleId = " + roleId);
        String role = roleDetails.get(roleId);
        String roleName = "ROLE_" + role.toUpperCase();
        return roleName;
    }


    @Override
    public void fillRoleDetails(){
        HashMap<String,String> getRoleIdFromGrantedAuthority = new HashMap<>();
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(fetchRoleIdAndRoleNameQuery);
            ResultSet roles = statement.executeQuery();
            while(roles.next()){
                roleDetails.put(roles.getInt("role_id"),roles.getString("role_name"));
                getRoleIdFromGrantedAuthority.put(getRoleName(roles.getInt("role_id")),String.valueOf(roles.getInt("role_id")));
            }
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        this.getRoleIdFromRole = getRoleIdFromGrantedAuthority;
    }


    @Override
    public String getRoleIdFromRole(String roleName) {
        String roleId = getRoleIdFromRole.get(roleName);
        return roleId;
    }


    @Override
    public List<UserDashboard> dashboardFindUserByDifferentFeilds(UserSearch userSearch) {
        RbacsApplication.printString(userSearch.toString());
        Boolean fireQuery = false;
        StringBuilder userSearchQuery = new StringBuilder("SELECT ud.user_id, ud.user_email," +
                " ud.enabled," +
                " utr.role_id" +
                " FROM user_details ud" +
                " JOIN user_to_role utr ON ud.user_id = utr.user_id WHERE ");
        if (userSearch.getUserAlternateName() != null && userSearch.getUserAlternateName().isEmpty() == false) {
            if (fireQuery == false) {
                userSearchQuery.append("ud.alternate_username LIKE '" + userSearch.getUserAlternateName() + "%' ");
                fireQuery = true;

            } else {
                userSearchQuery.append("AND ud.alternate_username LIKE '" + userSearch.getUserAlternateName() + "%' ");
            }
        }
        if (userSearch.getUserFirstName() != null && userSearch.getUserFirstName().isEmpty() == false) {
            if (fireQuery == false) {
                userSearchQuery.append("ud.user_first_name LIKE '%" + userSearch.getUserFirstName() + "%' ");
                fireQuery = true;
            } else {
                userSearchQuery.append("AND ud.user_first_name LIKE '%" + userSearch.getUserFirstName() + "%' ");
            }
        }
        if (userSearch.getUserLastName() != null && userSearch.getUserLastName().isEmpty() == false) {
            if (fireQuery == false) {
                userSearchQuery.append("ud.user_last_name LIKE '" + userSearch.getUserLastName() + "%' ");
                fireQuery = true;
            } else {
                userSearchQuery.append("AND ud.user_last_name LIKE '" + userSearch.getUserLastName() + "%' ");
            }
        }
        if (userSearch.getUserRoleName() != null && userSearch.getUserRoleName().isEmpty() == false) {
            String roleIdInString = getRoleIdFromRole(userSearch.getUserRoleName());
            if (roleIdInString == null || roleIdInString.isEmpty()) {
                for (String key : getRoleIdFromRole.keySet()) {
                    RbacsApplication.printString("key = " + key);
                    if (key.contains(userSearch.getUserRoleName().toUpperCase())) {
                        roleIdInString = getRoleIdFromRole.get(key);
                        RbacsApplication.printString("role matched");
                        break;
                    }
                }
                if (roleIdInString != null && roleIdInString.isEmpty() == false) {
                    if (fireQuery == false) {
                        userSearchQuery.append(" utr.role_id = " + roleIdInString);
                        fireQuery = true;
                    } else userSearchQuery.append(" AND utr.role_id = " + roleIdInString);
                }
            }
        }

        RbacsApplication.printString(fireQuery + userSearchQuery.toString());
        if (fireQuery == false) return null;
        List<UserDashboard> userDashboardList  = new ArrayList<>();
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(userSearchQuery.toString());
            ResultSet resultSet = statement.executeQuery();
            if(!resultSet.next()) return null;
            do {
                UserDashboard userDashboard = new UserDashboard();
                userDashboard.setUserEmail(resultSet.getString("user_email"));
                userDashboard.setUserId(resultSet.getInt("user_id"));
                userDashboard.setRoleName(roleDetails.get(resultSet.getInt("role_id")));
                userDashboardList.add(userDashboard);
            }while(resultSet.next());
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return userDashboardList;
    }

    @Override
    public List<User> findUserByDifferentFeilds(UserSearch userSearch) {
        RbacsApplication.printString(userSearch.toString());
        Boolean fireQuery = false;
        StringBuilder userSearchQuery = new StringBuilder("SELECT ud.user_id, ud.user_first_name, ud.user_last_name, ud.user_email," +
                " ud.user_password, ud.status, ud.user_phone_number, ud.enabled," +
                " ud.is_super_admin, ud.should_loan_auto_apply, ud.alternate_username," +
                " utr.role_id" +
                " FROM user_details ud" +
                " JOIN user_to_role utr ON ud.user_id = utr.user_id WHERE ");
        if (userSearch.getUserAlternateName() != null && userSearch.getUserAlternateName().isEmpty() == false) {
            if (fireQuery == false) {
                userSearchQuery.append("ud.alternate_username LIKE '" + userSearch.getUserAlternateName() + "%' ");
                fireQuery = true;

            } else {
                userSearchQuery.append("AND ud.alternate_username LIKE '" + userSearch.getUserAlternateName() + "%' ");
            }
        }
        if (userSearch.getUserFirstName() != null && userSearch.getUserFirstName().isEmpty() == false) {
            if (fireQuery == false) {
                userSearchQuery.append("ud.user_first_name LIKE '%" + userSearch.getUserFirstName() + "%' ");
                fireQuery = true;
            } else {
                userSearchQuery.append("AND ud.user_first_name LIKE '%" + userSearch.getUserFirstName() + "%' ");
            }
        }
        if (userSearch.getUserLastName() != null && userSearch.getUserLastName().isEmpty() == false) {
            if (fireQuery == false) {
                userSearchQuery.append("ud.user_last_name LIKE '" + userSearch.getUserLastName() + "%' ");
                fireQuery = true;
            } else {
                userSearchQuery.append("AND ud.user_last_name LIKE '" + userSearch.getUserLastName() + "%' ");
            }
        }
        if (userSearch.getUserRoleName() != null && userSearch.getUserRoleName().isEmpty() == false) {
            String roleIdInString = getRoleIdFromRole(userSearch.getUserRoleName());
            if (roleIdInString == null || roleIdInString.isEmpty()) {
                for (String key : getRoleIdFromRole.keySet()) {
                    RbacsApplication.printString("key = " + key);
                    if (key.contains(userSearch.getUserRoleName().toUpperCase())) {
                        roleIdInString = getRoleIdFromRole.get(key);
                        RbacsApplication.printString("role matched");
                        break;
                    }
                }
                if (roleIdInString != null && roleIdInString.isEmpty() == false) {
                    if (fireQuery == false) {
                        userSearchQuery.append(" utr.role_id = " + roleIdInString);
                        fireQuery = true;
                    } else userSearchQuery.append(" AND utr.role_id = " + roleIdInString);
                }
            }
        }

        RbacsApplication.printString(fireQuery + userSearchQuery.toString());
        if (fireQuery == false) return null;
        List<User> userList = new ArrayList<>();
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(userSearchQuery.toString());
            ResultSet resultSet = statement.executeQuery();
            if(!resultSet.next()) return null;
            do{
                User newUser = new User();
                newUser.setUserId(resultSet.getInt("user_id"));
                newUser.setUserFirstName(resultSet.getString("user_first_name"));
                newUser.setUserLastName(resultSet.getString("user_last_name"));
                newUser.setUserEmail(resultSet.getString("user_email"));
                newUser.setUserPassword(resultSet.getString("user_password"));
                newUser.setUserStatus(resultSet.getString("status"));
                newUser.setUserPhoneNumber(resultSet.getString("user_phone_number"));
                newUser.setEnabled(resultSet.getBoolean("enabled"));
                newUser.setIsSuperAdmin(resultSet.getBoolean("is_super_admin"));
                newUser.setShouldLoanAutoApply(resultSet.getBoolean("should_loan_auto_apply"));
                newUser.setAlternateUsername(resultSet.getString("alternate_username"));
                newUser.setUserRoleId(resultSet.getInt("role_id"));
                newUser.setUserRoleName(getRoleName(newUser.getUserRoleId()));
                userList.add(newUser);
            }while(resultSet.next());
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return userList;
    }




    //this below methord is for jwt, use to validate user agaist database using its id.
    @Override
    public LoginPostOb fetchUserDetailFromUserId(int userId){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(validateUserAgaistUserIdQuery);
            statement.setInt(1,userId);
            ResultSet validator = statement.executeQuery();
            if(!validator.next()) return null;
            if(validator.getBoolean("enabled") != true) return null;
            LoginPostOb loginPostOb = new LoginPostOb();
            loginPostOb.setUserEmail(validator.getString("user_email"));
            loginPostOb.setUserPassword(validator.getString("user_password"));
            RbacsApplication.printString("loginpost from jwt is " + loginPostOb.toString());
            return loginPostOb;
        }catch (SQLException e){
            System.err.println(e.getMessage());
            return null;
        }
    }


    @Override
    public LoginResponse2 fetchUserDetailsFromUserId2(int userId , String userEmail){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(validateUserAgaistUserIdQuery);
            preparedStatement.setInt(1,userId);
            ResultSet validatedUser = preparedStatement.executeQuery();
            if(!validatedUser.next()) return null;
            if(validatedUser.getString("user_email").equals(userEmail) == false) return null;
            int roleId = -1;
            try{
                PreparedStatement statement = connection.prepareStatement(getAllRolesIdAssociatedWithUserQuery);
                statement.setInt(1,userId);
                ResultSet roleIdRS = statement.executeQuery();
                if(!roleIdRS.next()) return null;
                roleId = roleIdRS.getInt("role_id");
            }catch (SQLException e){
                System.err.println(e.getMessage());
            }
            LoginResponse2 loginResponse = LoginResponse2.builder().build();
            loginResponse.setUserRoleId(roleId);
            loginResponse.setRoleName(roleDetails.get(roleId));
            loginResponse.setUserPassword(validatedUser.getString("user_password"));
            loginResponse.setUserEmail(userEmail);
            loginResponse.setUserId(userId);
            return loginResponse;
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return null;
    }



    @Override
    public LoginResponse2 loadUserByEmailId2(String emailId){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(checkExistingUserQuery);
            statement.setString(1,emailId);
            ResultSet loadedUser = statement.executeQuery();
            if(!loadedUser.next()) return null;
            RbacsApplication.printString("here this point 1");
            if(loadedUser.getBoolean("enabled") == false) return null;
            int userId = loadedUser.getInt("user_id");
            RbacsApplication.printString("user id = " + userId);
            int userRoleId = -1;
            try{
                PreparedStatement statement1 = connection.prepareStatement(getAllRolesIdAssociatedWithUserQuery);
                statement1.setInt(1,userId);
                ResultSet roleIdSet = statement1.executeQuery();
                if(!roleIdSet.next()) return null;
                userRoleId = roleIdSet.getInt("role_id");
            }catch (SQLException e){
                System.err.println(e.getMessage());
            }

            LoginResponse2 loginResponse = LoginResponse2.builder().build();
            loginResponse.setUserPassword(loadedUser.getString("user_password"));
            loginResponse.setRoleName(roleDetails.get(userRoleId));
            loginResponse.setUserId(userId);
            loginResponse.setUserEmail(emailId);
            loginResponse.setUserRoleId(userRoleId);
            if(verifiedUserId != -1) RbacsApplication.printString("this userid technique fail");
            verifiedUserId = userId;
            return loginResponse;


        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return null;
    }






    @Override
    public LoginResponse loadUserByEmailId(String emailId){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(checkExistingUserQuery);
            statement.setString(1,emailId);
            ResultSet resultSet = statement.executeQuery();
            if(!resultSet.next()) {
                RbacsApplication.printString("result set found empty");
                return null;
            }
            if(resultSet.getBoolean("enabled") == false) {
                RbacsApplication.printString("user found disabled");
                return null;
            }
            LoginResponse loginResponseObjet = LoginResponse.builder().build();
            loginResponseObjet.setUserEmailId(emailId);
            loginResponseObjet.setUserPassword(resultSet.getString("user_password"));
            loginResponseObjet.setUserId(resultSet.getInt("user_id"));
            statement = connection.prepareStatement(getAllPermissionIdsForUserByIdQuery);
            try {
                statement.setInt(1, loginResponseObjet.getUserId());
                ResultSet resultSet1 = statement.executeQuery();
                if(!resultSet1.next()) return null;
                loginResponseObjet.setUserRoleId(resultSet1.getInt("role_id"));
                HashSet<String> permissionIds = new HashSet<>();
                do{
                    permissionIds.add(String.valueOf(resultSet1.getInt("permission_id")));
                }while(resultSet1.next());
                loginResponseObjet.setUserPermissionId(permissionIds);
                RbacsApplication.printUserLogin(loginResponseObjet);
                sessionPermissions.clear();
                for(String temp : loginResponseObjet.getUserPermissionId()) sessionPermissions.add(Integer.valueOf(temp));
                RbacsApplication.printString("session user id = " + sessionUserId + " session role id = " + sessionRoleId + "session permissions = ");
                RbacsApplication.printSet(sessionPermissions);
                loginResponse = loginResponseObjet;
                connection.close();
                RbacsApplication.printString("this from userServiceimplentation not from session service");
                return loginResponseObjet;
            }catch (SQLException e){
                e.printStackTrace();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }


public LoginResponse getUserLogin(){
        return this.loginResponse;
}


    //function to display list of users with their roles, if one user have multiple role then it will return separate row for that.
    //one row with first role, another row with second role and so on.
    @Override
    public List<User> getAllUsers() {
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(getAllUsersQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<User> userList = new ArrayList<>();
            while(resultSet.next()){
                User newUser = new User();
                newUser.setUserId(resultSet.getInt("user_id"));
                newUser.setUserFirstName(resultSet.getString("user_first_name"));
                newUser.setUserLastName(resultSet.getString("user_last_name"));
                newUser.setUserEmail(resultSet.getString("user_email"));
                newUser.setUserPassword(resultSet.getString("user_password"));
                newUser.setUserStatus(resultSet.getString("status"));
                newUser.setUserPhoneNumber(resultSet.getString("user_phone_number"));
                newUser.setEnabled(resultSet.getBoolean("enabled"));
                newUser.setIsSuperAdmin(resultSet.getBoolean("is_super_admin"));
                newUser.setShouldLoanAutoApply(resultSet.getBoolean("should_loan_auto_apply"));
                newUser.setAlternateUsername(resultSet.getString("alternate_username"));
                PreparedStatement userRoleFetched = connection.prepareStatement(fetchRoleNameFromRoleIdQuery);
                userRoleFetched.setInt(1,resultSet.getInt("role_id"));
                ResultSet roleNameFetched = userRoleFetched.executeQuery();
                roleNameFetched.next();
                newUser.setUserRoleName(roleNameFetched.getString("role_name"));
                newUser.setUserRoleId(resultSet.getInt("role_id"));
                userList.add(newUser);
            }
            return userList;
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return null;
    }

    @Override
    public Boolean checkEmailAlreadyExist(String emailId){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(checkExistingUserQuery);
            statement.setString(1,emailId);
            ResultSet checkEmailId = statement.executeQuery();
            if(!checkEmailId.next()) return false;
            else return true;
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return false;
    }



    //function to add new user from now onwards
    @Override
    public String addNewUser(@Valid User user){
        if(user.getUserEmail() == null || user.getUserEmail().isEmpty() || checkEmailAlreadyExist(user.getUserEmail())) return "TRY WITH A DIFFERENT EMAIL";
        Connection connection = null;
        try{
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement(addUserDetailQuery,PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUserStatus()); // Status
            statement.setString(2, user.getUserEmail()); // userEmail
            statement.setString(3, user.getUserFirstName()); // userFirstName
            statement.setString(4, user.getUserLastName()); // userLastName
            statement.setString(5, user.getUserPassword()); // userPassword
            statement.setString(6, user.getUserPhoneNumber()); // userPhoneNumber
            statement.setBoolean(7,user.getEnabled()); //enable
            statement.setBoolean(8, user.getIsSuperAdmin()); //supradmin
            statement.setBoolean(9,user.getShouldLoanAutoApply()); //loanautoaply
            statement.setString(10,user.getAlternateUsername());
            int rowsAffected = statement.executeUpdate();
            if(rowsAffected == 0) return "Adding User Failed. :(";
            int autoGeneratedUserId = 0;
            try{
                ResultSet autoGeneratedKey = statement.getGeneratedKeys();
                if(!autoGeneratedKey.next()) return "Key not Generated, user adding failed";
                autoGeneratedUserId = autoGeneratedKey.getInt(1);
            }catch (SQLException e){
                System.err.println(e.getMessage());
            }
            statement = connection.prepareStatement(addUserRoleQuery);
            statement.setInt(1,autoGeneratedUserId);
            statement.setInt(2,user.getUserRoleId());
            int rowsAffected2 = 0;
            try {
                rowsAffected2 = statement.executeUpdate();
            }catch (SQLException e){
                connection.rollback();
                System.err.println(e.getMessage());
            }
            if(rowsAffected2 == 0) {
                return "invalid role passed, or user may not be present in database, adding failed";
            }
            if(rowsAffected + rowsAffected2 > 1) {
                connection.commit();
                return "New User Added Successfully :)";
            }
        }
        catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return "User adding failed, check entered details, entered role etc";
    }





    public String updateUser(User user, int id){
        Connection connection = null;
        try{
            connection  = dataSource.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement(updateInUserDetailsQuery);
            statement.setString(1, user.getUserStatus()); // Status
            statement.setString(2, user.getUserFirstName()); // userFirstName
            statement.setString(3, user.getUserLastName()); // userLastName
            statement.setString(4, user.getUserPassword()); // userPassword
            statement.setString(5, user.getUserPhoneNumber()); // userPhoneNumber
            statement.setBoolean(6,user.getEnabled()); //enable
            statement.setBoolean(7, user.getIsSuperAdmin()); //supradmin
            statement.setBoolean(8,user.getShouldLoanAutoApply());
            statement.setInt(9,id);
            int rowsAffected = statement.executeUpdate();
            if(rowsAffected == 0) return "updating user failed, invalid user_id in url";
            if(rowsAffected == 1) RbacsApplication.printString("user details table updated successfully");
            //till here, user details table has been updated successfully, now for user_to_role table:-
            //now we will update in user_to_role table.
            statement = connection.prepareStatement(updateRoleOfUserQuery);
            statement.setInt(1,user.getUserRoleId());
            statement.setInt(2,id);
            int rowsAffected2 = 0;
            try{
                rowsAffected2 = statement.executeUpdate();
            }catch (SQLException e){
                connection.rollback();
                System.err.println(e.getMessage());
            }
            if(rowsAffected2 == 0) return "Invalid role Entered\n";
            if(rowsAffected2 >0 && rowsAffected > 0) {
                connection.commit();
                return "user updated successfully";
            }
        }catch (SQLException e){   //main catch block
            System.err.println(e.getMessage());
        }
        return "User adding failed, check Query, Connection with DB and place holders.";
    }



    @Override
    public User getParticularUserById(int id){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statementForOldUser = connection.prepareStatement(getExistingUserDetailsQuery);
            statementForOldUser.setInt(1,id);
            User existingUser = new User();
            ResultSet existingUserFetched = statementForOldUser.executeQuery();
            if(!existingUserFetched.next()) {
                return null;
            }
            existingUser.setUserId(id);
            existingUser.setUserPhoneNumber(existingUserFetched.getString("user_phone_number"));
            existingUser.setUserEmail(existingUserFetched.getString("user_email"));
            existingUser.setUserStatus(existingUserFetched.getString("status"));
            existingUser.setUserFirstName(existingUserFetched.getString("user_first_name"));
            existingUser.setUserLastName(existingUserFetched.getString("user_last_name"));
            existingUser.setUserPassword(existingUserFetched.getString("user_password"));
            existingUser.setEnabled(existingUserFetched.getBoolean("enabled"));
            existingUser.setIsSuperAdmin(existingUserFetched.getBoolean("is_super_admin"));
            existingUser.setShouldLoanAutoApply(existingUserFetched.getBoolean("should_loan_auto_apply"));
            existingUser.setAlternateUsername(existingUserFetched.getString("alternate_username"));
            RbacsApplication.printString("user after fetching from user_details;");
            RbacsApplication.check2(existingUser);
             //got everything except role array.
            HashMap<Integer,String > roleDetails = new HashMap<>();
            PreparedStatement roleTableStatement = connection.prepareStatement(fetchRoleIdAndRoleNameQuery);
            ResultSet roleDetailsSet = roleTableStatement.executeQuery();
            while(roleDetailsSet.next()){
                roleDetails.put(roleDetailsSet.getInt("role_id"),roleDetailsSet.getString("role_name"));
            }
            PreparedStatement userRoleStatment = connection.prepareStatement(getAllRolesIdAssociatedWithUserQuery);
            userRoleStatment.setInt(1,id);
            ResultSet userRoleSet = userRoleStatment.executeQuery();
            List<String > userRoleNameList = new ArrayList<>();
            userRoleSet.next();
            existingUser.setUserRoleId(userRoleSet.getInt("role_id"));
            do{
                userRoleNameList.add(roleDetails.get(userRoleSet.getInt("role_id")));
            }while(userRoleSet.next());
            existingUser.setUserRoleNameList(userRoleNameList);
            existingUser.setUserRoleName(roleDetails.get(existingUser.getUserRoleId()));
            return existingUser;
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return null;
    }


    //this function is to delete user
    @Override
    public String deleteUser(int id) {
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(deleteUserInUserToRoleQuery);
            statement.setInt(1,id);
            int rowsAffected = statement.executeUpdate();
            RbacsApplication.printString("deleted in user to role " + rowsAffected);
            statement = connection.prepareStatement(deleteUserInUserDetailsQuery);
            statement.setInt(1,id);
            int rowsAffected2 = statement.executeUpdate();
            RbacsApplication.printString("for user details " + rowsAffected2);
            if(rowsAffected + rowsAffected2>1) return "User deleted successfully";
            return "User didnot deleted";
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return "user did not deleted";
    }


    //this functino is for return user list for the dashborad.
    //a hashmap is created here whenever the query is fired, it adds role_id and role_name in it
    // and then returns role_name from hashmap. so we dont want to read from table(roles) for every entry.
    @Override
    public List<UserDashboard> getAllUserDashboard(){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(userDashboardQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            preparedStatement = connection.prepareStatement(fetchRoleIdAndRoleNameQuery);
            ResultSet roleIdRoleName = preparedStatement.executeQuery();
            HashMap<Integer,String> roleDetails = new HashMap<>();
            while (roleIdRoleName.next()){
                roleDetails.put(roleIdRoleName.getInt("role_id"),roleIdRoleName.getString("role_name"));
            }
            List<UserDashboard> userDashboardList  = new ArrayList<>();
            while(resultSet.next()){
                UserDashboard userDashboard = new UserDashboard();
                userDashboard.setUserEmail(resultSet.getString("user_email"));
                userDashboard.setUserId(resultSet.getInt("user_id"));
                userDashboard.setRoleName(roleDetails.get(resultSet.getInt("role_id")));
                userDashboardList.add(userDashboard);
            }
            return userDashboardList;
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return null;
    }


}
