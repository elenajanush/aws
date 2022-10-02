package com.finki.file.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class FileUploadService {

    public void saveOutputFile(List<String> outputList) throws IOException {
            FileWriter myWriter = new FileWriter(".\\src\\main\\resources\\templates\\uploads\\result.txt");
            for (String outputLine : outputList) {
                myWriter.write(outputLine + "\n");
            }
    }

    public List<String> processInputData(MultipartFile file) throws IOException {
        List<String> result = new ArrayList<>();
        InputStream inputStream = file.getInputStream();
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .forEach(s -> {
                    String[] s1 = s.split(" ");
                    if (s1.length == 3) {
                        int diff1 = Difference(Integer.parseInt(s1[0]), Integer.parseInt(s1[1]));
                        int diff2 = Difference(Integer.parseInt(s1[1]), Integer.parseInt(s1[2]));
                        result.add(String.valueOf(diff1+diff2));
                    }
                });
        return result;
    }

    public List<String> getInputData(MultipartFile file) throws IOException {
        List<String> input = new ArrayList<>();
        InputStream inputStream = file.getInputStream();
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .forEach(input::add);
        return input;

    }

    private int Difference(int a, int b) {
        return Math.abs(a - b);
    }

}