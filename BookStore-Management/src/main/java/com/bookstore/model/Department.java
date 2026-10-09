package com.bookstore.model;

public class Department {
    private int id;
    private String departmentName;
    private Integer managerId;
    private String managerName;

    public Department() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Integer getManagerId() { return managerId; }
    public void setManagerId(Integer managerId) { this.managerId = managerId; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    @Override public String toString() { return departmentName; }
}
