package com.company.firemanagement.domains.shift.entity;

import com.company.firemanagement.domains.geography.entity.FireStation;
import com.company.firemanagement.domains.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "employee_shifts")
@Getter
@Setter
public class EmployeeShift {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private FireStation station;

    @Column(name = "check_in_time", nullable = false)
    private Instant checkInTime = Instant.now();

    @Column(name = "check_out_time")
    private Instant checkOutTime;

    @Column(name = "check_in_latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal checkInLatitude;

    @Column(name = "check_in_longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal checkInLongitude;

    @Column(name = "check_out_latitude", precision = 9, scale = 6)
    private BigDecimal checkOutLatitude;

    @Column(name = "check_out_longitude", precision = 9, scale = 6)
    private BigDecimal checkOutLongitude;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
