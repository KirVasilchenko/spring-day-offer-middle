package com.onedayoffer.taskdistribution.repositories;

import com.onedayoffer.taskdistribution.repositories.entities.Employee;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    List<Employee> findAll(Sort sort);
}
