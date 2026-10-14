package com.acme.salarymgmt.dto;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

public class FilterQueryRequest {

    private String search;
    private String country;
    private String dept;
    private String status;

    @PageableDefault(size = 25, page = 0)
    private Pageable pageable;

    // --- Getters and Setters ---
    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Pageable getPageable() { return pageable; }
    public void setPageable(Pageable pageable) { this.pageable = pageable; }
}
