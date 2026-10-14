package com.acme.salarymgmt.controller;

import com.acme.salarymgmt.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/csv")
    public void exportCsv(
            @RequestParam String export,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String dept,
            HttpServletResponse response) throws IOException {

        LocalDate now = LocalDate.now();
        String filename = "acme_" + export + "_export_" + now.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);

        if ("employees".equals(export)) {
            reportService.exportEmployeesCsv(response.getOutputStream(), search, country, dept, null, false);
        } else {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }
    }
}
