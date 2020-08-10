package com.softserve.edu.controller;

import com.softserve.edu.model.Marathon;
import com.softserve.edu.model.Role;
import com.softserve.edu.model.User;
import com.softserve.edu.service.MarathonService;
import com.softserve.edu.service.RoleService;
import com.softserve.edu.service.UserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Data
public class UserController {

    private UserService userService;
    private RoleService roleService;
    private MarathonService marathonService;
    private PasswordEncoder passwordEncoder;

    public UserController(UserService userService, RoleService roleService, MarathonService marathonService) {
        this.userService = userService;
        this.roleService = roleService;
        this.marathonService = marathonService;
    }

    @Autowired
    @Qualifier("bCrypt")
    public void setPasswordEncoder(@Lazy PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasAuthority('MENTOR')")
    @GetMapping("/create-student")
    public String createStudent(Model model) {
        List<Role> roles = roleService.getAll();
        model.addAttribute("roles", roles);
        model.addAttribute("user", new User());
        return "create-student";
    }

    @PreAuthorize("hasAuthority('MENTOR')")
    @PostMapping("students/add")
    public String createStudent(@RequestParam(value = "marathon_id", required = false, defaultValue = "0") long marathonId, @RequestParam("role_id") long roleId,
                                @Validated @ModelAttribute User user, BindingResult result) {
        if (result.hasErrors()) {
            return "create-student";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(roleService.getRoleById(roleId));
        if (marathonId != 0) {
            userService.addUserToMarathon(
                    userService.createOrUpdateUser(user),
                    marathonService.getMarathonById(marathonId));
            return "redirect:/students/" + marathonId;
        }
        userService.createOrUpdateUser(user);
        return "redirect:/students";
    }

    @PreAuthorize("hasAuthority('MENTOR')")
    @GetMapping("students/{marathon_id}/add")
    public String createStudent(@RequestParam("user_id") long userId, @PathVariable("marathon_id") long marathonId) {
        userService.addUserToMarathon(
                userService.getUserById(userId),
                marathonService.getMarathonById(marathonId));
        return "redirect:/students/" + marathonId;
    }

    @PreAuthorize("hasAuthority('MENTOR')")
    @GetMapping("/students/{marathon_id}/edit/{student_id}")
    public String updateStudent(@PathVariable("marathon_id") long marathonId, @PathVariable("student_id") long studentId,
                                Model model) {
        User user = userService.getUserById(studentId);
        List<Role> roles = roleService.getAll();
        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        model.addAttribute("marathon_id", marathonId);
        return "update-student";
    }

    @PreAuthorize("hasAuthority('MENTOR')")
    @PostMapping("/students/edit/{id}")
    public String updateStudent(@PathVariable long id, @RequestParam("role_id") long roleId,
                                @RequestParam(value = "marathon_id", required = false, defaultValue = "0") long marathonId,
                                @Validated @ModelAttribute User user, BindingResult result) {
        if (result.hasErrors()) {
            return "update-marathon";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(roleService.getRoleById(roleId));
        userService.createOrUpdateUser(user);
        if (marathonId != 0) {
            return "redirect:/students/" + marathonId;
        }
        return "redirect:/students";
    }

    @PreAuthorize("hasAuthority('MENTOR')")
    @GetMapping("/students/{marathon_id}/delete/{student_id}")
    public String deleteStudent(@PathVariable("marathon_id") long marathonId, @PathVariable("student_id") long studentId) {
        userService.deleteUserFromMarathon(
                userService.getUserById(studentId),
                marathonService.getMarathonById(marathonId));
        return "redirect:/students/" + marathonId;
    }

    @PreAuthorize("hasAuthority('MENTOR')")
    @GetMapping("/students")
    public String getAllStudents(Model model) {
        List<User> students = userService.getAll();
        model.addAttribute("students", students);
        return "students";
    }

    @PreAuthorize("hasAuthority('MENTOR')")
    @GetMapping("/students/edit/{id}")
    public String updateStudent(@PathVariable long id, Model model) {
        User user = userService.getUserById(id);
        List<Role> roles = roleService.getAll();
        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        return "update-student";
    }

    @PreAuthorize("hasAuthority('MENTOR')")
    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable long id) {
        User student = userService.getUserById(id);
        for (Marathon marathon : student.getMarathons()) {
            userService.deleteUserFromMarathon(student, marathon);
        }
        userService.deleteUserById(id);
        return "redirect:/students";
    }

    @GetMapping("/user/{id}")
    public String showUser(@PathVariable("id") long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "user-info";
    }

}
