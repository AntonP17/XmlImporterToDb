package by.antohakon.xmlimportertodb.controller;

import by.antohakon.xmlimportertodb.service.XmlImporterGroovy;
import by.antohakon.xmlimportertodb.service.XmlImporterServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/xmlParser")
public class XmlImporterController {

    private final XmlImporterServiceImpl xmlImporterService;


    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/xmlUrl")
    public List<String> getAllTableNames(@RequestParam String xmlUrl){

     //   return xmlImporterService.getAllTableNames(xmlUrl);
        XmlImporterGroovy xmlImporterGroovy = new XmlImporterGroovy();
       return xmlImporterGroovy.getTableNames(xmlUrl);

    }

}
