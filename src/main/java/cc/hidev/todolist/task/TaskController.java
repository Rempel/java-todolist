package cc.hidev.todolist.task;

import org.springframework.web.bind.annotation.RestController;

import cc.hidev.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepositoryInterface taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        UUID userId = getUserId(request);
        taskModel.setUserId(userId);

        LocalDateTime currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt())) {
            return ResponseEntity.badRequest().body("Start date must be greater than current date");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.badRequest().body("Start date must be less than end date");
        }

        TaskModel task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    private UUID getUserId(HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        return userId;
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        UUID userId = getUserId(request);
        List<TaskModel> tasks = taskRepository.findByUserId(userId);
        return tasks;
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
        TaskModel task = this.taskRepository.findById(id).orElse(null);
        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }

        UUID userId = getUserId(request);
        if (!task.getUserId().equals(userId)) {
            return ResponseEntity.badRequest().body("You are not allowed to update this task");
        }

        Utils.copyNonNullProperties(taskModel, task);

        TaskModel taskUpdated = this.taskRepository.save(task);
        return ResponseEntity.ok().body(taskUpdated);
    }

}
