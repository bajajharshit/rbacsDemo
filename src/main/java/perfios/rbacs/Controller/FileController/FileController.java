package perfios.rbacs.Controller.FileController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import perfios.rbacs.Model.Users.UserSearch;
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


    @GetMapping("/userdetails/excel")
    public String getUserDetailsExcel(){
        return fileServices.getUserDetailsExcelFile();
        }

    @PostMapping("/file/upload")
    public List<String> uploadNewUserXlxsFile(@RequestParam("file") MultipartFile file){
        if(file == null || file.isEmpty()) {
            return new ArrayList<>();
        }
        return fileServices.addUsersFromUploadedFile(file);
    }

    @GetMapping("/download-users-based-on-search")
    public String getUserDetailsExcelBasedOnFilters(@RequestBody UserSearch userSearch){
        return fileServices.getUserDetailsInXlsxFileBasedOnSearch(userSearch);
    }
}
