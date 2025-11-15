package com.hometutor.service;

import com.hometutor.entity.*;
import com.hometutor.repository.BookingRequestRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookingService {
    private final BookingRequestRepository repo;
    private final TutorSlotService slotService;
    public BookingService(BookingRequestRepository repo, TutorSlotService slotService){ this.repo=repo; this.slotService = slotService; }
    public BookingRequest create(BookingRequest b){ return repo.save(b); }
    public BookingRequest approve(Long id){ BookingRequest br=repo.findById(id).orElseThrow(); br.setStatus(BookingRequest.Status.APPROVED); return repo.save(br); }
    public BookingRequest reject(Long id){ 
        BookingRequest br=repo.findById(id).orElseThrow(); 
        br.setStatus(BookingRequest.Status.REJECTED); 
        // reopen the slot if linked
        if(br.getSlotId()!=null){
            try{
                com.hometutor.entity.TutorSlot s = slotService.get(br.getSlotId());
                s.setOpen(true);
                slotService.save(s);
            }catch(Exception ex){ /* ignore */ }
        }
        return repo.save(br); 
    }
    public List<BookingRequest> all(){ return repo.findAll(); }
    public BookingRequest get(Long id){ return repo.findById(id).orElseThrow(); }
}