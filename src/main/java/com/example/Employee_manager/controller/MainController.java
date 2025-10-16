package com.example.Employee_manager.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication != null) {
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            model.addAttribute("username", authentication.getName());
            model.addAttribute("role", role);
            
            // All users now use the admin dashboard
            return "admin-dashboard";
        }
        return "redirect:/login";
    }


    @GetMapping("/init-users")
    @ResponseBody
    public String initUsers() {
        return "In-memory users are ready: admin/admin123, manager/manager123, user/user123";
    }
}