package com.hometutor.repository;
import com.hometutor.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BookingRequestRepository extends JpaRepository<BookingRequest,Long>{
    List<BookingRequest> findByTutor(TutorProfile tutor);
    List<BookingRequest> findByStudent(StudentProfile student);
}