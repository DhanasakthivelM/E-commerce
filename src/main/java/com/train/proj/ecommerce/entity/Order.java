package com.train.proj.ecommerce.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "orders") // Using orders because Order is a reserved keyword in SQL
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "orderId")
	private int orderId;
	
	@Column(nullable = false)
	private float totalAmount;
	
	@Column(nullable = false)
	private LocalDate orderDate;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;
	
	// Many-to-One relationship with User
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId", nullable = false)
	private User user;
	
	// One-to-Many relationship with Payment
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Payment> payments;

	public enum Status {
		PENDING, SHIPPED, DELIVERED, CANCELLED
	}
	
	// Constructors
	public Order() {}
	
	public Order(User user, float totalAmount, LocalDate orderDate, Status status) {
		this.user = user;
		this.totalAmount = totalAmount;
		this.orderDate = orderDate;
		this.status = status;
	}

	// Getters and Setters
	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public float getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(float totalAmount) {
		this.totalAmount = totalAmount;
	}
	
	public LocalDate getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDate orderDate) {
		this.orderDate = orderDate;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public List<Payment> getPayments() {
		return payments;
	}

	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}
	
	// Convenience method to get userId for backwards compatibility
	public int getUserId() {
		return user != null ? user.getUserId() : 0;
	}
}
