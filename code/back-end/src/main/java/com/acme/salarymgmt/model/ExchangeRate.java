package com.acme.salarymgmt.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "exchange_rates")
@SQLRestriction("is_active = true")
public class ExchangeRate {

    @EmbeddedId
    private ExchangeRateId id;

    @Column(precision = 12, scale = 8)
    private BigDecimal rate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(length = 50)
    private String source;

    public ExchangeRate() {}

    // --- Getters and Setters ---
    public ExchangeRateId getId() { return id; }
    public void setId(ExchangeRateId id) { this.id = id; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    @Embeddable
    public static class ExchangeRateId implements java.io.Serializable {
        @Column(name = "from_code", length = 3)
        private String fromCode;

        @Column(name = "to_code", length = 3)
        private String toCode;

        public ExchangeRateId() {}

        public String getFromCode() { return fromCode; }
        public void setFromCode(String fromCode) { this.fromCode = fromCode; }
        public String getToCode() { return toCode; }
        public void setToCode(String toCode) { this.toCode = toCode; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ExchangeRateId)) return false;
            ExchangeRateId that = (ExchangeRateId) o;
            return fromCode.equals(that.fromCode) && toCode.equals(that.toCode);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(fromCode, toCode);
        }
    }
}
