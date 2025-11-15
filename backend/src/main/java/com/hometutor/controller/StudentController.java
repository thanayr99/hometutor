package com.hometutor.controller;

import com.hometutor.entity.BookingRequest;
import com.hometutor.entity.StudentProfile;
import com.hometutor.entity.TutorProfile;
import com.hometutor.service.BookingService;
import com.hometutor.service.StudentService;
import com.hometutor.service.TutorService;
import com.hometutor.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    private final StudentService studentService;
    private final TutorService tutorService;
    private final BookingService bookingService;
    private final UserService userService;
    private final com.hometutor.service.TutorSlotService tutorSlotService;

    public StudentController(StudentService studentService, TutorService tutorService, BookingService bookingService, UserService userService, com.hometutor.service.TutorSlotService tutorSlotService){
        this.studentService = studentService;
        this.tutorService = tutorService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.tutorSlotService = tutorSlotService;
    }

    @GetMapping("/profile/{id}")
    public StudentProfile profile(@PathVariable Long id){
        return studentService.get(id);
    }

    @PostMapping("/profile")
    public StudentProfile saveProfile(@RequestBody Map<String,String> b){
        com.hometutor.auth.AuthTokenService.Principal current = com.hometutor.auth.CurrentUser.get();
        if(current==null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");

        StudentProfile s = new StudentProfile();
    s.setPhone(b.get("phone"));
    s.setFullName(b.get("fullName"));
    s.setFatherName(b.get("fatherName"));
    s.setDob(b.get("dob"));
    s.setAddress(b.get("address"));
    s.setCity(b.get("city"));
    if(b.containsKey("school")) s.setSchool(b.get("school"));
    if(b.containsKey("grade")) s.setGrade(b.get("grade"));
    // accept either a url or a base64 data string under photoBase64
    if(b.containsKey("photoUrl")) s.setPhotoUrl(b.get("photoUrl"));
    else if(b.containsKey("photoBase64")) s.setPhotoUrl(b.get("photoBase64"));
        // if userId provided, ensure owner or admin
        if(b.containsKey("userId")){
            Long uid = Long.valueOf(b.get("userId"));
            if(!current.role.equals("ADMIN") && !current.userId.equals(uid)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Forbidden");
            // attach user and copy basic info into the user record if provided
            com.hometutor.entity.User u = userService.get(uid);
            if(b.containsKey("name")) u.setName(b.get("name"));
            if(b.containsKey("phone")) u.setPhone(b.get("phone"));
            // fatherName is stored on profile; optional to mirror to user.name or another field if desired
            userService.save(u);
            s.setUser(u);
        } else {
            // default to current user
            com.hometutor.entity.User u = userService.get(current.userId);
            if(b.containsKey("name")) u.setName(b.get("name"));
            if(b.containsKey("phone")) u.setPhone(b.get("phone"));
            userService.save(u);
            s.setUser(u);
        }
        return studentService.save(s);
    }

    @GetMapping("/profile/by-user/{userId}")
    public StudentProfile profileByUser(@PathVariable Long userId){
        com.hometutor.auth.AuthTokenService.Principal current = com.hometutor.auth.CurrentUser.get();
        if(current==null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        if(!current.role.equals("ADMIN") && !current.userId.equals(userId)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Forbidden");
        return studentService.findByUserId(userId);
    }

    @PutMapping("/profile/{id}")
    public StudentProfile updateProfile(@PathVariable Long id, @RequestBody Map<String,String> b){
        com.hometutor.auth.AuthTokenService.Principal current = com.hometutor.auth.CurrentUser.get();
        if(current==null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        StudentProfile s = studentService.get(id);
        Long ownerId = s.getUser()!=null ? s.getUser().getId() : null;
        if(!current.role.equals("ADMIN") && (ownerId==null || !ownerId.equals(current.userId))) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Forbidden");
    if(b.containsKey("phone")) s.setPhone(b.get("phone"));
    if(b.containsKey("fullName")) s.setFullName(b.get("fullName"));
    if(b.containsKey("fatherName")) s.setFatherName(b.get("fatherName"));
    if(b.containsKey("dob")) s.setDob(b.get("dob"));
    if(b.containsKey("address")) s.setAddress(b.get("address"));
    if(b.containsKey("city")) s.setCity(b.get("city"));
    if(b.containsKey("school")) s.setSchool(b.get("school"));
    if(b.containsKey("grade")) s.setGrade(b.get("grade"));
    if(b.containsKey("photoUrl")) s.setPhotoUrl(b.get("photoUrl"));
    else if(b.containsKey("photoBase64")) s.setPhotoUrl(b.get("photoBase64"));
        // also update linked user basic info when provided
        if(s.getUser()!=null){
            com.hometutor.entity.User u = s.getUser();
            if(b.containsKey("name")) u.setName(b.get("name"));
            if(b.containsKey("phone")) u.setPhone(b.get("phone"));
            userService.save(u);
        }
        return studentService.save(s);
    }

    @GetMapping("/search")
    public List<TutorProfile> search(@RequestParam(required=false) String subject,
                                     @RequestParam(required=false) String day,
                                     @RequestParam(required=false) Double maxCost){
        return tutorService.search(subject, day, maxCost);
    }

    @PostMapping("/request")
    public BookingRequest create(@RequestBody Map<String,String> dto){
        Long tutorId = Long.valueOf(dto.get("tutorId"));
        Long studentId = Long.valueOf(dto.get("studentId"));
        Long slotId = dto.containsKey("slotId") ? Long.valueOf(dto.get("slotId")) : null;
        com.hometutor.auth.AuthTokenService.Principal current = com.hometutor.auth.CurrentUser.get();
        if(current==null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        if(!current.role.equals("ADMIN") && !current.userId.equals(studentId)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Forbidden");

        TutorProfile t = tutorService.get(tutorId);
        StudentProfile s = studentService.get(studentId);

        BookingRequest br = new BookingRequest();
        br.setStudent(s);
        br.setTutor(t);
        br.setSubject(dto.get("subject"));

        // If slotId provided, lock the slot (set open=false) and attach
        if(slotId!=null){
            com.hometutor.entity.TutorSlot slot = tutorSlotService.get(slotId);
            if(!slot.isOpen()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Slot already booked");
            slot.setOpen(false);
            tutorSlotService.save(slot);
            br.setSlotId(slotId);
            br.setRequestedSlot(slot.getDate()+" "+slot.getStart()+"-"+slot.getEnd());
        } else {
            br.setRequestedSlot(dto.get("requestedSlot"));
        }

        return bookingService.create(br);
    }

    @GetMapping("/requests/{studentId}")
    public List<BookingRequest> myRequests(@PathVariable Long studentId){
        return bookingService.all().stream().filter(b -> b.getStudent()!=null && b.getStudent().getId().equals(studentId)).toList();
    }

    @GetMapping("/dashboard/{studentId}")
    public Map<String,Object> dashboard(@PathVariable Long studentId){
        Map<String,Object> m = new HashMap<>();
        List<BookingRequest> all = bookingService.all().stream().filter(b -> b.getStudent()!=null && b.getStudent().getId().equals(studentId)).toList();
        long pending = all.stream().filter(b -> b.getStatus()==BookingRequest.Status.PENDING).count();
        long approved = all.stream().filter(b -> b.getStatus()==BookingRequest.Status.APPROVED).count();
        long rejected = all.stream().filter(b -> b.getStatus()==BookingRequest.Status.REJECTED).count();
        m.put("totalRequests", all.size());
        m.put("pendingRequests", pending);
        m.put("approvedRequests", approved);
        m.put("rejectedRequests", rejected);
        m.put("requests", all);
        return m;
    }
}
