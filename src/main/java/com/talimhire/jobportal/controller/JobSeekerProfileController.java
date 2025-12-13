package com.talimhire.jobportal.controller;

import com.talimhire.jobportal.entity.JobSeekerProfile;
import com.talimhire.jobportal.entity.Skills;
import com.talimhire.jobportal.entity.Users;
import com.talimhire.jobportal.repository.UsersRepository;
import com.talimhire.jobportal.services.JobSeekerProfileService;
import com.talimhire.jobportal.util.FileUploadUtil; // ✅ Updated Import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/job-seeker-profile")
public class JobSeekerProfileController {

    private final JobSeekerProfileService jobSeekerProfileService;
    private final UsersRepository usersRepository;
    private final FileUploadUtil fileUploadUtil; // ✅ Inject Helper

    @Autowired
    public JobSeekerProfileController(JobSeekerProfileService jobSeekerProfileService,
                                      UsersRepository usersRepository,
                                      FileUploadUtil fileUploadUtil) {
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.usersRepository = usersRepository;
        this.fileUploadUtil = fileUploadUtil;
    }

    @GetMapping("/")
    public String jobSeekerProfile(Model model) {
        JobSeekerProfile jobSeekerProfile = new JobSeekerProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Skills> skills = new ArrayList<>();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Users user = usersRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found."));
            Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(user.getUserId());

            if (seekerProfile.isPresent()) {
                jobSeekerProfile = seekerProfile.get();
                if (jobSeekerProfile.getSkills().isEmpty()) {
                    skills.add(new Skills());
                    jobSeekerProfile.setSkills(skills);
                }
            }
            model.addAttribute("skills", skills);
            model.addAttribute("profile", jobSeekerProfile);
        }
        return "job-seeker-profile";
    }

    @PostMapping("/addNew")
    public String addNew(JobSeekerProfile jobSeekerProfile,
                         @RequestParam("image") MultipartFile image,
                         @RequestParam("pdf") MultipartFile pdf,
                         Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Users user = usersRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found."));
            jobSeekerProfile.setUserId(user);
            jobSeekerProfile.setUserAccountId(user.getUserId());
        }

        List<Skills> skillsList = new ArrayList<>();
        model.addAttribute("profile", jobSeekerProfile);
        model.addAttribute("skills", skillsList);

        for (Skills skills : jobSeekerProfile.getSkills()) {
            skills.setJobSeekerProfile(jobSeekerProfile);
        }

        // ✅ NEW LOGIC: Upload to Cloudinary
        try {
            // 1. Upload Photo
            if (!image.isEmpty()) {
                String imageUrl = fileUploadUtil.uploadFile(image);
                jobSeekerProfile.setProfilePhoto(imageUrl); // Saves URL (e.g. https://res.cloudinary...)
            }

            // 2. Upload Resume
            if (!pdf.isEmpty()) {
                String pdfUrl = fileUploadUtil.uploadFile(pdf);
                jobSeekerProfile.setResume(pdfUrl); // Saves URL
            }

        } catch (IOException e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }

        // Save to DB (The strings saved are now URLs, not filenames)
        jobSeekerProfileService.addNew(jobSeekerProfile);

        return "redirect:/dashboard";
    }

    @GetMapping("/{id}")
    public String candidateProfile(@PathVariable("id") int id, Model model) {
        Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(id);
        model.addAttribute("profile", seekerProfile.get());
        return "job-seeker-profile";
    }

    // ❌ DELETED: downloadResume()
    // We don't need Java to download anymore. The HTML link will do it directly.
}