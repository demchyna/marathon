package com.softserve.edu.controller;

import com.softserve.edu.model.Marathon;
import com.softserve.edu.model.User;
import com.softserve.edu.security.WebAuthenticationToken;
import com.softserve.edu.service.MarathonService;
import com.softserve.edu.service.UserService;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@Data
public class MarathonController {
    private MarathonService marathonService;
    private UserService studentService;

    public MarathonController(MarathonService marathonService, UserService studentService) {
        this.marathonService = marathonService;
        this.studentService = studentService;
    }

    @PreAuthorize("hasAuthority('MENTOR')")
    @GetMapping("/create-marathon")
    public String createMarathon(Model model) {
        model.addAttribute("marathon", new Marathon());
        return "create-marathon";
    }

    @PreAuthorize("hasAuthority('MENTOR')")
    @PostMapping("/marathons")
    public String createMarathon(@Validated @ModelAttribute Marathon marathon, BindingResult result) {
        if (result.hasErrors()) {
            return "create-marathon";
        }
        marathonService.createOrUpdate(marathon);
        return "redirect:/marathons";
    }

    @PreAuthorize("hasAuthority('MENTOR')")
    @GetMapping("/marathons/edit/{id}")
    public String updateMarathon(@PathVariable long id, Model model) {
        Marathon marathon = marathonService.getMarathonById(id);
        model.addAttribute("marathon", marathon);
        return "update-marathon";
    }

    @PreAuthorize("hasAuthority('MENTOR')")
    @PostMapping("/marathons/edit/{id}")
    public String updateMarathon(@PathVariable long id, @ModelAttribute Marathon marathon, BindingResult result) {
        if (result.hasErrors()) {
            return "update-marathon";
        }
        marathonService.createOrUpdate(marathon);
        return "redirect:/marathons";
    }

    @PreAuthorize("hasAuthority('MENTOR')")
    @GetMapping("/marathons/delete/{id}")
    public String deleteMarathon(@PathVariable long id) {
        marathonService.deleteMarathonById(id);
        return "redirect:/marathons";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/students/{marathon_id}")
    public String getStudentsFromMarathon(@PathVariable("marathon_id") long marathonId, Model model) {
        WebAuthenticationToken authentication
                = (WebAuthenticationToken)SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("TRAINEE"))) {
            return "redirect:/sprints/" + marathonId;
        }
        List<User> students = studentService.getAll().stream().filter(
                student -> student.getMarathons().stream().anyMatch(
                        marathon -> marathon.getId() == marathonId)).collect(Collectors.toList());
        Marathon marathon = marathonService.getMarathonById(marathonId);
        model.addAttribute("students", students);
        model.addAttribute("all_students", studentService.getAll());
        model.addAttribute("marathon", marathon);
        return "marathon-students";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/marathons")
    public String getAllMarathons(Model model) {
        List<Marathon> marathons;
        WebAuthenticationToken authentication
                = (WebAuthenticationToken)SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("MENTOR"))) {
            marathons = marathonService.getAll();
        } else {
            User user = (User)authentication.getDetails();
            marathons = user.getMarathons();
        }
        model.addAttribute("marathons", marathons);
        return "marathons";
    }
}
