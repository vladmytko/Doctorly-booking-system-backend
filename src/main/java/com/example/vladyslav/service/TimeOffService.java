package com.example.vladyslav.service;

import com.example.vladyslav.exception.NotFoundException;
import com.example.vladyslav.exception.OurException;
import com.example.vladyslav.model.TimeOff;
import com.example.vladyslav.repository.TimeOffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeOffService {

    private final TimeOffRepository timeOffRepository;

    public TimeOff get(String id){
        return timeOffRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("TimeOff not found: " + id));
    }

    public TimeOff create(TimeOff timeOff) {
        validate(timeOff);
        // Optional: check for overlapping time-off for same doctor
        List<TimeOff> overlaps = timeOffRepository
                .findByDoctorIdAndStartLessThanEqualAndEndGreaterThanEqual(
                        timeOff.getDoctorId(), timeOff.getEnd(), timeOff.getStart());
        if(!overlaps.isEmpty()) {
            throw new OurException("Overlapping time-off exist for this doctor.");
        }
        return timeOffRepository.save(timeOff);
    }

    public void delete(String id){
        timeOffRepository.deleteById(id);
    }

    public void validate(TimeOff timeOff){
        if(timeOff.getDoctorId() == null) throw new OurException("Doctor is required");
        if(timeOff.getStart() == null || timeOff.getEnd() == null || !timeOff.getEnd().isAfter(timeOff.getStart())){
            throw new OurException("Invalid start/end for time-off");
        }
        if(timeOff.getStart().isBefore(Instant.EPOCH)){
            throw new OurException("start can not be before 1970-01-01.");
        }
    }



}
