package perfios.rbacs.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import perfios.rbacs.Repository.FileRepository.FileServices;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
public class FileController {

    @Autowired
    FileServices fileServices;



    @GetMapping("/file/upload")
    public ModelAndView fileUpload(){
        return new ModelAndView("fileupload");
    }

    @PostMapping("/uploadnewuser")
    public List<String> uploadNewUser(@RequestParam("file")MultipartFile file){
        if(file == null || file.isEmpty()) return new ArrayList<>();
        return fileServices.addUserFromCSVFile(file);
    }

    @GetMapping("/userdetails/excel")
    public String getUserDetailsExcel(){
        return fileServices.getUserDetailsExcelFile();
        }

    @PostMapping("/file/upload-xlxs")
    public List<String> uploadNewUserXlxsFile(@RequestParam("file") MultipartFile file){
        if(file == null || file.isEmpty()) return new ArrayList<>();
        return fileServices.addUserFromXlxsFile(file);
    }

    @GetMapping("/file/upload/xlxs")
    public ModelAndView uploadXlxsFile(){
        return new ModelAndView ("fileUploadXlxs");
    }
}
