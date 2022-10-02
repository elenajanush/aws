package com.finki.file.controller;

import com.finki.file.service.FileUploadService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

import org.supercsv.io.*;
import org.supercsv.prefs.CsvPreference;

@Controller
public class FileUploadController {

    private final FileUploadService fileUploadService;

    private List<String> outputList;
    private List<String> inputList;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @GetMapping(path = "/")
    public String index() {
        return "index";
    }


    @PostMapping(path = "/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes attributes) {
        inputList = new ArrayList<>();
        outputList = new ArrayList<>();

        long timeBefore = System.currentTimeMillis();

        if (file.isEmpty()) {
            return responseMessage(attributes,"ERROR","Please select a file to upload");
        }
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (!fileName.endsWith(".txt")) {
            return responseMessage(attributes,"ERROR","Wrong input format. Only .txt files can be processed");
        }
        try {
            this.outputList = fileUploadService.processInputData(file);
            this.inputList = fileUploadService.getInputData(file);
        } catch (IOException e) {
            return responseMessage(attributes,"ERROR","Error has occurred while processing input data" + e);
        }
        if (this.outputList.size() < 1) {
            return responseMessage(attributes,"ERROR","Wrong values inside txt file. Each line should have 3 numbers (Example: 45 613 480)");
        }
        long timeAfter = System.currentTimeMillis();
        return responseMessage(attributes,"OK","You successfully processed " + fileName + "! and it took " + (timeAfter - timeBefore) + "ms to process the file ");
    }

    private String responseMessage(RedirectAttributes attributes, String status, String message) {
        attributes.addFlashAttribute("status", status);
        attributes.addFlashAttribute("message", message);
        return "redirect:/";
    }

    @GetMapping("/resultOnScreen")
    public String displayOnScreen(Model model) {
        final List<String> header = new ArrayList<>();
        header.add("No.");
        header.add("Input");
        header.add("Result");
        model.addAttribute("tableHeader", header);

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < inputList.size(); i++) {
            map.put(inputList.get(i), outputList.get(i));
        }
        model.addAttribute("resultList", map);
        return "result";
    }

    @GetMapping("/downloadCsv")
    public void downloadCSV(HttpServletResponse response) throws IOException {
        CsvBeanWriter csvBeanWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=neighborDifferenceCSV.csv";
        response.setHeader(headerKey, headerValue);

        csvBeanWriter.writeHeader("Difference");
        for (String single : outputList) {
            csvBeanWriter.writeComment(single);
        }
        csvBeanWriter.close();
    }

    @GetMapping("/downloadTxt")
    public void downloadTXT(HttpServletResponse response) throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=\"neighborDifferenceTXT.txt\";";
        response.setHeader(headerKey, headerValue);

        OutputStream outStream = response.getOutputStream();
        for (String single : outputList) {
            outStream.write(single.getBytes());
            outStream.write(String.format("%n").getBytes());

        }
        outStream.close();
    }

}