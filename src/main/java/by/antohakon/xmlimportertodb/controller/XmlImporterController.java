package by.antohakon.xmlimportertodb.controller;

import by.antohakon.xmlimportertodb.service.XmlImporterServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/xmlParser/xmlUrl")
public class XmlImporterController {

    private final XmlImporterServiceImpl xmlImporterService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping()
    public List<String> getAllTableNames(@RequestParam String xmlUrl){

     return xmlImporterService.getAllTableNames(xmlUrl);

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/getDdl")
    public String getDdl(@RequestParam String xmlUrl,
                         @RequestParam String tableName){
        return xmlImporterService.getTableDDL(xmlUrl, tableName);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/updateTable")
    public String updateTable(@RequestParam(required = false) String tableName,
                              @RequestParam String xmlUrl) {

        return xmlImporterService.updateTablesFromParam(tableName, xmlUrl);
    }


}
