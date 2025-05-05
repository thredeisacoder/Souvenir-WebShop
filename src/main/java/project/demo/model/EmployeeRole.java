package project.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "EmployeeRole", schema = "dbo", catalog = "SouvenirShopDB")
public class EmployeeRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_role_id")
    private Integer employeeRoleId;

    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "status", length = 20)
    private String status;

    @ManyToOne
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;

    public EmployeeRole() {}

    public EmployeeRole(Integer employeeRoleId, Integer employeeId, Integer roleId,
            String status) {
        this.employeeRoleId = employeeRoleId;
        this.employeeId = employeeId;
        this.roleId = roleId;
        this.status = status;
    }

    public Integer getEmployeeRoleId() {
        return employeeRoleId;
    }

    public void setEmployeeRoleId(Integer employeeRoleId) {
        this.employeeRoleId = employeeRoleId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
