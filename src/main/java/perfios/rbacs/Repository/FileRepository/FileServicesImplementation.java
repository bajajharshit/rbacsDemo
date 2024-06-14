package perfios.rbacs.Repository.FileRepository;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import perfios.rbacs.Model.Users.User;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.UserRepository.UserService;

import java.io.*;
import java.util.*;

@Service
public class FileServicesImplementation implements FileServices{

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    /*
    {
"userFirstName": "userSeven",
"userLastName": "userSevenLastName",
"userPassword": "userSevenPassword",
"userPhoneNumber": "9889487778",
"userStatus": "Active",
"userEmail": "userseven@example.com",
"userRoleId": 5,
"enabled": true,
"isSuperAdmin": false,
"shouldLoanAutoApply": 0
}

     */


    @Autowired
    UserService userService;



    @Override
    public List<String> addUserFromCSVFile(MultipartFile file) {

        List<String> headers = new ArrayList<>();
        headers.add("userFirstName");
        headers.add("userLastName");
        headers.add("userPassword");
        headers.add("userPhoneNumber");
        headers.add("userStatus");
        headers.add("userEmail");
        headers.add("userRoleId");
        headers.add("enabled");
        headers.add("isSuperAdmin");
        headers.add("shouldLoanAutoApply");

        // Convert the list to a String array
        String[] headerArray = headers.toArray(new String[0]);

        try{
            Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            CSVParser parser =  CSVFormat.Builder.create(CSVFormat.EXCEL)
                    .setHeader(headerArray)
                    .build().parse(reader);

            int rowsInserted = 0;
            Iterator<CSVRecord> record = parser.iterator();
//            RbacsApplication.printString(record.next().toMap().toString());
            List<String> verificationList = new ArrayList<>();
            while(record.hasNext()){
//                RbacsApplication.printString("first line = " + record.hasNext());
                User user = new User();
                try {
                    user.setFeildsFromMapForCsvFile(record.next().toMap());
                }catch(java.lang.Exception e){
                    System.err.println(e.getMessage());
                }
                RbacsApplication.printString(user.toString());
                verificationList.add(user.getUserEmail() + " -> " + userService.addNewUser(user));
//                RbacsApplication.printString("------------------------------------------");
//                RbacsApplication.printString(verificationList.toString());
//                RbacsApplication.printString(String.valueOf(record.hasNext()));
            }
            return verificationList;
        }catch (IOException e){
            System.err.println(e.getMessage());
        }
       return null;
    }


    @Override
    public List<String> addUserFromXlxsFile(MultipartFile file) {

        Workbook workbook = new XSSFWorkbook(file);
    }

    @Override
    public String getUserDetailsExcelFile() {

        List<User> allUsers= userService.getAllUsers();

        RbacsApplication.printString("fetched users = " + allUsers);
        Workbook workbook  = new XSSFWorkbook();
        Sheet sheet  = workbook.createSheet("All Users");


        Row header = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());

        //creating headers
        List<String> headerList = new ArrayList<>();
        headerList.add("User Id");
        headerList.add("User First Name");
        headerList.add("User Last Name");
        headerList.add("User Password");
        headerList.add("User Phone Number");
        headerList.add("Alternate Username");
        headerList.add("User Status");
        headerList.add("User Email");
        headerList.add("User Role Name");
        headerList.add("User Role Id");
        headerList.add("Enabled");
        headerList.add("Is Super Admin");
        headerList.add("Should Loan Auto Apply");



        int columnCount = 0;
        for(String headerCellValue : headerList){
            sheet.setColumnWidth(columnCount, 6000);
            Cell headerCell = header.createCell(columnCount++);
            headerCell.setCellStyle(headerStyle);
            headerCell.setCellValue(headerCellValue);
        }


        int rowCount = 1;

        for(User user : allUsers){
            Row userRow = sheet.createRow(rowCount++);
            Map<Integer, Object> userMap = fillUserMap(user);
            columnCount = 0;
            while(userMap.containsKey(columnCount)){
                Cell userDataCell = userRow.createCell(columnCount);
                userDataCell.setCellValue(String.valueOf(userMap.get(columnCount)));
                columnCount++;
            }

        }

        File currDir = new File("/home/harshit.baja/Desktop/sampleFile/");
        String path = currDir.getAbsolutePath();
        String fileLocation = path + "userDetails.xlsx";

        try {
            FileOutputStream outputStream = new FileOutputStream(fileLocation);
            workbook.write(outputStream);
            workbook.close();
        }catch (IOException e){
            return  "failed " + e.getMessage();
        }



        return "saved at " + fileLocation;
    }


    public static Map<Integer,Object> fillUserMap(User user){
        int cc = 0;
        Map<Integer, Object> userMap = new HashMap<>();
        userMap.put(cc++, user.getUserId());
        userMap.put(cc++, user.getUserFirstName());
        userMap.put(cc++, user.getUserLastName());
        userMap.put(cc++, user.getUserPassword());
        userMap.put(cc++, user.getUserPhoneNumber());
        userMap.put(cc++, user.getAlternateUsername());
        userMap.put(cc++, user.getUserStatus());
        userMap.put(cc++, user.getUserEmail());
        userMap.put(cc++, user.getUserRoleName());
        userMap.put(cc++, user.getUserRoleId());
        userMap.put(cc++, user.getEnabled());
        userMap.put(cc++, user.getIsSuperAdmin());
        userMap.put(cc++, user.getShouldLoanAutoApply());

        return userMap;
    }


}
