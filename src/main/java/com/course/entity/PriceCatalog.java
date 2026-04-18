package com.course.entity;

import com.course.enums.EntityType;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "price_catalog_tbl")
public class PriceCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityType entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false)
    private Double amount;

    private Double discountedAmount;

    private Boolean isActive = true;

    private Long updatedBy;

    // Default Constructor
    public PriceCatalog() {
    }

    // Full Constructor
    public PriceCatalog(EntityType entityType, Long entityId, Double amount, Double discountedAmount, Long updatedBy) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.amount = amount;
        this.discountedAmount = discountedAmount;
        this.updatedBy = updatedBy;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Double getDiscountedAmount() { return discountedAmount; }
    public void setDiscountedAmount(Double discountedAmount) { this.discountedAmount = discountedAmount; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }

    // Equals and HashCode (Crucial for Entities)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceCatalog that = (PriceCatalog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}