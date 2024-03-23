package com.onedayoffer.taskdistribution.services;

import com.onedayoffer.taskdistribution.DTO.EmployeeDTO;
import com.onedayoffer.taskdistribution.DTO.TaskDTO;
import com.onedayoffer.taskdistribution.DTO.TaskStatus;
import com.onedayoffer.taskdistribution.exception.EmployeeNotFound;
import com.onedayoffer.taskdistribution.exception.TaskNotFound;
import com.onedayoffer.taskdistribution.exception.UnknownValueException;
import com.onedayoffer.taskdistribution.repositories.EmployeeRepository;
import com.onedayoffer.taskdistribution.repositories.TaskRepository;
import com.onedayoffer.taskdistribution.repositories.entities.Employee;
import com.onedayoffer.taskdistribution.repositories.entities.Task;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    public List<EmployeeDTO> getEmployees(@Nullable String sortDirection) {
        List<Employee> allEmployees;
        if (sortDirection == null || sortDirection.isEmpty()) {
            log.debug("Retrieving all employees without sorting");
            allEmployees = employeeRepository.findAll();
        } else {
            log.debug("Retrieving employees sorted in " + sortDirection + " direction");
            Sort.Direction direction;
            try {
                direction = Sort.Direction.fromString(sortDirection);
            } catch (IllegalArgumentException e) {
                throw new UnknownValueException("Unknown sort direction: " + sortDirection);
            }
            allEmployees = employeeRepository.findAll(Sort.by(direction, "fio"));
        }
        List<EmployeeDTO> mappedEmployees = modelMapper.map(
                allEmployees,
                new TypeToken<List<EmployeeDTO>>() {}.getType()
        );
        log.debug("Retrieved {} employees", allEmployees.size());
        return mappedEmployees;
    }

    @Transactional
    public EmployeeDTO getOneEmployee(Integer id) {
        log.info("Fetching employee with id: {}", id);
        var employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFound("Employee with id " + id + " not found"));
        log.info("Employee fetched successfully: {}", employee.getId());
        return modelMapper.map(employee, EmployeeDTO.class);
    }

    public List<TaskDTO> getTasksByEmployeeId(Integer id) {
        log.debug("Fetching tasks for employee with id: {}", id);
        List<Task> tasks = taskRepository.findAllByEmployeeId(id);
        log.debug("Retrieved {} tasks for employee with id: {}", tasks.size(), id);
        List<TaskDTO> taskDTOs = modelMapper.map(
                tasks,
                new TypeToken<List<TaskDTO>>() {}.getType()
        );
        log.debug("Mapped tasks to TaskDTOs for employee with id: {}", id);
        return taskDTOs;
    }

    @Transactional
    public void changeTaskStatus(Integer id, Integer taskId, String newStatus) {
        log.debug("Changing task status for employee with id: {}, task with id: {}, new status: {}", id, taskId, newStatus);
        TaskStatus status;
        try {
            status = TaskStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new UnknownValueException("Unknown status: " + newStatus);
        }
        var task = taskRepository.findById(taskId).orElseThrow(() ->
                new TaskNotFound("Task with id " + taskId + " not found"));
        if (!Objects.equals(task.getEmployee().getId(), id)) {
            throw new TaskNotFound("Task with id " + taskId + " not found");
        }
        task.setStatus(status);
        log.debug("Changed task status for employee with id: {}, task with id: {}, new status: {}", id, taskId, newStatus);
    }

    @Transactional
    public void postNewTask(Integer employeeId, TaskDTO newTask) {
        log.debug("Posting new task for employee with id: {}, new task: {}", employeeId, newTask);
        var employee = employeeRepository.findById(employeeId).orElseThrow(() ->
                new EmployeeNotFound("Employee with id " + employeeId + " not found"));
        var task = modelMapper.map(newTask, Task.class);
        task.setEmployee(employee);
        taskRepository.save(task);
        log.debug("Posted new task for employee with id: {}, new task: {}", employeeId, newTask);
    }
}
