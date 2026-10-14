package com.acme.salarymgmt.model;

import jakarta.persistence.*;

@Entity
@Table(name = "currencies")
public class Currency {

    @Id
    @Column(name = "code", length = 3)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 5)
    private String symbol;

    @Column(name = "native_name", length = 80)
    private String nativeName;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public Currency() {}

    public Currency(String code, String name, String symbol, String nativeName, Boolean isActive) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.nativeName = nativeName;
        this.isActive = isActive;
    }

    // --- Getters and Setters ---
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getNativeName() { return nativeName; }
    public void setNativeName(String nativeName) { this.nativeName = nativeName; }
    public Boolean getActive() { return isActive; }
    public void setActive(Boolean active) { isActive = active; }
}
